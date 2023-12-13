package su.xash.engine;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import su.xash.engine.*;
import java.io.*;
import java.net.*;
import org.json.*;
import android.preference.*;
import su.xash.fwgslib.*;
import android.Manifest;
import java.util.Enumeration;
import java.util.ArrayList;

public class LauncherActivity extends Activity
{
	// public final static String ARGV = "su.xash.engine.MESSAGE";
	public final static int sdk = FWGSLib.sdk;
	public final static String UPDATE_LINK = "https://api.github.com/repos/FWGS/xash3d-fwgs/releases"; // releases/latest doesn't return prerelease and drafts
	static SharedPreferences mPref;
	
	static EditText cmdArgs, resPath, writePath, resScale, resWidth, resHeight, debuggerCommand;
	static ToggleButton useVolume, resizeWorkaround, useRoDir;
	static CheckBox	checkUpdates, immersiveMode, useRoDirAuto;
	static TextView tvResPath, resResult;
	static RadioButton radioScale, radioCustom;
	static RadioGroup scaleGroup;
	static CheckBox resolution, debugger, debuggerWait;
	static LinearLayout rodirSettings; // to easy show/hide
	
	static int mEngineWidth, mEngineHeight;
	final static int REQUEST_PERMISSIONS = 42;
	private View.OnClickListener buttonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) 
		{
			int id = v.getId();
			switch( id )
			{
				case R.id.button_share_fileserver:
					shareFileServer();
				break;
				case R.id.button_fileserver:
				case R.id.button_manage_files:
				case R.id.button_fileserver_rwdir:
					showFileServer( id == R.id.button_fileserver_rwdir );
				break;
				case R.id.cmd_path_rw_select:
					selectRwFolder(v);
				break;
				case R.id.button_select:
				case R.id.cmd_path_select:
					selectFolder(v);
				break;
				case R.id.button_launch:
					startXash(v);
				break;
				case R.id.button_shortcut:
					createShortcut(v);
				break;
				case R.id.button_about:
					aboutXash(v);
				break;
			}
		}
	};

	private CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged( CompoundButton v, boolean isChecked )
		{
			switch( v.getId() )
			{
				case R.id.toggle_file_server:
					toggleFileServer(v, isChecked);
				case R.id.resolution_custom_r:
					updateResolutionResult();
					toggleResolutionFields();
				break;
				case R.id.resolution:
					hideResolutionSettings( !isChecked );
				break;
				case R.id.debugger:
					hideDebuggerSettings( !isChecked );
				break;
				case R.id.use_rodir:
					hideRodirSettings( !isChecked );
				break;
				case R.id.use_rodir_auto:
					if( isChecked )
					{
						writePath.setText( FWGSLib.getExternalFilesDir( LauncherActivity.this ) );
					}
					writePath.setEnabled( !isChecked );
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//super.setTheme( 0x01030005 );
		if ( sdk >= 21 )
			super.setTheme( 0x01030224 );
		else super.setTheme( 0x01030005 );
				
		if( sdk >= 8 && CertCheck.dumbAntiPDALifeCheck( this ) )
		{
			finish();
			return;
		}
		
		setContentView(R.layout.activity_launcher);
		
/*		if( sdk > 17 )
		{
			ImageView icon = (ImageView) findViewById(R.id.launcherIcon);
			icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
		}
*/
		TabHost tabHost = (TabHost) findViewById(R.id.tabhost);

		tabHost.setup();
		
		TabHost.TabSpec tabSpec;
		tabSpec = tabHost.newTabSpec("tabtag1");
		tabSpec.setIndicator(getString(R.string.text_tab1));
		tabSpec.setContent(R.id.tab1);
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabtag2");
		tabSpec.setIndicator(getString(R.string.text_tab2));
		tabSpec.setContent(R.id.tab2);
		tabHost.addTab(tabSpec);
		if( sdk < 21 )
		{
			try
			{
				tabHost.invalidate();
				for(int i = 0; i < tabHost.getTabWidget().getChildCount(); i++)
				{
					tabHost.getTabWidget().getChildAt(i).getBackground().setAlpha(255);
					tabHost.getTabWidget().getChildAt(i).getLayoutParams().height = (int) (40 * getResources().getDisplayMetrics().density);
				}
			}
			catch(Exception e){}
		}
		

		mPref        = getSharedPreferences("engine", 0);
		cmdArgs      = (EditText) findViewById(R.id.cmdArgs);
		useVolume    = (ToggleButton) findViewById( R.id.useVolume );
		resPath      = (EditText) findViewById( R.id.cmd_path );
		checkUpdates = (CheckBox)findViewById( R.id.check_updates );
		//updateToBeta = (CheckBox)findViewById( R.id.check_betas );
		resizeWorkaround = (ToggleButton) findViewById( R.id.enableResizeWorkaround );
		tvResPath    = (TextView) findViewById( R.id.textView_path );
		immersiveMode = (CheckBox) findViewById( R.id.immersive_mode );
		resolution = (CheckBox) findViewById(R.id.resolution);
		resWidth = (EditText) findViewById(R.id.resolution_width);
		resHeight = (EditText) findViewById(R.id.resolution_height);
		resScale = (EditText) findViewById(R.id.resolution_scale);
		radioCustom = (RadioButton) findViewById(R.id.resolution_custom_r);
		radioScale = (RadioButton) findViewById(R.id.resolution_scale_r);
		scaleGroup = (RadioGroup) findViewById( R.id.scale_group );
		resResult = (TextView) findViewById( R.id.resolution_result );
		writePath = (EditText) findViewById( R.id.cmd_path_rw );
		useRoDir = (ToggleButton) findViewById( R.id.use_rodir );
		useRoDirAuto = (CheckBox) findViewById( R.id.use_rodir_auto );
		rodirSettings = (LinearLayout) findViewById( R.id.rodir_settings );
		debuggerCommand = (EditText) findViewById( R.id.debugger_command );
		debugger = (CheckBox) findViewById( R.id.debugger );
		debuggerWait = (CheckBox) findViewById( R.id.debugger_wait );

		Button selectFolderButton = ( Button ) findViewById( R.id.button_select );

		(( Button ) findViewById( R.id.button_select )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_launch )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_shortcut )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_about )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.cmd_path_select )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.cmd_path_rw_select )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_manage_files )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_fileserver )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_fileserver_rwdir )).setOnClickListener(buttonListener);
		useVolume.setChecked(mPref.getBoolean("usevolume",true));
		checkUpdates.setChecked(mPref.getBoolean("check_updates",true));
		//updateToBeta.setChecked(mPref.getBoolean("check_betas", false));
		updatePath(mPref.getString("basedir", FWGSLib.getDefaultXashPath() ) );
		cmdArgs.setText(mPref.getString("argv","-dev 3 -log"));
		resizeWorkaround.setChecked(mPref.getBoolean("enableResizeWorkaround", true));
		useRoDir.setChecked( mPref.getBoolean("use_rodir", false) );
		useRoDirAuto.setChecked( mPref.getBoolean("use_rodir_auto", true) );
		writePath.setText(mPref.getString("writedir", FWGSLib.getExternalFilesDir(this)));

		debugger.setChecked( mPref.getBoolean( "launch_gdb", false ));
		try {
			Class.forName( "su.xash.engine.DebugService" );
			debugger.setChecked( mPref.getBoolean( "launch_gdb", false ));
		} catch( ClassNotFoundException e ) {
			debugger.setChecked( false );
			debugger.setEnabled( false );
		}
		debuggerWait.setChecked( mPref.getBoolean( "gdb_wait", true ));
		debuggerCommand.setText( mPref.getString( "gdb_command", "" ));
		resolution.setChecked( mPref.getBoolean("resolution_fixed", false ) );
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		// Swap resolution here, because engine is always(should be always) run in landscape mode
		if( FWGSLib.isLandscapeOrientation( this ) )
		{
			mEngineWidth = metrics.widthPixels;
			mEngineHeight = metrics.heightPixels;
		}
		else
		{
			mEngineWidth = metrics.heightPixels;
			mEngineHeight = metrics.widthPixels;
		}		
		
		resWidth.setText(String.valueOf(mPref.getInt("resolution_width", mEngineWidth )));
		resHeight.setText(String.valueOf(mPref.getInt("resolution_height", mEngineHeight )));
		resScale.setText(String.valueOf(mPref.getFloat("resolution_scale", 2.0f)));
		
		resWidth.addTextChangedListener( resWidthTextChangeWatcher );
		resHeight.addTextChangedListener( resTextChangeWatcher );
		resScale.addTextChangedListener( resTextChangeWatcher );
		
		if( mPref.getBoolean("resolution_custom", false) )
			radioCustom.setChecked(true);
		else radioScale.setChecked(true);
		
		radioCustom.setOnCheckedChangeListener( checkListener );
		resolution.setOnCheckedChangeListener( checkListener );
		useRoDir.setOnCheckedChangeListener( checkListener );
		useRoDirAuto.setOnCheckedChangeListener( checkListener );
		debugger.setOnCheckedChangeListener( checkListener );
		
		if( sdk >= 19 )
		{
			immersiveMode.setChecked(mPref.getBoolean("immersive_mode", true));
		}
		else
		{
			immersiveMode.setVisibility(View.GONE); // not available
		}
		
		resPath.setOnFocusChangeListener( new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				updatePath( resPath.getText().toString() );
				
				// I know what I am doing, so don't ask me about folder!
				XashActivity.setFolderAsk( LauncherActivity.this, false );
			}
		} );
		


		// disable autoupdater for Google Play
		if( !XashConfig.GP_VERSION && mPref.getBoolean("check_updates", true))
		{
			new CheckUpdate(getBaseContext(),true, false).execute(UPDATE_LINK);
		}
		FWGSLib.changeButtonsStyle((ViewGroup)tabHost.getParent());
		// strange layout bug
		if(sdk < 11)
			((ViewGroup.MarginLayoutParams)((ScrollView)findViewById( R.id.scrollView2 )).getLayoutParams()).setMargins(10,10,-10,10);

		hideResolutionSettings( !resolution.isChecked() );
		hideRodirSettings( !useRoDir.isChecked() );
		hideDebuggerSettings( !debugger.isChecked() );
		updateResolutionResult();
		toggleResolutionFields();
		FWGSLib.cmp.applyPermissions( this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSIONS );
		if( getIntent().getBooleanExtra( "startServer", false ))
			showFileServer( false );
		else if( !mPref.getBoolean("successfulRun",false) )
			showFirstRun();
	}

	public void onRequestPermissionsResult( int requestCode,  String[] permissions,  int[] grantResults )
	{
		if( requestCode == REQUEST_PERMISSIONS ) 
		{
			if( grantResults[0] == PackageManager.PERMISSION_DENIED ) 
			{
				Toast.makeText( this, R.string.no_permissions, Toast.LENGTH_LONG ).show();
				finish();
			}
			else
			{
				// open again?
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		useRoDir.setChecked( mPref.getBoolean("use_rodir", false) );
		useRoDirAuto.setChecked( mPref.getBoolean("use_rodir_auto", true) );
		writePath.setText(mPref.getString("writedir", FWGSLib.getExternalFilesDir(this)));
		
		hideRodirSettings( !useRoDir.isChecked() );
	}

	void updatePath( String text )
	{
		tvResPath.setText(getString(R.string.text_res_path) + ":\n" + text );
		resPath.setText(text);
	}
	
	void hideResolutionSettings( boolean hide )
	{
		scaleGroup.setVisibility( hide ? View.GONE : View.VISIBLE );
	}
	
	void hideRodirSettings( boolean hide )
	{
		rodirSettings.setVisibility( hide ? View.GONE : View.VISIBLE );
	}
	String getDebuggerCommand( String saved )
	{
		if( saved.length() == 0 )
		{
			return "logcat&HOME=\""+getFilesDir().getPath()+"\" LD_LIBRARY_PATH=\""+FWGSLib.cmp.getNativeLibDir(this)+":$LD_LIBRARY_PATH\" FAKE_TTY=1 TERM=linux {GDB} {APP_PROCESS} -p {PID} 2>&1";
		}
		return saved;
	}

	void hideDebuggerSettings( boolean hide )
	{
		debuggerCommand.setVisibility( hide ? View.GONE : View.VISIBLE );
		debuggerWait.setVisibility( hide ? View.GONE : View.VISIBLE );
		if( !hide )
			debuggerCommand.setText( getDebuggerCommand( mPref.getString( "gdb_command", "" )));
		else if( debuggerCommand.getText().toString().length() != 0 )
		{
			SharedPreferences.Editor editor = mPref.edit();
			editor.putString( "gdb_command", getDebuggerCommand( debuggerCommand.getText().toString() ));
			editor.commit();
		}
	}
		
	TextWatcher resWidthTextChangeWatcher = new TextWatcher()
	{
		@Override
		public void afterTextChanged(Editable s){}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			int h = (int)((float)mEngineHeight / mEngineWidth * getCustomEngineWidth());
			resHeight.setText(String.valueOf(h));
			updateResolutionResult();
		}
	};

	TextWatcher resTextChangeWatcher = new TextWatcher()
	{
		@Override
		public void afterTextChanged(Editable s){}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after){}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			updateResolutionResult();
		}
	};
	
	void updateResolutionResult( )
	{
		int w, h;
		if( radioCustom.isChecked() )
		{
			w = getCustomEngineWidth();
			h = getCustomEngineHeight();

			// some fool-proof
			if( Math.abs((float)w/(float)h - 4.0/3.0) < 0.001 )
			{
				w = (int)((float)mEngineWidth / mEngineHeight * h+0.5);
				resWidth.setText(String.valueOf(w));
			}
		}
		else
		{
			final float scale = getResolutionScale();
			w = (int)((float)mEngineWidth / scale);
			h = (int)((float)mEngineHeight / scale);
		}
		
		resResult.setText( getString( R.string.resolution_result ) + w + "x" + h );
	}
	
	void toggleResolutionFields()
	{
		boolean isChecked = radioCustom.isChecked();
		resWidth.setEnabled( isChecked );
		resHeight.setEnabled( isChecked );
		resScale.setEnabled( !isChecked );
	}
	
	float getResolutionScale()
	{
		return FWGSLib.atof( resScale.getText().toString(), 1.0f );
	}
	
	int getCustomEngineHeight()
	{
		return FWGSLib.atoi( resHeight.getText().toString(), mEngineHeight );
	}
	
	int getCustomEngineWidth()
	{
		return FWGSLib.atoi( resWidth.getText().toString(), mEngineWidth );
	}
	
	public void startXash(View view)
	{
		Intent intent = new Intent(this, XashActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	
		SharedPreferences.Editor editor = mPref.edit();
		editor.putString("argv", cmdArgs.getText().toString());
		editor.putBoolean("usevolume",useVolume.isChecked());
		editor.putBoolean("use_rodir", useRoDir.isChecked() );
		editor.putBoolean("use_rodir_auto", useRoDirAuto.isChecked() );
		editor.putString("writedir", writePath.getText().toString());
		editor.putString("basedir", resPath.getText().toString());
		editor.putBoolean("enableResizeWorkaround",resizeWorkaround.isChecked());
		editor.putBoolean("check_updates", checkUpdates.isChecked());
		editor.putBoolean("resolution_fixed", resolution.isChecked());
		editor.putBoolean("resolution_custom", radioCustom.isChecked());
		editor.putFloat("resolution_scale", getResolutionScale() );
		editor.putInt("resolution_width", getCustomEngineWidth() );
		editor.putInt("resolution_height", getCustomEngineHeight() );
		editor.putString( "gdb_command", getDebuggerCommand( debuggerCommand.getText().toString() ));
		editor.putBoolean( "launch_gdb", debugger.isChecked() );
		editor.putBoolean( "gdb_wait", debuggerWait.isChecked() );
		
		if( sdk >= 19 )
			editor.putBoolean("immersive_mode", immersiveMode.isChecked());
		else
			editor.putBoolean("immersive_mode", false); // just in case...
		editor.commit();
		startActivity(intent);
	}
	private Dialog fileServerDialog;
	private TextView ipInfo;
	private ToggleButton fileServerToggle;
	private int serverPort;
	private String serverPath;
	private java.lang.Process serverProcess;
	private void toggleFileServer(View v, boolean checked)
	{
		fileServerDialog.setCancelable(!checked);
		fileServerDialog.findViewById( R.id.button_share_fileserver ).setEnabled( checked );
		if( checked )
		{
			serverPort = Integer.valueOf(((EditText) fileServerDialog.findViewById( R.id.fileserver_port )).getText().toString());
			mPref.edit().putInt( "fileServerPort", serverPort ).commit();
			try
			{
				String execFallback = FWGSLib.execFallback( this, FWGSLib.cmp.getNativeLibDir( this ) + "/libfileserver.so" );
				serverProcess = Runtime.getRuntime().exec(new String[]{execFallback, serverPath, String.valueOf( serverPort )});
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				if( serverProcess != null )
					serverProcess.destroy();
				serverProcess = null;
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		UpdateAddresses();
	} 

	private ArrayList<Inet4Address> addresses;
	private void UpdateAddresses()
	{
		if(addresses == null)
			return;
		String text = "";
		for(Inet4Address addr : addresses)
		{
			if( !fileServerToggle.isChecked() )
				text += addr.getHostAddress() + '\n';
			else
				text += "http://" + addr.getHostAddress() +':'+ serverPort + "/\n";
		}
		ipInfo.setText(text);
	}
	private void shareFileServer()
	{
		String text = "";
		for(Inet4Address addr : addresses)
			text += "http://" + addr.getHostAddress() +':'+ serverPort + "/\n";
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(shareIntent, "Send this somewhere you can read from PC"));
	}
	
	private int runPing(String args, byte[] outaddr)
	{
		try{
			byte[] data = new byte[255];
			java.lang.Process p = Runtime.getRuntime().exec(new String[]{"/system/bin/sh","-c","ping "+args+" 2>&1"});
			Log.d("ping", args);
			InputStream out = p.getInputStream();
			out.read(data);
			String output = new String(data);
			int i = output.indexOf( "\nFrom " );
			if( i >= 0 )
			{
				String addr = output.substring( i + 6 );
				int i1 = addr.indexOf( ':' );
				i = addr.indexOf( ' ' );
				if( i1 > 0 && i1 < i )
					i = i1;
				String[] spl = addr.substring( 0, i ).split("\\.");
				//Log.d("ping", "err " + spl + "" + addr);
				outaddr[0] = (byte)(int)Integer.valueOf(spl[0]);
				outaddr[1] = (byte)(int)Integer.valueOf(spl[1]);
				outaddr[2] = (byte)(int)Integer.valueOf(spl[2]);
				outaddr[3] = (byte)(int)Integer.valueOf(spl[3]);
				
				i = 1; // unknown
				if( addr.indexOf("exceeded") > 0 ) // TTL
					i = 2;
				else if( addr.indexOf("Unreachable") > 0 ) // Destination Host Unreachable
					i = 3;
				Log.d("ping", "err:"+i);
				return i;
			}
			else {
				i = output.indexOf( "\n64 bytes from " );
				String addr = output.substring( i + 15 );
				addr = addr.substring( 0, addr.indexOf(':') );
				String[] spl = addr.split("\\.");
				Log.d("ping", "ok " + spl + "" + addr);
				outaddr[0] = (byte)(int)Integer.valueOf(spl[0]);
				outaddr[1] = (byte)(int)Integer.valueOf(spl[1]);
				outaddr[2] = (byte)(int)Integer.valueOf(spl[2]);
				outaddr[3] = (byte)(int)Integer.valueOf(spl[3]);
				
				return 0; // OK
			}
			//Log.e("ping", output);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}
	private void addAddrCallback(byte[] addr)
	{
		try {
			Inet4Address a = (Inet4Address)InetAddress.getByAddress(addr);
			if(addresses.contains(a))
				return;
			addresses.add(a);
		}
		catch(UnknownHostException e){} // why the fuck Inet4Address does not have constructor only allowing to construct it with impossible exceptions???
		runOnUiThread(new Runnable(){
			@Override
					public void run()
			{
				UpdateAddresses();
			}
		});
	}

	// ping-based discovery, in case android does not give addresses for some paranoid-schizosecurity reason
	private Runnable pingWorker = new Runnable() {
		@Override
		public void run()
		{
			byte[] addr = new byte[4];

			// first, check some default android addresses if we are using tethering
			// wifi tethering
			if( runPing( "-c 1 -w 1 -t 1 192.168.43.1", addr ) == 0 )
			{
				addAddrCallback(addr);
			}
			// usb tethering
			if( runPing( "-c 1 -w 1 -t 1 192.168.42.129", addr ) == 0 )
			{
				addAddrCallback(addr);
			}
			try{
			Thread.sleep(500);
			}catch(Exception e){}
			// p2p
			if( runPing( "-c 1 -w 1 -t 1 192.168.49.254", addr ) == 3 && runPing( "-c 1 -w 1 -t 1 192.168.49.1", addr ) == 0 )
			{
				addAddrCallback(addr);
			}
			try{
				Thread.sleep(1000);
			}catch(Exception e){}
			// try get gateway (should get TTL error with GW address);
			if( runPing( "-c 1 -w 1 -t 1 255.255.255.254", addr ) == 2 )
			{
				byte[] o2 = new byte[4];
				Log.d( "ping", "" + ((int)addr[0] & 0xFF) + "." + ((int)addr[1] & 0xFF) +"." + ((int)addr[2] & 0xFF) + "." + ((int)addr[3] & 0xFF ));
				for(int b = 1; b < 10; b++ )
				{
					int r = runPing( "-c 1 -w 3 -t 1 " + ((int)addr[0] & 0xFF) + "." + ((int)addr[1] & 0xFF) +"." + ((int)addr[2] & 0xFF) + "." + b, o2);
					if( r != 3 )
						r = runPing( "-c 1 -w 3 -t 1 " + ((int)addr[0] & 0xFF) + "." + ((int)addr[1] & 0xFF) +"." + ((int)addr[2] & 0xFF) + "." + (255 - b), o2);
					if( r != 3 )
						r = runPing( "-c 1 -w 3 -t 1 " + ((int)addr[0] & 0xFF) + "." + ((int)addr[1] & 0xFF) +"." + ((int)addr[2] & 0xFF) + "." +((int)addr[3] & 0xFF+b), o2);
					// get first unreachable source address (need more time sometimes)
					if( r == 3 )
					{
						// should successfully ping it now
						if( runPing( "-c 1 -w 1 -t 1 " + ((int)o2[0] & 0xFF) + "." + ((int)o2[1] & 0xFF) +"." + ((int)o2[2] & 0xFF) + "." + ((int)o2[3] & 0xFF), o2) == 0 )
						{
							addAddrCallback(o2);
						}
						break;
					}
				}
			}
		}
	};
	private void showFileServer( boolean rwdir )
	{
		fileServerDialog = new Dialog(this);
		fileServerDialog.setContentView(R.layout.fileserver);
		//dialog.setCancelable(false);
		fileServerDialog.show();
		fileServerToggle = (ToggleButton) fileServerDialog.findViewById( R.id.toggle_file_server );
		fileServerToggle.setOnCheckedChangeListener( checkListener );
		FWGSLib.changeButtonsStyle((ViewGroup)fileServerToggle.getParent());
		View share = fileServerDialog.findViewById( R.id.button_share_fileserver );
		share.setOnClickListener( buttonListener );
		share.setEnabled( false );
		ipInfo = (TextView) fileServerDialog.findViewById( R.id.fileserver_info );
		((EditText) fileServerDialog.findViewById( R.id.fileserver_port )).setText(String.valueOf(mPref.getInt("fileServerPort", 8080)));
		serverPath = rwdir ? writePath.getText().toString() : resPath.getText().toString();
		Log.d( "serverPath", serverPath + " " + rwdir );
		addresses = new ArrayList<Inet4Address>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				String name = intf.getName();
				// ignore dummy and mobile interfaces, we do not need public or nat addresses
				if( name.indexOf("dummy") >= 0 || name.indexOf("rmnet") >= 0 || name.indexOf("ccmni") >= 0 || name.indexOf("pdp") >= 0 )
					continue;
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						addresses.add((Inet4Address)inetAddress);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		UpdateAddresses();
		new Thread(pingWorker).start();
	}

	public void aboutXash(View view)
	{
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.about);
		dialog.setCancelable(true);
		dialog.show();
		/*if( sdk > 17 )
		{
			ImageView icon = (ImageView) dialog.findViewById(R.id.aboutIcon);
			icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
		}*/

		TextView tView6 = (TextView) dialog.findViewById(R.id.textView6);
		tView6.setMovementMethod(LinkMovementMethod.getInstance());
		((Button)dialog.findViewById( R.id.button_about_ok )).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		((Button)dialog.findViewById( R.id.show_firstrun )).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					dialog.cancel();
					Intent intent = new Intent(LauncherActivity.this, XashTutorialActivity.class);
					startActivity(intent);
				}
			});
		FWGSLib.changeButtonsStyle((ViewGroup)dialog.findViewById( R.id.show_firstrun ).getParent());
	}

	int m_iFirstRunCounter = 0;
	public void showFirstRun()
	{
		startActivity(new Intent(this, su.xash.engine.XashTutorialActivity.class));
	}

	public static final int ID_SELECT_FOLDER = 42, ID_SELECT_RW_FOLDER = 43;

	public void selectFolder(View view)
	{
		Intent intent = new Intent(this, su.xash.engine.FPicker.class);
		intent.putExtra("path",resPath.getText().toString());
		startActivityForResult(intent, ID_SELECT_FOLDER);
		resPath.setEnabled(false);
		XashActivity.setFolderAsk( this, false );
	}
	
	public void selectRwFolder(View view)
	{
		Intent intent = new Intent(this, su.xash.engine.FPicker.class);
		intent.putExtra("dontWarnEmpty", true );
		startActivityForResult(intent, ID_SELECT_RW_FOLDER);
		writePath.setEnabled(false);
		XashActivity.setFolderAsk( this, false );
	}


	public void onActivityResult(int requestCode, int resultCode, Intent resultData) 
	{
		switch(requestCode)
		{
		case ID_SELECT_FOLDER:
		{
			if (resultCode == RESULT_OK) 
			{
				try	
				{
					if( resPath == null )
						return;
					updatePath(resultData.getStringExtra("GetPath"));
					resPath.setEnabled( true );
					if( resultData.getBooleanExtra("startServer", false ))
						showFileServer( false );
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			resPath.setEnabled(true);
			break;
		}
		case ID_SELECT_RW_FOLDER:
		{
			if (resultCode == RESULT_OK) 
			{
				try	
				{
					if( writePath == null )
						return;
					writePath.setText(resultData.getStringExtra("GetPath"));
					writePath.setEnabled( true );
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			writePath.setEnabled(true);
			break;
		}
		}
	}

	public void createShortcut(View view)
	{
		Intent intent = new Intent(this, ShortcutActivity.class);
		intent.putExtra( "basedir", resPath.getText().toString() );
		intent.putExtra( "name", "Xash3D FWGS" );
		intent.putExtra( "argv", cmdArgs.getText().toString() );
		startActivity(intent);
	}
}
