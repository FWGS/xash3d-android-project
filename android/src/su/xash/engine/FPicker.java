package su.xash.engine;
//Created by Solexid
import android.app.Activity;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ToggleButton;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import su.xash.fwgslib.FWGSLib;
import su.xash.engine.R;

public class FPicker extends Activity {
    private File currentDir;
    private FileArrayAdapter adapter;
	private boolean internal = false;
    static ListView delta;
    public static final int sdk = Integer.valueOf(Build.VERSION.SDK);
    static private Button mSelectBtn;
	static private ToggleButton mToggleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ( sdk >= 21 )
			super.setTheme( 0x01030224 );
		else super.setTheme( 0x01030005 );

		setContentView( R.layout.activity_fpicker );
		String path = getIntent().getStringExtra("path");
		if( path == null )
			path = Environment.getExternalStorageDirectory().toString();
		currentDir = new File( path );
		View.OnClickListener buttonListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				switch(v.getId())
				{
					case R.id.button_fpicker_select:
						onFileClick(v);
						break;
					case R.id.button_internal_storage:
						toggleInternalStorage(v);
						break;
				}
			}
		};
		mSelectBtn = ((Button)findViewById( R.id.button_fpicker_select ));
		mSelectBtn.setOnClickListener(buttonListener);
		mToggleBtn = ((ToggleButton)findViewById( R.id.button_internal_storage));
		mToggleBtn.setOnClickListener(buttonListener);
		internal = path.indexOf("/data") == 0;
		mToggleBtn.setChecked(internal);

		FWGSLib.changeButtonsStyle((ViewGroup)mSelectBtn.getParent());

		fill( currentDir, false );
    }
	private void toggleInternalStorage(View v)
	{
		internal = mToggleBtn.isChecked();
		if(internal)
			currentDir = getFilesDir();
		else
			currentDir = new File(Environment.getExternalStorageDirectory().toString());
		fill( currentDir, internal );
		if(internal)
			Toast.makeText(this, "Internal storage data selected\nuse file server to upload files", Toast.LENGTH_SHORT).show();
	}

    private void fill( File folder, boolean warnEmpty )
    {
		mSelectBtn.setEnabled( false );
		new Fill(folder, warnEmpty).execute();
    }
    
    private class Fill extends AsyncTask<Void, Void, List<Item>> 
    {
		File folder;
		boolean warnEmpty;
    
		public Fill( File f, boolean w )
		{
			folder = f;
			warnEmpty = w;
		}
		
		protected List<Item> doInBackground(Void... voids)
		{
			File[] dirs = folder.listFiles();
			List<Item> dir = new ArrayList<Item>();

			while( dirs == null )
			{
				String parent = folder.getParent();
				folder = new File( parent != null ? parent : Environment.getExternalStorageDirectory().toString() );
				dirs = folder.listFiles();
			}

			for( File ff: dirs )
			{
				Date lastModDate = new Date(ff.lastModified());
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if(ff.isDirectory())
				{
					boolean isXashDir = false;
					File[] fbuf = ff.listFiles();
					int buf = 0;
					
					if( fbuf != null && fbuf.length < 20 )
					{
						buf = fbuf.length;
						for (File valves: fbuf) 
						{
							if (valves.isDirectory() && valves.getName().contains("valve"))
									isXashDir=true;
						}
					}
					
					String num_item = getResources().getQuantityString(R.plurals.item_plurals, buf, buf);
					dir.add(new Item(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), isXashDir ? R.drawable.ic_launcher : R.drawable.folder ));
				}
            }
            
			Collections.sort(dir);

			if(folder.getPath().length() > 1)
				dir.add(0, new Item( "..", getString(R.string.parent_directory), "", folder.getParent(), R.drawable.folder));
			else internal = false;
				
			return dir;
		}
		
		protected void onPostExecute(List<Item> dir)
		{
			setTitle(getString(R.string.current_dir) + " " +folder.getName());
			
			adapter = new FileArrayAdapter(FPicker.this,R.layout.row,dir);
			delta = (ListView)findViewById(R.id.FileView);
			delta.setAdapter(adapter);
			delta.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent , View v, int position, long id) 
				{
					Item o = adapter.getItem(position);
					currentDir = new File(o.getPath());
					fill( currentDir, false );
				}
			});
			FPicker.mSelectBtn.setEnabled( true );
			if( warnEmpty && dir.size() == 1 )
				onFileClick(null);
		}
    }
	private void returnResult( boolean startServer )
	{
		Toast.makeText(this, getString(R.string.chosen_path) + " " + currentDir, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("GetPath",currentDir.toString());
		intent.putExtra( "startServer", startServer );
		setResult(RESULT_OK, intent);
		finish();
	}
    
	public void onFileClick(View v)
	{
		if( internal && !getIntent().getBooleanExtra( "dontWarnEmpty", false ))
		{
			final boolean fromButton = v != null;
			DialogInterface.OnClickListener startServerAsk = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick( DialogInterface dialog, int whichButton ) 
				{
					if( fromButton )
					{
						returnResult( whichButton == DialogInterface.BUTTON_POSITIVE );
					}
					else if( whichButton == DialogInterface.BUTTON_POSITIVE )
						returnResult( true );
				}
			};
			
			new AlertDialog.Builder( this )
				.setTitle( "Internal folder" )
				.setMessage( "Internal folder selected.\nStart file server to manage files now? You can start it with \"Manage files\" button later" )
				.setPositiveButton( "Yes", startServerAsk )
				.setNegativeButton( "Not now", startServerAsk )
				.setCancelable( true )
				.show();
			return;
		}
		returnResult( false );
	}
}

class FileArrayAdapter extends ArrayAdapter<Item> 
{
	private Context c;
	private int id;
	private List<Item>items;

	public FileArrayAdapter(Context context, int textViewResourceId, List<Item> objects) 
	{
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}
	
	public Item getItem(int i)
	{
		return items.get(i);
	}
    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) 
		{
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
        
		final Item finstance = items.get(position);
		if (finstance != null) 
		{
			TextView filename = (TextView) v.findViewById(R.id.filename);
			TextView fileitems = (TextView) v.findViewById(R.id.fileitems);
			TextView filedate = (TextView) v.findViewById(R.id.filedate);
			ImageView imageicon = (ImageView) v.findViewById(R.id.fd_Icon1);

			Drawable image = c.getResources().getDrawable(finstance.getImage());
			imageicon.setImageDrawable(image);

			if(filename!=null)
				filename.setText(finstance.getName());
			if(fileitems!=null)
				fileitems.setText(finstance.getData());
			if(filedate!=null)
				filedate.setText(finstance.getDate());
		}
		return v;
	}
}

class Item implements Comparable<Item>
{
	private String name;
	private String data;
	private String date;
	private String path;
	private int image;
	
	public Item(String n,String d, String dt, String p, int img)
	{
		name = n;
		data = d;
		date = dt;
		path = p;
		image = img;
	}
    
	public String getName()
	{
		return name;
	}
    
	public String getData()
	{
		return data;
    }
    
	public String getDate()
	{
		return date;
	}
	
	public String getPath()
	{
		return path;
	}
    
	public int getImage() 
	{
		return image;
	}
    
	public int compareTo(Item o) 
	{
		if(this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
		else
			throw new IllegalArgumentException();
	}
}
