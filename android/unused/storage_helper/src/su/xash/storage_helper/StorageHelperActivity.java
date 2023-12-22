package su.xash.storage_helper;
import android.widget.LinearLayout.*;
import android.widget.LinearLayout;
import android.widget.Button;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.util.Log;
import su.xash.fwgslib.Sender;
import android.content.Intent;
/**
* A terminal emulator activity.
*/


public class StorageHelperActivity extends Activity {

	private static native int getFd();
	private static native void sendPath();

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
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.e("StorageHelper", "onCreate");
		
		System.loadLibrary("fdhelper");

		//setContentView(R.layout.term_activity);

		//mEmulatorView = (EmulatorView) findViewById(EMULATOR_VIEW);
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams maxweight = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		maxweight.weight = 1;
		setContentView(layout);
		String path = getFilesDir().getPath() + "/test";
		(new File(path)).mkdirs();
		chmod( getFilesDir().getPath(), 0777 );
		chmod( path, 0777 );
		path = getFilesDir().getPath() + "/test/dir";
		(new File(path)).mkdirs();
		chmod( path, 0777 );
		final Sender objSent = new Sender.Stub(){
			@Override
			public android.os.ParcelFileDescriptor getFD() throws android.os.RemoteException
			{
				try {
					int fd = StorageHelperActivity.getFd();
					Log.d("XashStorage","got fd " + fd);
				new Thread(new Runnable() {
				public void run() {
					try{
					Thread.sleep(10000);
					}catch(Exception e){}
					sendPath();
				}
				}).start();;
					return ParcelFileDescriptor.fromFd(fd);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					throw new android.os.RemoteException();
				}
			}
			@Override
			public void sendFD(android.os.ParcelFileDescriptor fd) throws android.os.RemoteException
			{
			}
		};

			final Bundle bundle = new Bundle();
    		bundle.putBinder("object_value", objSent.asBinder());
			startActivity(new Intent().setComponent(new android.content.ComponentName("su.xash.engine", "su.xash.engine.LauncherActivity")).putExtras(bundle));
			/*
		try{
			System.loadLibrary( "xash" );
			final Sender objReceived = Sender.Stub.asInterface(getIntent().getExtras().getBinder("object_value"));
			gFD = objReceived.getFD();
			int fd = gFD.getFd();
			Log.d("XashRemoteFD", "" + fd);
			Log.d("XashReceivedFD", "" + XashBinding.receiveFD(fd));
			Log.d("path", new File("").getAbsoluteFile().getPath() );
		}
		catch(Exception e){e.printStackTrace();} */


	}




	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

}