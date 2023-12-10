package su.xash.fwgslib;

import android.Manifest;
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
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import org.json.*;
import android.preference.*;
import java.lang.reflect.*;

/*
 * This utility class is intended to hide some Android and Java design-flaws and
 * also just shortcuts
 */
public class FWGSLib
{
	private static final String TAG = "FWGSLib";
	static String externalFilesDir;
	public static boolean FBitSet( final int bits, final int mask )
	{
		return ((bits & mask) != 0);
	}
	
	public static boolean FExactBitSet( final int bits, final int mask )
	{
		return ((bits & mask) == mask );
	}
	
	public static float atof( String str, float fallback )
	{
		float ret;
		try
		{
			ret = Float.valueOf( str );
		}
		catch( Exception e )
		{
			ret = fallback;
		}
		
		return ret;
	}
	
	public static int atoi( String str, int fallback )
	{
		int ret;
		try
		{
			ret = Integer.valueOf( str );
		}
		catch( Exception e )
		{
			ret = fallback;
		}
		
		return ret;
	}
	
	public static boolean checkGameLibDir( String gamelibdir, String allowed )
	{
		try
		{
			Log.d( TAG, " gamelibdir = " + gamelibdir + " allowed = " + allowed );
			
			if( gamelibdir.contains( "/.." ))
				return false;
			
			File f = new File( gamelibdir );
		
			if( !f.isDirectory() )
			{
				Log.d( TAG, "Not a directory" );
				return false;
			}
		
			if( !f.exists() )
			{
				Log.d( TAG, "Does not exist" );
				return false;
			}
			
			// add trailing / for simple regexp
			if( gamelibdir.charAt(gamelibdir.length() - 1) != '/' )
				gamelibdir = gamelibdir + "/";
					
			final String regex = ".+\\/" + allowed.replace(".",  "\\.") + "(|(-\\d))\\/(.+|)";
			
			Log.d( TAG, regex );
		
			final boolean ret =  gamelibdir.matches( regex );
			
			Log.d( TAG, "ret = " + ret );
			
			return ret;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static String getDefaultXashPath()
	{
		File dir = Environment.getExternalStorageDirectory();
		if( dir != null && dir.exists() )
			return dir.getPath() + "/xash";
		return "/sdcard/xash";
	}
	static class GetExternalFilesDir extends Thread
	{
		Context ctx;
		GetExternalFilesDir( Context ctx1 )
		{
			ctx = ctx1;
		}
		@Override
		public void run()
		{
			try
			{
				File f = ctx.getExternalFilesDir(null);

				f.mkdirs();

				externalFilesDir = f.getAbsolutePath();
				Log.d(TAG, "getExternalFilesDir success");
			}
			catch( Exception e )
			{
				Log.e( TAG, e.toString(), e);
			}
		}
	}
	public static String getExternalFilesDir( Context ctx )
	{
		if( externalFilesDir != null )
			return externalFilesDir;
		try
		{
			if( sdk >= 8 )
			{
				Thread t = new GetExternalFilesDir(ctx);
				t.start();
				t.join(2000);
			}
		}
		catch(Exception e)
		{
			Log.e( TAG, e.toString(), e);
			externalFilesDir = getDefaultXashPath();
		}
		if( externalFilesDir == null )
			externalFilesDir = getDefaultXashPath();
		return externalFilesDir;
	}
	
	public static boolean isLandscapeOrientation( Activity act )
	{
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return (metrics.widthPixels > metrics.heightPixels);
	}
	
	public static String getStringExtraFromIntent( Intent intent, String extraString, String ifNotFound )
	{
		String ret = intent.getStringExtra( extraString );
		if( ret == null )
		{
			ret = ifNotFound;
		}
		
		return ret;
	}

	public static String readlink(String path) throws Exception
	{
		java.lang.Process process = Runtime.getRuntime().exec( new String [] { "/system/bin/sh", "-c", "TERM=vt100 ls -l \"" + path + "\" 2>/dev/null" });
		InputStream reader = process.getInputStream();
		char prev = '\0';
		boolean found = false;
		String output = "";
		while(true)
		{
			try
			{
				int c = reader.read();
				if( c <= 0 )break;
				char ch = ( char )c;

				if( found )
				{
					if( ch != '\n' )
						output += ch;
					else
						while( reader.read() >= 0 ) continue;
				}
				else if( ch == '>' && prev == '-' && reader.read() == 0x20 )
					found = true;
				prev = ch;
			}
			catch( Exception e ){
				e.printStackTrace();
				break;
			}
		}
		process.waitFor();
		if( output.length() == 0)
			throw new Exception();
		return output;
	}
	public static byte[] getPermsBuffer(String path) throws Exception
	{
		String cmd = "TERM=vt100 ls -l \"" + path + "\" 2>/dev/null";
		Log.d("exec", cmd );
		java.lang.Process process = Runtime.getRuntime().exec( new String [] { "/system/bin/sh", "-c", cmd });
		InputStream reader = process.getInputStream();
		byte[] perms = new byte[10]; // drwxrwxrwx
		reader.read(perms);
		Log.d("exec", "result: "+ new String(perms) );
		process.waitFor();
		return perms;
	}
	public static boolean canExecute(String path)
	{
		try{
			byte[] perms = getPermsBuffer(path);
			return perms[3] == (byte)'x';
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return true;
		}
	}

	public static int chmod(String path, int mode)
	{
		try {
			String cmd = "chmod " + Integer.toOctalString( mode ) + " \"" + path + "\"";
			Log.d("exec", cmd );
			java.lang.Process process = Runtime.getRuntime().exec( new String [] { "/system/bin/sh", "-c", cmd });
			return process.waitFor();
		} catch(Exception e)
		{
			e.printStackTrace();
			return 127;
		}
	}
	public static String execFallback(Context ctx, String path)
	{
		try
		{
			if( canExecute( path ) )
				return path;
			if( chmod( path, 0777 ) == 0 && canExecute(path) )
				return path;
		}
		catch(Exception e){ e.printStackTrace();}
		try{
			String fallback = ctx.getFilesDir().getPath() + '/' + path.substring(path.lastIndexOf('/')+1);
			String cmd = "cat \"" + path + "\" > \"" + fallback +"\";chmod 777 \"" + fallback+ "\"";
			Log.d("exec", cmd );
			java.lang.Process process = Runtime.getRuntime().exec( new String [] { "/system/bin/sh", "-c", cmd });
			if(process.waitFor() == 0 && canExecute( fallback ))
				return fallback;
		}
		catch( Exception e){ e.printStackTrace();}
		return path;
	}

	public static String getAppProcessPath( int pid )
	{
		try{
			/* NOTE: this does not work for current PID for some reason (process missing in procfs for child processes? thank you, google!),
			but passing PID of other application process works */
			return readlink( "/proc/" + pid + "/exe" );
		}
		catch(Exception e)
		{
			e.printStackTrace();
			String abi = cmp.getAbi();
			if(abi.contains("64"))
				return "/system/bin/app_process64";
			else if(sdk >= 21)
				return "/system/bin/app_process32";
			else
				return "/system/bin/app_process";
		}
	}

	public static void changeButtonsStyle( ViewGroup parent )
	{
		if( sdk >= 21 )
			return;
		
		for( int i = parent.getChildCount() - 1; i >= 0; i-- ) 
		{
			try
			{
				final View child = parent.getChildAt(i);
				
				if( child == null )
					continue;

				if( child instanceof ViewGroup )
				{
					changeButtonsStyle((ViewGroup) child);
					// DO SOMETHING WITH VIEWGROUP, AFTER CHILDREN HAS BEEN LOOPED
				} 
				else if( child instanceof Button )
				{
					final Button b = (Button)child;
					final Drawable bg = b.getBackground();
					if(bg!= null)bg.setAlpha( 96 );
					b.setTextColor( 0xFFFFFFFF );
					b.setTextSize( 15f );
					//b.setText(b.getText().toString().toUpperCase());
					b.setTypeface( b.getTypeface(),Typeface.BOLD );
				}
				else if( child instanceof EditText )
				{
					final EditText b = ( EditText )child;
					b.setBackgroundColor( 0xFF353535 );
					b.setTextColor( 0xFFFFFFFF );
					b.setTextSize( 15f );
				}
			}
			catch( Exception e )
			{
			}
		}
	}

	public static void enableNavbarMenu(Activity act)
	{
		if( sdk < 21 )
			return;
		Window w = act.getWindow();
		for (Class clazz = w.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			try {
				Method method = clazz.getDeclaredMethod("setNeedsMenuKey", int.class);
				method.setAccessible(true);
				try {
					method.invoke(w, 1);  // 1 == WindowManager.LayoutParams.NEEDS_MENU_SET_TRUE
					break;
				} catch (IllegalAccessException e) {
					Log.d(TAG, "IllegalAccessException on window.setNeedsMenuKey");
				} catch (java.lang.reflect.InvocationTargetException e) {
					Log.d(TAG, "InvocationTargetException on window.setNeedsMenuKey");
				}
			} catch (NoSuchMethodException e) {
				// Log.d(TAG, "NoSuchMethodException");
			}
		}
	}
	
	public static class Compat
	{
		public void applyPermissions( final Activity act, final String permissions[], final int code ) {}
		public void applyImmersiveMode( boolean keyboardVisible, View decorView ) {}
		public void startForegroundService( Context ctx, Intent intent ) {ctx.startService( intent );}
		public void startForeground( Service service, int notid, Notification notification ) {}
		public String getNativeLibDir(Context ctx)
		{
			return ctx.getFilesDir().getParentFile().getPath() + "/lib";
		}
		public Surface getDummySurface( Context ctx )
		{
			return null; // it is only possible to get valid surface from XashService, but it's too complex to implement
		}
		public void stopForeground( Service service, int notificationBehavior )
		{
		}
		public String getAbi()
		{
			return "armeabi";
		}
	}
	
	static class Compat_9 extends Compat
	{
		public String getNativeLibDir(Context ctx)
		{
			try {
				ApplicationInfo ai = getApplicationInfo(ctx, null, 0);
				return ai.nativeLibraryDir;
			}
			catch(Exception e) {
				return super.getNativeLibDir(ctx);
			}
		}
		public void startForeground( Service service, int notid, Notification notification ) {
			service.startForeground(notid, notification);
		}
		public void stopForeground( Service service, int notificationBehavior )
		{
			service.stopForeground( notificationBehavior == 1 );
		}
		public String getAbi()
		{
			return Build.CPU_ABI;
		}

	}

	static class Compat_11 extends Compat_9
	{
		private SurfaceTexture mDummySurface = null;

		public Surface getDummySurface( Context ctx )
		{
			if( mDummySurface == null )
			{
				mDummySurface = new SurfaceTexture( false );
			}
			return new Surface( mDummySurface );
		}

	}

	static class Compat_19 extends Compat_11
	{
		public void applyImmersiveMode( boolean keyboardVisible, View decorView )
		{
			if( decorView == null )
				return;
			if( !keyboardVisible )
				decorView.setSystemUiVisibility(
					0x00000100   // View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| 0x00000200 // View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| 0x00000400 // View.SYSTEM_UI_FLAG_LAYOUT_FULSCREEN
					| 0x00000002 // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
					| 0x00000004 // View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
					| 0x00001000 // View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					);
			else
				decorView.setSystemUiVisibility( 0 );
		}
	}

	static class Compat_23 extends Compat_19
	{
		public void applyPermissions( final Activity act, final String permissions[], final int code )
		{
			List<String> requestPermissions = new ArrayList<String>();
		
			for( int i = 0; i < permissions.length; i++ )
			{
				if( act.checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED )
				{
					requestPermissions.add(permissions[i]);
				}
			}
			

			if( !requestPermissions.isEmpty() )
			{
				String[] requestPermissionsArray = new String[requestPermissions.size()];
				for( int i = 0; i < requestPermissions.size(); i++ )
				{
					requestPermissionsArray[i] = requestPermissions.get(i);
				}
				act.requestPermissions(requestPermissionsArray, code);
			}
		}
	}
	
	static class Compat_26 extends Compat_23
	{
		public void startForegroundService(Context ctx, Intent intent){
			ctx.startForegroundService(intent);
		}
		public void stopForeground( Service service, int notificationBehavior )
		{
			service.stopForeground( notificationBehavior );
		}
	}

	public static Compat cmp;
	static {
		int sdk1 = Integer.valueOf(Build.VERSION.SDK);
		if(  sdk1 >= 26 )
			cmp = new Compat_26();
		else if(  sdk1 >= 23 )
			cmp = new Compat_23();
		else if(  sdk1 >= 19 )
			cmp = new Compat_19();
		else if(  sdk1 >= 11 )
			cmp = new Compat_11();
		else if( sdk1 >= 9 )
			cmp = new Compat_9();
		else cmp = new Compat();
	}

	public static ApplicationInfo getApplicationInfo(Context ctx, String pkgName, int flags) throws PackageManager.NameNotFoundException
	{
		PackageManager pm = ctx.getPackageManager();
		
		if( pkgName == null )
			pkgName = ctx.getPackageName();
		
		return pm.getApplicationInfo(pkgName, flags);
	}
	
	public static final int sdk = Integer.valueOf(Build.VERSION.SDK);
}

