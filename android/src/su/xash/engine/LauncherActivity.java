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
		View.OnClickListener buttonListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				switch(v.getId())
				{
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
		(( Button ) findViewById( R.id.button_select )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_launch )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_shortcut )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.button_about )).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.cmd_path_select)).setOnClickListener(buttonListener);
		(( Button ) findViewById( R.id.cmd_path_rw_select)).setOnClickListener(buttonListener);
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

		CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged( CompoundButton v, boolean isChecked )
			{
				switch( v.getId() )
				{
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
		if( !mPref.getBoolean("successfulRun",false) )
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

	public void aboutXash(View view)
	{
		final Activity a = this;
		this.runOnUiThread(new Runnable() 
		{
			public void run()
			{
				final Dialog dialog = new Dialog(a);
				dialog.setContentView(R.layout.about);
				dialog.setCancelable(true);
				dialog.show();
/*				if( sdk > 17 )
				{
					ImageView icon = (ImageView) dialog.findViewById(R.id.aboutIcon);
					icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
				}
*/
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
							Intent intent = new Intent(a, XashTutorialActivity.class);
							startActivity(intent);
						}
					});
				FWGSLib.changeButtonsStyle((ViewGroup)dialog.findViewById( R.id.show_firstrun ).getParent());

			}
		});
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
