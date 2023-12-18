/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package su.xash.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;
import android.provider.DocumentsContract;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import android.os.Environment;
import android.util.Log;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.webkit.MimeTypeMap;

public class InternalStorageProvider extends DocumentsProvider {

	public static final String AUTHORITY = "com.app.android.localstorage.documents";
	private static final String SIMPLE_NAME = "INTERNAL_FS";

	/**
	* Default root projection: everything but Root.COLUMN_MIME_TYPES
	*/
	private final static String[] DEFAULT_ROOT_PROJECTION = new String[]{
			DocumentsContract.Root.COLUMN_ROOT_ID,
			DocumentsContract.Root.COLUMN_FLAGS,
			DocumentsContract.Root.COLUMN_TITLE,
			DocumentsContract.Root.COLUMN_DOCUMENT_ID,
			DocumentsContract.Root.COLUMN_ICON,
			DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
	};
	/**
	* Default document projection: everything but Document.COLUMN_ICON and
	* Document.COLUMN_SUMMARY
	*/
	private final static String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
			DocumentsContract.Document.COLUMN_DOCUMENT_ID,
			DocumentsContract.Document.COLUMN_DISPLAY_NAME,
			DocumentsContract.Document.COLUMN_FLAGS,
			DocumentsContract.Document.COLUMN_MIME_TYPE,
			DocumentsContract.Document.COLUMN_SIZE,
			DocumentsContract.Document.COLUMN_LAST_MODIFIED
	};

	@Override
	public Cursor queryRoots(final String[] projection) throws FileNotFoundException {
		// Create a cursor with either the requested fields, or the default
		// projection if "projection" is null.
		final MatrixCursor result = new MatrixCursor(projection != null ? projection
				: DEFAULT_ROOT_PROJECTION);
		// Add Home directory
		File homeDir = new File("/data/data/su.xash.engine/files");
		final MatrixCursor.RowBuilder row = result.newRow();
		// These columns are required
		row.add(DocumentsContract.Root.COLUMN_ROOT_ID, homeDir.getAbsolutePath());
		row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, homeDir.getAbsolutePath());
		row.add(DocumentsContract.Root.COLUMN_TITLE, "Xash3D");
		row.add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_SUPPORTS_CREATE| DocumentsContract.Root.FLAG_SUPPORTS_SEARCH | DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD);
		row.add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_launcher);
		// These columns are optional
		row.add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, homeDir.getFreeSpace());
		// Root.COLUMN_MIME_TYPE is another optional column and useful if you
		// have multiple roots with different
		// types of mime types (roots that don't match the requested mime type
		// are automatically hidden)
		return result;
	}

	@Override
	public String createDocument(final String parentDocumentId, final String mimeType,
								final String displayName) throws FileNotFoundException {
		File newFile = new File(parentDocumentId, displayName);
		try {
			if( mimeType.equals("vnd.android.document/directory") )
				newFile.mkdir();
			else
				newFile.createNewFile();
			//Log.i(SIMPLE_NAME, "create file " + displayName + " " + mimeType);
			return newFile.getAbsolutePath();
		} catch (IOException e) {
			Log.e(SIMPLE_NAME, "Error creating new file " + newFile);
		}
		return null;
	}

	@Override
	public AssetFileDescriptor openDocumentThumbnail(final String documentId, final Point sizeHint,
													final CancellationSignal signal) throws FileNotFoundException {
		// Assume documentId points to an image file. Build a thumbnail no
		// larger than twice the sizeHint
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(documentId, options);
		final int targetHeight = 2 * sizeHint.y;
		final int targetWidth = 2 * sizeHint.x;
		final int height = options.outHeight;
		final int width = options.outWidth;
		options.inSampleSize = 1;
		if (height > targetHeight || width > targetWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / options.inSampleSize) > targetHeight
					|| (halfWidth / options.inSampleSize) > targetWidth) {
				options.inSampleSize *= 2;
			}
		}
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(documentId, options);
		// Write out the thumbnail to a temporary file
		File tempFile = null;
		FileOutputStream out = null;
		try {
			tempFile = File.createTempFile("thumbnail", null, getContext().getCacheDir());
			out = new FileOutputStream(tempFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (IOException e) {
			Log.e(SIMPLE_NAME, "Error writing thumbnail", e);
			return null;
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					Log.e(SIMPLE_NAME, "Error closing thumbnail", e);
				}
		}
		// It appears the Storage Framework UI caches these results quite
		// aggressively so there is little reason to
		// write your own caching layer beyond what you need to return a single
		// AssetFileDescriptor
		return new AssetFileDescriptor(ParcelFileDescriptor.open(tempFile,
				ParcelFileDescriptor.MODE_READ_ONLY), 0,
				AssetFileDescriptor.UNKNOWN_LENGTH);
	}

	@Override
	public Cursor queryChildDocuments(final String parentDocumentId, final String[] projection,
									final String sortOrder) throws FileNotFoundException {
		// Create a cursor with either the requested fields, or the default
		// projection if "projection" is null.
		final MatrixCursor result = new MatrixCursor(projection != null ? projection
				: DEFAULT_DOCUMENT_PROJECTION);
		final File parent = new File(parentDocumentId);
		for (File file : parent.listFiles()) {
			// Don't show hidden files/folders
			if (!file.getName().startsWith(".")) {
				// Adds the file's display name, MIME type, size, and so on.
				includeFile(result, file);
			}
		}
		return result;
	}

	@Override
	public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
		final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
		final File parent = new File(rootId);
		final LinkedList<File> pending = new LinkedList();
		pending.add(parent);

		final int MAX_SEARCH_RESULTS = 50;
		while (!pending.isEmpty() && result.getCount() < MAX_SEARCH_RESULTS) {
			final File file = pending.removeFirst();
			if (file.isDirectory()) {
				Collections.addAll(pending, file.listFiles());
			} else {
				if (file.getName().toLowerCase().contains(query)) {
					includeFile(result, file);
				}
			}
		}

		return result;
	}

	@Override
	public Cursor queryDocument(final String documentId, final String[] projection)
			throws FileNotFoundException {
		// Create a cursor with either the requested fields, or the default
		// projection if "projection" is null.
		final MatrixCursor result = new MatrixCursor(projection != null ? projection
				: DEFAULT_DOCUMENT_PROJECTION);
		includeFile(result, new File(documentId));
		return result;
	}
	@Override
	public boolean isChildDocument(String parentDocumentId, String documentId) {
		return documentId.startsWith(parentDocumentId);
	}

	private void includeFile(final MatrixCursor result, final File file)
			throws FileNotFoundException {
		final MatrixCursor.RowBuilder row = result.newRow();
		// These columns are required
		row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, file.getAbsolutePath());
		row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.getName());
		String mimeType = getDocumentType(file.getAbsolutePath());
		row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType);
		int flags = file.canWrite() ? DocumentsContract.Document.FLAG_SUPPORTS_DELETE | DocumentsContract.Document.FLAG_SUPPORTS_WRITE
				: 0;
		if( file.isDirectory() && flags != 0 )
			flags |= DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE;
		// We only show thumbnails for image files - expect a call to
		// openDocumentThumbnail for each file that has
		// this flag set
		if (mimeType.startsWith("image/"))
			flags |= DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL;
		row.add(DocumentsContract.Document.COLUMN_FLAGS, flags);
		// COLUMN_SIZE is required, but can be null
		row.add(DocumentsContract.Document.COLUMN_SIZE, file.length());
		// These columns are optional
		row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified());
		// Document.COLUMN_ICON can be a resource id identifying a custom icon.
		// The system provides default icons
		// based on mime type
		// Document.COLUMN_SUMMARY is optional additional information about the
		// file
	}

	@Override
	public String getDocumentType(final String documentId) throws FileNotFoundException {
		File file = new File(documentId);
		if (file.isDirectory())
			return DocumentsContract.Document.MIME_TYPE_DIR;
		// From FileProvider.getType(Uri)
		final int lastDot = file.getName().lastIndexOf('.');
		if (lastDot >= 0) {
			final String extension = file.getName().substring(lastDot + 1);
			final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			if (mime != null) {
				return mime;
			}
		}
		return "application/octet-stream";
	}

	@Override
	public void deleteDocument(final String documentId) throws FileNotFoundException {
		new File(documentId).delete();
	}

	@Override
	public ParcelFileDescriptor openDocument(final String documentId, final String mode,
											final CancellationSignal signal) throws FileNotFoundException {
		File file = new File(documentId);
		final boolean isWrite = (mode.indexOf('w') != -1);
		if (isWrite) {
			return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
		} else {
			return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
		}
	}

	@Override
	public boolean onCreate() {
		return true;
	}
}