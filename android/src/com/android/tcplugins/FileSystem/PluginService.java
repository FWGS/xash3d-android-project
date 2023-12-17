package com.android.tcplugins.FileSystem;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.os.RemoteException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.graphics.Bitmap;
import java.util.List;
import java.util.ArrayList;

class PluginFunctions extends IPluginFunctions.Stub {

	private static final String TAG = "XASH3D.PluginFunctions";
	private IRemoteDialogCallback mDialogCallback = null;
	private IRemoteProgressCallback mProgressCallback = null;
	private PluginService service = null;

    private static final int FS_COPYFLAGS_EXISTS_DIFFERENTCASE = 16;
    private static final int FS_COPYFLAGS_EXISTS_SAMECASE = 8;
    private static final int FS_COPYFLAGS_MOVE = 1;
    private static final int FS_COPYFLAGS_OVERWRITE = 2;
    private static final int FS_COPYFLAGS_RESUME = 4;
    public static final int FS_DELETE = 64;
    public static final int FS_EXECUTE = 32;
    private static final int FS_EXEC_ERROR = 1;
    private static final int FS_EXEC_OK = 0;
    private static final int FS_EXEC_SYMLINK = -2;
    private static final int FS_EXEC_YOURSELF = -1;
    private static final int FS_FILE_EXISTS = 1;
    private static final int FS_FILE_EXISTSRESUMEALLOWED = 7;
    private static final int FS_FILE_NOTFOUND = 2;
    private static final int FS_FILE_NOTSUPPORTED = 6;
    private static final int FS_FILE_OK = 0;
    private static final int FS_FILE_READERROR = 3;
    private static final int FS_FILE_USERABORT = 5;
    private static final int FS_FILE_WRITEERROR = 4;
    public static final int FS_GET_BITMAP = 256;
    public static final int FS_GET_COPY_FILE = 4;
    public static final int FS_GET_LOCAL_NAME = 16;
    public static final int FS_GET_MOVE_FILE = 8;
    public static final int FS_HIDE_TRANSFER_MODE = 4096;
    public static final int FS_MAKEDIR = 128;
    public static final int FS_PUT_COPY_FILE = 1;
    public static final int FS_PUT_MOVE_FILE = 2;
    public static final int FS_REMOTE_COPY = 1024;
    public static final int FS_REMOTE_SOURCE = 8192;
    public static final int FS_REMOTE_TARGET = 16384;
    public static final int FS_RENAME = 512;
    public static final int FS_STATUS_INFO_NEEDED = 2048;
    public static final int FS_STATUS_LANGUAGE_CHANGED = 4096;
    public static final int FS_STREAM = 2048;

	public String getLocalFileName( String str ) throws RemoteException {
		Log.i( TAG, "getLocal" + str);
		return null;
	}

	public String getModifiedLocalName( String str, String str2 ) throws RemoteException {
		Log.i( TAG, "getModLocal" + str + " " + str2 );
		return null;
	}

	public int getSupportedFunctions() throws RemoteException {
		return FS_MAKEDIR | FS_PUT_COPY_FILE | FS_GET_COPY_FILE | FS_PUT_MOVE_FILE | FS_GET_MOVE_FILE | FS_GET_LOCAL_NAME | FS_STATUS_INFO_NEEDED  | FS_RENAME | FS_REMOTE_TARGET | FS_REMOTE_SOURCE | FS_REMOTE_COPY | FS_DELETE;// | FS_EXECUTE | FS_GET_BITMAP;
	}

	public PluginFunctions( PluginService pluginService ) {
		this.service = pluginService;
	}

	public boolean deleteFile( String str ) throws RemoteException {
		Log.i( TAG, "del" + str);
		try{
			File f = new File( internalFileName( str ));
			f.delete();
		}
		catch( Exception e )
		{
			return false;
		}
		return true;
	}

	public boolean removeDir( String str ) throws RemoteException {
		Log.i( TAG, "rmdir" + str);
		try{
			File f = new File(internalFileName(str));
			f.delete();
		}
		catch( Exception e )
		{
			return false;
		}
		return true;
	}

	public boolean makeDir( String str ) throws RemoteException {
		Log.i( TAG, "mkdir" + str);
		try{
			File f = new File( internalFileName( str ));
			f.mkdirs();
		}
		catch( Exception e )
		{
			return false;
		}
		return true;
	}

	public Bitmap getBitmap( String str ) throws RemoteException {
		return null;
	}




	public List<PluginItem> getDirectoryList( String str ) throws RemoteException {
		
		Log.i(TAG, "dirList " + str);
		File folder = new File( internalFileName( str ));
		File[] files = folder.listFiles();
		ArrayList arrayList = new ArrayList(files.length);
		for( File ff: files )
		{
			PluginItem pluginItem = new PluginItem();
			pluginItem.name = ff.getName();
			pluginItem.description = null;
			pluginItem.directory = ff.isDirectory();
			pluginItem.lastModified = ff.lastModified();
			pluginItem.length = ff.length();
			//pluginItem.iconFlags = 1;
			arrayList.add(pluginItem);
		}
		return arrayList;
	}

	public int execute( String[] strArr, String str ) throws RemoteException {
		Log.i(TAG, "execute " + str);
		return 0;
	}

	private String internalFileName(String p)
	{
		return "/data/data/su.xash.engine/files" + p;
	}

	public int putFile(String p1, String p2, int copyflags) throws RemoteException {
		Log.i(TAG, "putFile " + p1 + " " + p2 + " " + copyflags);
		File file = new File(p1);
		if (!file.isFile()) {
			return FS_FILE_NOTFOUND;
		}
		if ((copyflags & FS_COPYFLAGS_OVERWRITE) == 0 && (copyflags & (FS_COPYFLAGS_EXISTS_DIFFERENTCASE|FS_COPYFLAGS_EXISTS_SAMECASE)) != 0) {
			return FS_FILE_EXISTS;
		}
		try{
			OutputStream os = new FileOutputStream(internalFileName(p2));
			InputStream is = new FileInputStream(file);
			byte[] bArr = new byte[4096];
			for (int read = is.read(bArr); read > 0; read = is.read(bArr)) {
				os.write(bArr, 0, read);
			}
			return FS_FILE_OK;
		}
		catch(Exception e)
		{
			return FS_FILE_WRITEERROR;
		}
	}

	public int getFile( String p, String[] outputs, int x, long y, long z ) throws RemoteException {
		Log.i(TAG, "getFile " + p + " " + x + " " + y + " " + z);
		File file = new File(internalFileName(p));
		if (!file.isFile()) {
			return FS_FILE_NOTFOUND;
		}
		try{
			OutputStream os = new FileOutputStream(outputs[0]);
			InputStream is = new FileInputStream(file);
			byte[] bArr = new byte[4096];
			for (int read = is.read(bArr); read > 0; read = is.read(bArr)) {
				os.write(bArr, 0, read);
			}
			return FS_FILE_OK;
		}
		catch(Exception e)
		{
			return FS_FILE_READERROR;
		}
	}

	public int renMovFile( String p1, String p2, boolean f1, boolean f2, long date1, long date2 ) throws RemoteException {
		Log.i( TAG, "renmove" + p1 + " " + f1 + " " + p2 + " " + f2);
		try{
			File fl1 = new File(internalFileName(p1));
			File fl2 = new File(internalFileName(p2));
			fl1.renameTo(fl2);
			return FS_FILE_OK;
		}
		catch(Exception e)
		{
			return FS_FILE_NOTFOUND;
		}
	}

	public String disconnect( String reason ) throws RemoteException {
		Log.i( TAG, "disconnect: " + reason );
		return null;
	}

	public void statusInfo( String path, int startEnd, int op ) throws RemoteException {
		Log.i( TAG, "status: " + path + " - startEnd=" + startEnd + " op=" + op );
	}

	// is this even work??? No documentaion, no info!!!
	// it is declared in features, but seems to be mever caled!!!
	public void registerCallbacks( IRemoteProgressCallback iRemoteProgressCallback, IRemoteDialogCallback iRemoteDialogCallback ) throws RemoteException {
		Log.i( TAG, "regCallbacks" );
		this.mProgressCallback = iRemoteProgressCallback;
		this.mDialogCallback = iRemoteDialogCallback;
	}
	public int putFileFromCallback( IRemoteCopyCallback iRemoteCopyCallback, String path, int x, long y, long z ) throws RemoteException {
		Log.i( TAG, "putFileFromCallback " + path );
		throw new RemoteException();
	}
	public IRemoteCopyCallback getFileCallback( String path ) throws RemoteException {
		Log.i( TAG, "getFileCallback " + path );
		throw new RemoteException();
	}
	public void setAbortFlag( String str, boolean f ) throws RemoteException {
		Log.i( TAG, "abortFlag " + f );
	}

}


public class PluginService extends Service {
    private static final String TAG = "XASH3D.PluginService";

    PluginFunctions pluginFunctions = null;

    /* access modifiers changed from: private */
    public void displayResult(String str) {
    }

    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");

        if (this.pluginFunctions == null) {
            Log.i(TAG, "onBind");
            this.pluginFunctions = new PluginFunctions(this);
        }
        return this.pluginFunctions;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        Log.i(TAG, "onStartCommand");
        return 1;
    }

    public void onCreate() {
        Log.i(TAG, "onCreate");
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy");
    }
}
