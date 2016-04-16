package in.celest.xash3d;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Button;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Environment;
import java.lang.reflect.Method;
import java.util.List;
import java.io.File;

import in.celest.xash3d.hl.R;

public class LauncherActivity extends Activity {
   // public final static String ARGV = "in.celest.xash3d.MESSAGE";
	static EditText cmdArgs;
	static CheckBox useVolume;
	static EditText resPath;
	static SharedPreferences mPref;
	String getDefaultPath()
	{
		File dir = Environment.getExternalStorageDirectory();
		if( dir != null && dir.exists() )
			return dir.getPath() + "/xash";
		return "/sdcard/xash";
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Button selectFolder = ( Button ) findViewById( R.id.button_select );
		if ( Build.VERSION.SDK_INT < 21 )
			selectFolder.setVisibility( View.GONE );
		mPref = getSharedPreferences("engine", 0);
		cmdArgs = (EditText)findViewById(R.id.cmdArgs);
		cmdArgs.setText(mPref.getString("argv","-dev 3 -log"));
		useVolume = ( CheckBox ) findViewById( R.id.useVolume );
		useVolume.setChecked(mPref.getBoolean("usevolume",true));
		resPath = ( EditText ) findViewById( R.id.cmdPath );
		resPath.setText(mPref.getString("basedir", getDefaultPath()));
	}
    
    public void startXash(View view)
    {
	Intent intent = new Intent(this, org.libsdl.app.SDLActivity.class);
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

	SharedPreferences.Editor editor = mPref.edit();
	editor.putString("argv", cmdArgs.getText().toString());
	editor.putBoolean("usevolume",useVolume.isChecked());
	editor.putString("basedir", resPath.getText().toString());
	editor.commit();
	editor.apply();
	startActivity(intent);
    }

	public void aboutXash(View view)
	{
		final Activity a = this;
		this.runOnUiThread(new Runnable() {
			public void run()
			{
				final Dialog dialog = new Dialog(a);
				dialog.setContentView(R.layout.about);
				dialog.setCancelable(true);

				dialog.show();
			}
		});
	}

	public void selectFolder(View view)
	{
		Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT_TREE"); //For api level >= 21
		startActivityForResult(intent, 42);
		resPath.setEnabled(false);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		if ((resultCode == RESULT_OK) && (requestCode == 42) ) {
try{
		Uri pathUri = resultData.getData();
		final List<String> paths = pathUri.getPathSegments();
		String[] parts = paths.get(1).split(":");
		// TODO: Do some workaround regarding the external storage path issue: External storage location showing as internal(emulated/0) on some devices such as Samsung.
        // Maybe try to extract the exact location from URI instead of faking it with getExternalStorageDirectory() ?
		String storagepath = Environment.getExternalStorageDirectory().getPath() + "/";
		String path = storagepath + parts[1];
		if( path != null)
		{
			// Fresh persistent permissions. (For higher API levels of Android)
			// Even if 'path' is wrong (because of the issue noted above), content resolver will take the permissions for correct directory. However, user must change the path(resPath) manually according to real location.
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
				getContentResolver().takePersistableUriPermission(pathUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				getContentResolver().openFileDescriptor(pathUri, "rw");
			}
				resPath.setText( path );
			}
			resPath.setEnabled(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		}
		resPath.setEnabled(true);
	}

	public void createShortcut(View view)
	{
		Intent intent = new Intent(this, ShortcutActivity.class);
		intent.putExtra( "basedir", resPath.getText().toString() );
		intent.putExtra( "name", "Xash3D" );
		intent.putExtra( "argv", cmdArgs.getText().toString() );
		startActivity(intent);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
