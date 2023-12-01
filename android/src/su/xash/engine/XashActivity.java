package su.xash.engine;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.*;

import android.app.*;
import android.content.*;
import android.view.*;
import android.os.*;
import android.util.*;
import android.graphics.*;
import android.text.method.*;
import android.text.*;
import android.media.*;
import android.hardware.*;
import android.content.*;
import android.widget.*;
import android.content.pm.*;
import android.net.Uri;
import android.provider.*;
import android.database.*;

import android.view.inputmethod.*;

import java.lang.*;
import java.util.*;
import java.security.MessageDigest;

import su.xash.engine.R;
import su.xash.engine.XashConfig;
import su.xash.engine.XashInput;
import android.provider.Settings.Secure;
import android.Manifest;

import su.xash.fwgslib.*;
import android.sax.*;

/**
 Xash Activity
 */
public class XashActivity extends Activity {

	// Main components
	protected static XashActivity mSingleton;
	protected static View mTextEdit;
	protected static ViewGroup mLayout;
	
	private static boolean mUseRoDir;
	private static String mWriteDir;
	
	public static EngineSurface mSurface;
	public static String mArgv[];
	public static final int sdk = Integer.valueOf( Build.VERSION.SDK );
	public static final String TAG = "XASH3D:XashActivity";
	public static XashInput.JoystickHandler handler;
	public static boolean keyboardVisible = false;
	public static boolean mEngineReady = false;
	public static boolean mEnginePaused = false;
	public static Vibrator mVibrator;
	public static boolean fMouseShown = true;
	public static boolean fGDBSafe = false;
	public static float mScale = 0.0f, mTouchScaleX = 1.0f, mTouchScaleY = 1.0f;
	public static int mForceHeight = 0, mForceWidth = 0;
	public static int mMinHeight = 240, mMinWidth = 320; // hl1 support 320 width, but mods may not.
	public static boolean bIsCstrike = false;

	public static boolean mHasVibrator;
	private int mReturingWithResultCode = 0;
	
	private static int FPICKER_RESULT = 2;
	

	// Joystick constants
	public final static byte JOY_HAT_CENTERED = 0; // bitmasks for hat current status
	public final static byte JOY_HAT_UP       = 1;
	public final static byte JOY_HAT_RIGHT    = 2;
	public final static byte JOY_HAT_DOWN     = 4;
	public final static byte JOY_HAT_LEFT     = 8;

	public final static byte JOY_AXIS_SIDE  = 0; // this represents default
	public final static byte JOY_AXIS_FWD   = 1; // engine binding: SFPYRL
	public final static byte JOY_AXIS_PITCH = 2;
	public final static byte JOY_AXIS_YAW   = 3;
	public final static byte JOY_AXIS_RT    = 4;
	public final static byte JOY_AXIS_LT    = 5;
	
	final static int REQUEST_PERMISSIONS = 42;

	// Preferences
	public static SharedPreferences mPref = null;
	private static boolean mUseVolume;
	public static View mDecorView;
	

	// Load the .so
	static 
	{
		System.loadLibrary( "xash" );
	}

	void configureActivity()
	{
		// fullscreen
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		// this value was reused in 19+, so try get it in runtime
		int FLAG_NEEDS_MENU_KEY = 0;
		try
		{
			FLAG_NEEDS_MENU_KEY = WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null);
		}
		catch(Exception e){}
		
		int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | 
			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | FLAG_NEEDS_MENU_KEY;
		getWindow().setFlags( flags, flags );

		FWGSLib.enableNavbarMenu(this);
		// landscapeSensor is not supported until API9
		if( sdk < 9 )
			setRequestedOrientation( 0 );

		if( XashActivity.mPref.getBoolean( "immersive_mode", false ) )
			mDecorView = getWindow().getDecorView();
	}

	void setupStorage()
	{
		mUseRoDir = mPref.getBoolean("use_rodir", false);
		mWriteDir = mPref.getString("writedir", FWGSLib.getExternalFilesDir( this ));
		
		FWGSLib.cmp.applyPermissions( this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSIONS );
		
		// just in case
		if( mWriteDir.length() == 0 )
		{
			mWriteDir = FWGSLib.getExternalFilesDir( this );
		}
		
		if( mPref.getBoolean( "folderask", true ) )
		{
			Log.v( TAG, "folderask == true. Opening FPicker..." );
		
			Intent intent = new Intent( this, su.xash.engine.FPicker.class );
			startActivityForResult( intent, FPICKER_RESULT );
		}
		else
		{
			Log.v( TAG, "folderask == false. Checking write permission..." );

			// check write permission and run engine, if possible
			String basedir = FWGSLib.getStringExtraFromIntent( getIntent(), "basedir", mPref.getString( "basedir", "/sdcard/xash/" ) );
			checkWritePermission( basedir );
		}
	}

	// Setup
	@Override
	protected void onCreate( Bundle savedInstanceState ) 
	{
		Log.v( TAG, "onCreate()" );
		super.onCreate( savedInstanceState );

		mEngineReady = false;
		
		if( sdk >= 8 && CertCheck.dumbAntiPDALifeCheck( this ) )
		{
			finish();
			return;
		}

		// So we can call stuff from static callbacks
		mSingleton = this;
		mPref = this.getSharedPreferences( "engine", 0 );

		configureActivity();
		setupStorage();
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
	public void onActivityResult( int requestCode, int resultCode, Intent resultData ) 
	{
		if( resultCode != RESULT_OK )
		{
			Log.v( TAG, "onActivityResult: result is not OK. ReqCode: " + requestCode + ". ResCode: " + resultCode );
		}
		else
		{
			// it's not possible to create dialogs here
			// so most work will be done after Activity resuming, in onPostResume()
			mReturingWithResultCode = requestCode;
			if( requestCode == FPICKER_RESULT )
			{
				String newBaseDir = resultData.getStringExtra( "GetPath" );
				setNewBasedir( newBaseDir );
				setFolderAsk( this, false ); // don't ask on next run
				Log.v( TAG, "Got new basedir from FPicker: " + newBaseDir );
			}
		}
	}
	
	@Override
	public void onPostResume()
	{
		super.onPostResume();
		
		if( mReturingWithResultCode != 0 )
		{
			if( mReturingWithResultCode == FPICKER_RESULT )
			{
				String basedir = mPref.getString( "basedir", "/sdcard/xash/" );
				checkWritePermission( basedir );
			}
			
			mReturingWithResultCode = 0;
		}
	}
	
		// Events
	@Override
	protected void onPause() {
		Log.v( TAG, "onPause()" );
		
		if( mEngineReady )
		{
			// let engine save all configs before exiting.
			XashBinding.nativeOnPause();
		
			// wait until Xash will save all configs
			mSurface.engineThreadWait();
		}
		
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.v( TAG, "onResume()" );
		
		if( mEngineReady )
		{
			XashBinding.nativeOnResume();
		}
		
		mEnginePaused = false;

		super.onResume();
	}
	
	@Override
	protected void onStop() {
		Log.v( TAG, "onStop()" );
		/*if( mEngineReady )
		{
			nativeSetPause(0);
			// let engine properly exit, instead of killing it's thread
			nativeOnStop();
		
			// wait for engine
			mSurface.engineThreadWait();
		}*/
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Log.v( TAG, "onDestroy()" );
		
		if( mEngineReady )
		{
			XashBinding.nativeUnPause();
			
			// let engine a chance to properly exit
			XashBinding.nativeOnDestroy();
			
			//mSurface.engineThreadWait();
			
			// wait until Xash will exit
			mSurface.engineThreadJoin();
		}
		
		super.onDestroy();
	}
	
	@Override
	public void onWindowFocusChanged( boolean hasFocus ) 
	{
		if( mEngineReady )
		{
			XashBinding.nativeOnFocusChange();
		}
		
		super.onWindowFocusChanged( hasFocus );

		FWGSLib.cmp.applyImmersiveMode(keyboardVisible, mDecorView);
	}
	
	public static void setFolderAsk( Context ctx, Boolean b )
	{
		SharedPreferences pref = ctx.getSharedPreferences( "engine", 0 );
		
		if( pref.getBoolean( "folderask", true ) == b )
			return;
	
		SharedPreferences.Editor editor = pref.edit();
		
		editor.putBoolean( "folderask", b );
		editor.commit();
	}
	
	private void setNewBasedir( String baseDir )
	{
		SharedPreferences.Editor editor = mPref.edit();
		
		editor.putString( "basedir", baseDir );
		editor.commit();
	}
	
	
	private DialogInterface.OnClickListener folderAskEnable = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick( DialogInterface dialog, int whichButton ) 
		{
			XashActivity act = XashActivity.this;
			act.setFolderAsk( XashActivity.this, true );
			act.finish();
		}
	};
	
	private void checkWritePermission( String basedir )
	{
		Log.v( TAG, "Checking write permissions..." );
		
		String testDir = mUseRoDir ? mWriteDir : basedir;

		if( XashBinding.nativeTestWritePermission( testDir ) == 0 )
		{
			Log.v( TAG, "First check has failed!" );
			
			String msg = null;
			
			if( sdk > 20 )
			{
				// 5.0 and higher _allows_ writing to SD card, but have broken fopen()/open() call. So, no Xash here. F*ck you, Google!
				msg = getString( R.string.lollipop_write_fail_msg );
			}
			else if( sdk > 18 )
			{
				// 4.4 and 4.4W does not allow SD card write at all
				msg = getString( R.string.kitkat_write_fail_msg );
			}
			else
			{
				// Read-only filesystem
				// Logically should be never reached
				msg = getString( R.string.readonly_fs_fail_msg );
			}
			
			new AlertDialog.Builder( this )
				.setTitle( R.string.write_failed )
				.setMessage( msg )
				.setPositiveButton( R.string.ok, folderAskEnable )
				.setNegativeButton( R.string.convert_to_rodir, new DialogInterface.OnClickListener()
					{
						public void onClick( DialogInterface dialog, int whichButton )
						{
							XashActivity.this.convertToRodir();
						}
					})
				.setCancelable( false )
				.show();
		}
		else
		{
			// everything is normal, so launch engine
			launchSurfaceAndEngine();
		}
	}
	
	private void convertToRodir()
	{
		mWriteDir = FWGSLib.getExternalFilesDir(this);

		new AlertDialog.Builder( this )
			.setTitle( R.string.convert_to_rodir )
			.setMessage( String.format( getString( R.string.rodir_warning, mWriteDir ) ) )
			.setNegativeButton( R.string.cancel, folderAskEnable )
			.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick( DialogInterface dialog, int whichButton )
				{
					XashActivity.mUseRoDir = true;
														
					SharedPreferences.Editor editor = XashActivity.this.mPref.edit();
					editor.putBoolean("use_rodir", XashActivity.mUseRoDir);
					editor.putString("writedir", XashActivity.mWriteDir);
					editor.commit();
					
					XashActivity.this.launchSurfaceAndEngine();
				}
			})
			.setCancelable( false )
			.show();		
	}

	private void launchSurfaceAndEngine()
	{
		Log.v( TAG, "Everything is OK. Launching engine..." );

		if( !setupEnvironment() )
		{
			finish();
			return;
		}
		InstallReceiver.extractPAK( this, false );

		// Set up the surface
		mSurface = new EngineSurface( getApplication() );

		mLayout = new FrameLayout( this );
		mLayout.addView( mSurface );
		setContentView( mLayout );

		SurfaceHolder holder = mSurface.getHolder();
		holder.setType( SurfaceHolder.SURFACE_TYPE_GPU );
		handler = XashInput.getJoystickHandler();

		mVibrator = ( Vibrator )getSystemService( Context.VIBRATOR_SERVICE );
		mHasVibrator =  handler.hasVibrator() && (mVibrator != null);

		mUseVolume = mPref.getBoolean( "usevolume", false );
		if( mPref.getBoolean( "enableResizeWorkaround", true ) )
		{
			new AndroidBug5497Workaround(this)
			{
				@Override
				public void onKeyboardToggle(boolean visible, int heightUsable, int heightDiff)
				{
					XashActivity.keyboardVisible = visible;
					FWGSLib.cmp.applyImmersiveMode( XashActivity.keyboardVisible, XashActivity.mDecorView );
					FWGSLib.enableNavbarMenu(XashActivity.mSingleton);
					if( mSurface.mResizeEnabled )
					{
						mSurface.getHolder().setSizeFromLayout();
						mSurface.setSurfaceSize( mSurface.getHolder(), mSurface.getWidth(), heightUsable + heightDiff/2 );
					}
				}
			};

		}

		if( mPref.getBoolean( "resolution_fixed", false ) )
		{
			if( mPref.getBoolean( "resolution_custom", false ) )
			{
				mForceWidth = mPref.getInt( "resolution_width", 854 );
				mForceHeight = mPref.getInt( "resolution_height", 480 );
				if( mForceWidth < mMinWidth || mForceHeight < mMinHeight )
					mForceWidth = mForceHeight = 0;
			}
			else
			{
				mScale = mPref.getFloat( "resolution_scale", 1 );
				if( mScale < 0.5 )
					mScale = 0;
				
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				
				if( (float)metrics.widthPixels / mScale < (float)mMinWidth || 
					(float)metrics.heightPixels / mScale < (float)mMinHeight )
				{
					mScale = 0;
				}
			}
		}
		FWGSLib.cmp.startForegroundService( this, new Intent( getBaseContext(), XashService.class ) );
		Intent debugIntent = new Intent( getBaseContext(), DebugService.class );
		debugIntent.putExtra("PID", android.os.Process.myPid());
		FWGSLib.cmp.startForegroundService( this, debugIntent );

		mEngineReady = true;
	}
	
	private boolean checkCstrike( String gamelibdir, String packagename, String signature )
	{
		return ( !FWGSLib.checkGameLibDir( gamelibdir, packagename ) ||
				CertCheck.dumbCertificateCheck( this, packagename, signature, false ) );
	}

	private boolean setupEnvironment()
	{
		Intent intent = getIntent();
		
		String enginedir = FWGSLib.cmp.getNativeLibDir(this);
		
		String argv       = FWGSLib.getStringExtraFromIntent( intent, "argv", mPref.getString( "argv", "-dev 3 -log" ) );
		String gamelibdir = FWGSLib.getStringExtraFromIntent( intent, "gamelibdir", enginedir );
		String gamedir    = FWGSLib.getStringExtraFromIntent( intent, "gamedir", "valve" );
		String basedir    = FWGSLib.getStringExtraFromIntent( intent, "basedir", mPref.getString( "basedir", "/sdcard/xash/" ) );
		String gdbsafe    = intent.getStringExtra( "gdbsafe" );
		
		bIsCstrike = ( gamedir.equals("cstrike") || gamedir.equals("czero") || gamedir.equals("czeror") );
		
		if( bIsCstrike )
		{
			mMinWidth = 640;
			mMinHeight = 300;

			final String velaron_signature = "AtsVnfclsm6zLW/qxxPk970zgfA=";
			final String sdlash_signature = "DMsE8f5hlR7211D8uehbFpbA0n8=";
			
			if ( !checkCstrike( gamelibdir, "su.xash.cs16client", velaron_signature ) && !checkCstrike( gamelibdir, "in.celest.xash3d.cs16client", sdlash_signature ) )
			{
				finish();
				return false;
			}
		}
		
		if( gdbsafe != null || Debug.isDebuggerConnected() )
		{
			fGDBSafe = true;
			Log.e( TAG, "GDBSafe mode enabled!" );
		}
		
		Log.d( TAG, "argv = " + argv );
		Log.d( TAG, "gamedir = " + gamedir );
		Log.d( TAG, "basedir = " + basedir );
		Log.d( TAG, "enginedir = " + enginedir );
		Log.d( TAG, "gamelibdir = " + gamelibdir );
		
		mArgv = argv.split( " " );
		
		if( mUseRoDir )
		{
			Log.d( TAG, "Enabled RoDir: " + basedir + " -> " + mWriteDir );
		
			XashBinding.setenv( "XASH3D_RODIR",   basedir,   true );
			XashBinding.setenv( "XASH3D_BASEDIR", mWriteDir, true );
		}
		else
		{
			Log.d( TAG, "Disabled RoDir: " + basedir );
			
			XashBinding.setenv( "XASH3D_BASEDIR", basedir,   true );
		}
		XashBinding.setenv( "XASH3D_ENGLIBDIR",  enginedir,  true );
		XashBinding.setenv( "XASH3D_GAMELIBDIR", gamelibdir, true );
		XashBinding.setenv( "XASH3D_GAMEDIR",    gamedir,    true );
		XashBinding.setenv( "XASH3D_EXTRAS_PAK1", getFilesDir().getPath() + "/extras.pak", true );
		Log.d( TAG, "enginepak = " + getFilesDir().getPath() + "/extras.pak" );
		
		String pakfile = intent.getStringExtra( "pakfile" );
		if( pakfile != null && pakfile != "" )
			XashBinding.setenv( "XASH3D_EXTRAS_PAK2", pakfile, true );
		Log.d( TAG, "pakfile = " + ( pakfile != null ? pakfile : "null" ) );
		
		String[] env = intent.getStringArrayExtra( "env" );
		if( env != null )
		{
			try
			{
				for( int i = 0; i + 1 < env.length; i += 2 )
				{
					Log.d(TAG, "extraEnv[" + env[i] + "] = " + env[i + 1]);
					XashBinding.setenv( env[i], env[i + 1], true );
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		
		return true;
	}

	


	public static boolean handleKey( int keyCode, KeyEvent event )
	{
		if( mUseVolume && ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
			keyCode == KeyEvent.KEYCODE_VOLUME_UP ) )
			return false;
			
		final int source = XashActivity.handler.getSource( event );
		final int action = event.getAction();
		final boolean isGamePad  = FWGSLib.FExactBitSet( source, InputDevice.SOURCE_GAMEPAD );
		final boolean isJoystick = FWGSLib.FExactBitSet( source, InputDevice.SOURCE_CLASS_JOYSTICK );
		final boolean isDPad     = FWGSLib.FExactBitSet( source, InputDevice.SOURCE_DPAD );
		
		if( isDPad )
		{
			byte val;
			final byte hat = 0;
			final int id = 0;
			Log.d( TAG, "DPAD button: " + keyCode );
			switch( keyCode )
			{
			case KeyEvent.KEYCODE_DPAD_CENTER: val = JOY_HAT_CENTERED; break;
			case KeyEvent.KEYCODE_DPAD_UP:     val = JOY_HAT_UP;       break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:  val = JOY_HAT_RIGHT;    break;
			case KeyEvent.KEYCODE_DPAD_DOWN:   val = JOY_HAT_DOWN;     break;
			case KeyEvent.KEYCODE_DPAD_LEFT:   val = JOY_HAT_LEFT;     break;
			default: 
				return performEngineKeyEvent( action, keyCode, event );
			}

			if( action == KeyEvent.ACTION_DOWN )
			{
				XashBinding.nativeHat( id, hat, val, true  );
				return true;
			}
			else if( action == KeyEvent.ACTION_UP )
			{
				XashBinding.nativeHat( id, hat, val, false );
				return true;
			}

			return false;
		}

		// Engine will bind these to AUX${val} virtual keys
		// Android may send event without source flags set to GAMEPAD or CLASS_JOYSTICK
		// so check for gamepad buttons anyway
		if( isGamePad || isJoystick || XashActivity.handler.isGamepadButton( keyCode ) )
		{
			final int id = 0;
			byte val = 15;
			
			switch( keyCode )
			{
			// main buttons
			// see keydefs.h and keys.c
			case KeyEvent.KEYCODE_BUTTON_A:      val = 0; break;
			case KeyEvent.KEYCODE_BUTTON_B:	     val = 1; break;
			case KeyEvent.KEYCODE_BUTTON_X:	     val = 2; break;
			case KeyEvent.KEYCODE_BUTTON_Y:	     val = 3; break;
			case KeyEvent.KEYCODE_BUTTON_L1:     val = 4; break;
			case KeyEvent.KEYCODE_BUTTON_R1:     val = 5; break;
			case KeyEvent.KEYCODE_BUTTON_SELECT: val = 6; break;
			case KeyEvent.KEYCODE_BUTTON_MODE:   val = 7; break;
			case KeyEvent.KEYCODE_BUTTON_START:  val = 8; break;
			case KeyEvent.KEYCODE_BUTTON_THUMBL: val = 9; break;
			case KeyEvent.KEYCODE_BUTTON_THUMBR: val = 10; break;
			
			// other
			case KeyEvent.KEYCODE_BUTTON_L2: val = 11; break;
			case KeyEvent.KEYCODE_BUTTON_R2: val = 12; break;
			case KeyEvent.KEYCODE_BUTTON_C:  val = 13; break;
			case KeyEvent.KEYCODE_BUTTON_Z:  val = 14; break;
			default: 
				if( keyCode >= KeyEvent.KEYCODE_BUTTON_1 && keyCode <= KeyEvent.KEYCODE_BUTTON_16 )
				{
					val = ( byte )( ( keyCode - KeyEvent.KEYCODE_BUTTON_1 ) + 15);
				}
				else if( XashActivity.handler.isGamepadButton( keyCode ) )
				{
					// maybe never reached, as all possible gamepad buttons are checked before
					Log.d( TAG, "Unhandled GamePad button: " + XashActivity.handler.keyCodeToString( keyCode ) );
					return false;
				}
				else
				{
					// must be never reached too, but sometimes happens(for DPad, for example)
					Log.d( TAG, "Unhandled GamePad button: " + XashActivity.handler.keyCodeToString( keyCode ) + ". Passed as simple key.");
					return performEngineKeyEvent( action, keyCode, event );
				}
			}

			if( action == KeyEvent.ACTION_DOWN )
			{
				XashBinding.nativeJoyButton( id, val, true );
				return true;
			}
			else if( action == KeyEvent.ACTION_UP )
			{
				XashBinding.nativeJoyButton( id, val, false );
				return true;
			}
			return false;
		}

		return performEngineKeyEvent( action, keyCode, event );
	}

	public static boolean performEngineKeyEvent( int action, int keyCode, KeyEvent event )
	{
		//Log.v(TAG, "EngineKeyEvent( " + action +", " + keyCode +" "+ event.isCtrlPressed() +" )");
		if( action == KeyEvent.ACTION_DOWN )
		{
			if( event.isPrintingKey() || keyCode == 62 )// space is printing too
				XashBinding.nativeString( String.valueOf( ( char )event.getUnicodeChar() ) );
			
			XashBinding.nativeKey( 1, keyCode );
			
			return true;
		}
		/*else if( action == KeyEvent.ACTION_MULTIPLE )
		{
			if( keyCode == KeyEvent.KEYCODE_UNKNOWN )
			{
				XashActivity.nativeString( event.getCharacters() );
			}
			else
			{
				// maybe unneeded
			}
		}*/
		else if( action == KeyEvent.ACTION_UP )
		{
			XashBinding.nativeKey( 0, keyCode );
			
			return true;
		}
		return false;
	}
	
	public static float performEngineAxisEvent( float current, byte engineAxis, float prev, float flat )
	{
		if( prev != current )
		{
			final int id = 0;
			final short SHRT_MAX = 32767;
			if( current <= flat && current >= -flat )
				current = 0;
			
			XashBinding.nativeAxis( id, engineAxis, ( short )( current * SHRT_MAX ) );
		}
		
		return current;
	}
	
	public static float performEngineHatEvent( float curr, boolean isXAxis, float prev )
	{
		if( prev != curr )
		{
			final int id = 0;
			final byte hat = 0;
			if( isXAxis )
			{
				if( curr > 0 ) XashBinding.nativeHat( id, hat, JOY_HAT_RIGHT, true );
				else if( curr < 0 ) XashBinding.nativeHat( id, hat, JOY_HAT_LEFT,  true );
				// unpress previous if curr centered
				else if( prev > 0 ) XashBinding.nativeHat( id, hat, JOY_HAT_RIGHT, false );
				else if( prev < 0 ) XashBinding.nativeHat( id, hat, JOY_HAT_LEFT,  false );
			}
			else
			{
				if( curr > 0 ) XashBinding.nativeHat( id, hat, JOY_HAT_DOWN, true );
				else if( curr < 0 ) XashBinding.nativeHat( id, hat, JOY_HAT_UP,   true );
				// unpress previous if curr centered
				else if( prev > 0 ) XashBinding.nativeHat( id, hat, JOY_HAT_DOWN, false );
				else if( prev < 0 ) XashBinding.nativeHat( id, hat, JOY_HAT_UP,   false );
			}
		}
		return curr;
	}

	public static void showInput( int show )
	{
		InputMethodManager imm = ( InputMethodManager )mSingleton.getSystemService( Context.INPUT_METHOD_SERVICE );
			
		if( mTextEdit == null )
		{
			mTextEdit = new DummyEdit( mSingleton );
			mLayout.addView( mTextEdit );
		}
		if( show == 1 )
		{
			mTextEdit.setVisibility( View.VISIBLE );
			mTextEdit.requestFocus();
				
			imm.showSoftInput( mTextEdit, 0 );
			keyboardVisible = true;
			FWGSLib.cmp.applyImmersiveMode( keyboardVisible, mDecorView );
		}
		else
		{
			mTextEdit.setVisibility( View.GONE );
			imm.hideSoftInputFromWindow( mTextEdit.getWindowToken(), 0 );
			keyboardVisible = false;
			FWGSLib.cmp.applyImmersiveMode( keyboardVisible, mDecorView );
		}
	}

	
	public static void GenericUpdatePage()
	{
		mSingleton.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse("https://github.com/FWGS/xash3d/releases/latest" ) ) );
	}
	
	public static void PlatformUpdatePage()
	{
		// open GP
		try 
		{
			mSingleton.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse("market://details?id=in.celest.xash3d.hl") ) );
		}
		catch( android.content.ActivityNotFoundException e ) 
		{
			GenericUpdatePage();
		}
	}
	
}

/**
 EngineSurface. This is what we draw on, so we need to know when it's created
 in order to do anything useful.

 Because of this, that's where we set up the Xash3D thread
 */
class EngineSurface extends SurfaceView implements SurfaceHolder.Callback, View.OnKeyListener 
{
	public static final String TAG = "XASH3D-EngineSurface";
	
	// This is what Xash3D runs in. It invokes main(), eventually
	private static Thread mEngThread = null;
	private static Object mPauseLock = new Object(); 

	// EGL private objects
	private EGLContext  mEGLContext;
	private EGLSurface  mEGLSurface;
	private EGLDisplay  mEGLDisplay;
	private EGL10 mEGL;
	private EGLConfig mEGLConfig;
	private final static int surface_pause = 0;
	private final static int surface_active = 1;
	private final static int surface_dummy = 2;
	private int mEngineWidth = 640, mEngineHeight = 480;
	public boolean mResizeEnabled = false;
	// Sensors

	// Startup
	public EngineSurface( Context context )
	{
		super( context );
		getHolder().addCallback( this );

		setFocusable( true );
		setFocusableInTouchMode( true );
		requestFocus();
		setOnKeyListener( this );
		setOnTouchListener( XashInput.getTouchListener() );
	}
	
	// Called when we have a valid drawing surface
	public void surfaceCreated( SurfaceHolder holder )
	{
		Log.v( TAG, "surfaceCreated()" );
		
//		if( mEGL == null )
//			return;
		
		XashBinding.nativeSetPause( 0 );
		XashActivity.mEnginePaused = false;
		//holder.setFixedSize(640,480);
		//SurfaceHolder.setFixedSize(640,480);
	}

	// Called when we lose the surface
	public void surfaceDestroyed( SurfaceHolder holder )
	{
		Log.v( TAG, "surfaceDestroyed()" );
		
//		if( mEGL == null )
//			return;
		
		XashBinding.nativeSetPause(1);
		engineThreadWait();
	}

	// calculate surface size and configure holder
	public void setSurfaceSize( SurfaceHolder holder, int width, int height )
	{
		if( ( XashActivity.mForceHeight!= 0 && XashActivity.mForceWidth!= 0 || XashActivity.mScale != 0 ) )
		{
			int newWidth, newHeight;

			if( XashActivity.mForceHeight != 0 && XashActivity.mForceWidth != 0 )
			{
				newWidth = XashActivity.mForceWidth;
				newHeight = XashActivity.mForceHeight;
			}
			else
			{
				newWidth = ( int )( width / XashActivity.mScale );
				newHeight = ( int )( height / XashActivity.mScale );
			}
			//holder.setFixedSize( newWidth, newHeight );
			XashActivity.mTouchScaleX = ( float )newWidth / width;
			XashActivity.mTouchScaleY = ( float )newHeight / height;
			
			width = newWidth;
			height = newHeight;
			mResizeEnabled = true;
		}
		// Android may force only-landscape app to portait during lock
		// Just don't notify engine in that case
		if( width > height )
		{
			mEngineWidth = width;
			mEngineHeight = height;
			if( mResizeEnabled )
				holder.setFixedSize( width, height );
		}
	}

	// Called when the surface is resized
	public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
	{
		Log.v( TAG, "surfaceChanged(" + width + ","+ height+")" ); 

		setSurfaceSize( holder, getWidth(), getHeight() );

		XashBinding.onNativeResize( mEngineWidth, mEngineHeight );

		// Now start up the C app thread
		if( mEngThread == null ) 
		{
			mEngThread = new Thread( new Runnable(){
				@Override
				public void run()
				{
					XashBinding.nativeInit( XashActivity.mArgv );
				}
			}, "EngineThread" );
			mEngThread.start();
		}
	}
	
	public void engineThreadJoin()
	{
		Log.v( TAG, "engineThreadJoin()" );
		try
		{
			if( mEngThread != null )
				mEngThread.join( 5000 ); // wait until Xash will quit
		}
		catch( InterruptedException e )
		{
		}
	}
	
	public void engineThreadWait()
	{
		if( XashActivity.fGDBSafe )
			return;
		
		Log.v( TAG, "engineThreadWait()" );
		synchronized( mPauseLock )
		{
			try
			{
				mPauseLock.wait( 5000 ); // wait until engine notify
			}
			catch( InterruptedException e )
			{
			}
		}
	}
	
	public void engineThreadNotify()
	{
		if( XashActivity.fGDBSafe )
			return;
		
		Log.v( TAG, "engineThreadNotify()" );
		synchronized( mPauseLock )
		{
			mPauseLock.notify(); // send notify
		}
	}
	
	// unused
	public void onDraw( Canvas canvas )
	{
	}

	// first, initialize native backend
	public Surface getNativeSurface( int type )
	{
		if( type == surface_active )
			return getHolder().getSurface();
		else if( type == surface_dummy )
		{
			return FWGSLib.cmp.getDummySurface( getContext() );
		}
		return null;
	}
	
	public int getGLAttribute( final int attr )
	{
		EGL10 egl = ( EGL10 )EGLContext.getEGL();

		try
		{
			int[] value = new int[1];
			boolean ret = egl.eglGetConfigAttrib(mEGLDisplay, mEGLConfig, attr, value);
				
			if( !ret )
			{
				Log.e(TAG, "getGLAttribute(): eglGetConfigAttrib error " + egl.eglGetError());
				return 0;
			}
				
			// Log.e(TAG, "getGLAttribute(): " + attr + " => " + value[0]);
				
			return value[0];
		}
		catch( Exception e  )
		{
			Log.v( TAG, e + ": " + e.getMessage() + " " + egl.eglGetError() );
			for( StackTraceElement s : e.getStackTrace() ) 
			{
				Log.v( TAG, s.toString() );
			}
		}
		return 0;
	}
	
	// EGL functions
	public boolean InitGL( int[] attr, int[] contextAttrs ) 
	{
		Log.v( TAG, "attributes: " + Arrays.toString(attr));
		Log.v( TAG, "contextAttrs: " + Arrays.toString(contextAttrs));
		EGL10 egl = ( EGL10 )EGLContext.getEGL();
		if( egl == null )
		{
			Log.e( TAG, "Cannot get EGL from context" );
			return false;
		}
		
		try
		{
			EGLDisplay dpy = egl.eglGetDisplay( EGL10.EGL_DEFAULT_DISPLAY );

			if( dpy == null )
			{
				Log.e( TAG, "Cannot get display" );
				return false;
			}
			
			int[] version = new int[2];
			if( !egl.eglInitialize( dpy, version ) )
			{
				Log.e( TAG, "No EGL config available" );
				return false;
			}
			
			
			EGLConfig[] configs = new EGLConfig[1];
			int[] num_config = new int[1];
			if( !egl.eglChooseConfig( dpy, attr, configs, 1, num_config ) || num_config[0] == 0 )
			{
				Log.e( TAG, "No EGL config available" );
				return false;
			}
			EGLConfig config = configs[0];

			EGLContext ctx = egl.eglCreateContext( dpy, config, EGL10.EGL_NO_CONTEXT, contextAttrs );
			if( ctx == EGL10.EGL_NO_CONTEXT )
			{
				Log.e( TAG, "Couldn't create context" );
				return false;
			}

			EGLSurface surface = egl.eglCreateWindowSurface( dpy, config, this, null );
			if( surface == EGL10.EGL_NO_SURFACE )
			{
				Log.e( TAG, "Couldn't create surface" );
				return false;
			}

			if( !egl.eglMakeCurrent( dpy, surface, surface, ctx ) )
			{
				Log.e( TAG, "Couldn't make context current" );
				return false;
			}
			
			mEGLContext = ctx;
			mEGLDisplay = dpy;
			mEGLSurface = surface;
			mEGL = egl;
			mEGLConfig = config;
		} 
		catch( Exception e ) 
		{
			Log.v( TAG, e + ": " + e.getMessage() + " " + egl.eglGetError() );
			for( StackTraceElement s : e.getStackTrace() ) 
			{
				Log.v( TAG, s.toString() );
			}
			
			return false;
		}
		
		return true;
	}

	// EGL buffer flip
	public void SwapBuffers()
	{
		if( mEGLSurface == null )
			return;
		
		mEGL.eglSwapBuffers( mEGLDisplay, mEGLSurface );
	}
	
	public void toggleEGL( int toggle )
	{
		Log.v( TAG, "toggleEGL "+ toggle );

		if( mEGLSurface != null )
		{
			mEGL.eglMakeCurrent( mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT );
			mEGL.eglDestroySurface( mEGLDisplay, mEGLSurface );
			mEGLSurface = null;
		}
		if( toggle == surface_active )
		{
			mEGLSurface = mEGL.eglCreateWindowSurface( mEGLDisplay, mEGLConfig, this, null );
			mEGL.eglMakeCurrent( mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext );
		}
		else if( toggle == surface_dummy ) // only used with android_sleep=0
		{
			try
			{ // first, check if surfaceless context allowed
			   if( mEGL.eglQueryString( mEGLDisplay, EGL10.EGL_EXTENSIONS ).contains( "EGL_KHR_surfaceless_context" )
			   &&  mEGL.eglMakeCurrent( mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, mEGLContext ))
					return;
			}
			catch( Exception e ) { e.printStackTrace(); }
			try
			{ // if no or it failed, try use dummy SurfaceTexture
				Surface o = FWGSLib.cmp.getDummySurface( getContext() );
				mEGLSurface = mEGL.eglCreateWindowSurface( mEGLDisplay, mEGLConfig, o, null );
				if(!mEGL.eglMakeCurrent( mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext ) )
					throw new Exception("MakeCurrent failed");
			}
			catch( Exception e2 )
			{
				e2.printStackTrace();
				try
				{ // last try, surfaceless without ext, it will be broken anyway
					mEGL.eglMakeCurrent( mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, mEGLContext );
				}
				catch( Exception e3 ) { e3.printStackTrace(); }
			}
		}
	}
	
	public void ShutdownGL()
	{
		mEGL.eglDestroyContext( mEGLDisplay, mEGLContext );
		mEGLContext = null;
		
		mEGL.eglDestroySurface( mEGLDisplay, mEGLSurface );
		mEGLSurface = null;
		
		mEGL.eglTerminate( mEGLDisplay );
		mEGLDisplay = null;
	}
	
	@Override
	public boolean onKey( View v, int keyCode, KeyEvent event )
	{
		return XashActivity.handleKey( keyCode, event );
	}

	@Override
	public boolean onKeyPreIme( int keyCode, KeyEvent event )
	{
		Log.v( TAG, "PreIme: " + keyCode );
		return super.dispatchKeyEvent( event );
	}

}

class XashBinding
{
	// jni exports
	public static native int  nativeInit( Object arguments );
	public static native void nativeQuit();
	public static native void onNativeResize( int x, int y );
	public static native void nativeTouch( int pointerFingerId, int action, float x, float y );
	public static native void nativeKey( int down, int code );
	public static native void nativeString( String text );
	public static native void nativeSetPause( int pause );
	public static native void nativeOnDestroy();
	public static native void nativeOnResume();
	public static native void nativeOnFocusChange();
	public static native void nativeOnPause();
	public static native void nativeUnPause();
	public static native void nativeHat( int id, byte hat, byte keycode, boolean down ) ;
	public static native void nativeAxis( int id, byte axis, short value );
	public static native void nativeJoyButton( int id, byte button, boolean down );
	public static native int  nativeTestWritePermission( String path );
	public static native void nativeMouseMove( float x, float y );
	
	// for future expansion
	public static native void nativeBall( int id, byte ball, short xrel, short yrel );
	public static native void nativeJoyAdd( int id );
	public static native void nativeJoyDel( int id );
	
	// libjnisetenv
	public static native int setenv( String key, String value, boolean overwrite );

	// Java functions called from C
	public static boolean createGLContext( int[] attr, int[] contextAttr ) 
	{
		return XashActivity.mSurface.InitGL(attr, contextAttr);
	}
	
	public static int getGLAttribute( int attr )
	{
		return XashActivity.mSurface.getGLAttribute( attr );
	}
	
	public static void swapBuffers() 
	{
		XashActivity.mSurface.SwapBuffers();
	}
	
	public static void engineThreadNotify()
	{
		XashActivity.mSurface.engineThreadNotify();
	}
	
	public static Surface getNativeSurface( int type )
	{
		return XashActivity.mSurface.getNativeSurface( type );
	}
	
	public static void vibrate( int time ) 
	{
		if( XashActivity.mHasVibrator )
		{
			XashActivity.mVibrator.vibrate( time );
		}
	}
	
	public static void toggleEGL( int toggle )
	{
		XashActivity.mSurface.toggleEGL( toggle );
	}

	public static boolean deleteGLContext()
	{
		XashActivity.mSurface.ShutdownGL();
		return true;
	}

	// getNativeObject()?
	/*public static Context getContext() 
	{
		return XashActivity.mSingleton;
	}*/

	public static void showKeyboard( int show1 )
	{
		final int show = show1;
		// Transfer the task to the main thread as a Runnable
		XashActivity.mSingleton.runOnUiThread( new Runnable( ){
			@Override
			public void run() 
			{
				XashActivity.showInput( show );
			}
		} );
	}

	public static void setIcon( String path )
	{
		if( XashActivity.fGDBSafe )
			return;
		
		Log.v( "XASH", "setIcon(" + path + ")" );
		
		try
		{
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			
			Bitmap icon = BitmapFactory.decodeFile( path, o );
			
			if( icon.getWidth() < 16 )
				return;
			
			XashService.not.setIcon(icon);
		}
		catch( Exception e )
		{
		}
	}
	
	public static void setTitle( String title )
	{
		Log.v( "XASH", "setTitle(" + title + ")" );
		try
		{
			SharedPreferences.Editor editor = XashActivity.mPref.edit();
			editor.putBoolean("successfulRun", true);
			editor.commit();
			
			XashService.not.setText(title);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void preShutdown( )
	{
		Log.v( "XASH", "preShutdown" );
		try
		{
			XashService.requestStop();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getAndroidID()
	{
		String str = Secure.getString( XashActivity.mSingleton.getContentResolver(), Secure.ANDROID_ID );
		
		if( str == null )
			return "";
		
		return str;
	}

	public static String loadID()
	{
		return XashActivity.mPref.getString( "xash_id", "" );
	}

	public static void saveID( String id )
	{
		SharedPreferences.Editor editor = XashActivity.mPref.edit();

		editor.putString( "xash_id", id );
		editor.commit();
	}

	public static void showMouse( int show )
	{
		boolean sh = show != 0;
		XashActivity.fMouseShown = sh;
		XashActivity.handler.showMouse( sh );
	}

		// Just opens browser or update page
	public static void shellExecute( String path )
	{
		if( path.equals("PlatformUpdatePage"))
		{
			XashActivity.PlatformUpdatePage();
			return;
		}
		else if( path.equals( "GenericUpdatePage" ))
		{
			XashActivity.GenericUpdatePage();
			return;
		}
	
		final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(path));
		XashActivity.mSingleton.startActivity(intent);
	}

	static String[] messageboxData = new String[2];
	public static void messageBox( String title, String text )
	{
		Log.e( "Messagebox", title );
		Log.e( title, text );
		messageboxData[0] = title;
		messageboxData[1] = text;
		XashActivity.mSingleton.runOnUiThread( new Runnable() 
		{
			@Override
					public void run()
			{
				new AlertDialog.Builder( XashActivity.mSingleton )
						.setTitle( messageboxData[0] )
						.setMessage( messageboxData[1] )
						.setPositiveButton( "Ok", new DialogInterface.OnClickListener() 
				{
					public void onClick( DialogInterface dialog, int whichButton ) 
					{
						synchronized( messageboxData )
						{
							messageboxData.notify();
						}
					}
				})
						.setCancelable( false )
								.show();
			}
		});

		synchronized( messageboxData ) 
		{
			try 
			{
				messageboxData.wait();
			} 
			catch( InterruptedException ex )
			{
				ex.printStackTrace();
			}
		}
	}
}

