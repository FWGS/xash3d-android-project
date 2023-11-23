package su.xash.engine;
import android.widget.LinearLayout.*;
import android.widget.LinearLayout;
import android.widget.Button;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.util.Log;
import su.xash.fwgslib.TermView;
/**
* A terminal emulator activity.
*/


public class Term extends Activity {


	/**
	* Our main view. Displays the emulated terminal screen.
	*/
	private TermView mEmulatorView;

	public static final String LOG_TAG = "Term";

	/**
	* Used to send data to the remote process.
	*/
	private OutputStream mTermOut;


	private int mFontSize = 18;
	private int mColorId = 1;

	private static final int[][] COLOR_SCHEMES = {
		{TermView.BLACK, TermView.WHITE}, {TermView.WHITE, TermView.BLACK}, {TermView.WHITE, TermView.BLUE}};

	private final static String DEFAULT_SHELL = "/system/bin/sh -";
	private String mShell;

	private final static String DEFAULT_INITIAL_COMMAND =
		"hel\t\t\t";
	private String mInitialCommand;


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.e(Term.LOG_TAG, "onCreate");

		//setContentView(R.layout.term_activity);

		//mEmulatorView = (EmulatorView) findViewById(EMULATOR_VIEW);
		mEmulatorView = new TermView(this);
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(mEmulatorView);
		LayoutParams maxweight = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		maxweight.weight = 1;
		mEmulatorView.setLayoutParams(maxweight);
		setContentView(layout);
		LinearLayout footer = new LinearLayout(this);
		footer.setOrientation(LinearLayout.HORIZONTAL);
		Button btn;
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("M");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("C");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("P");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("T");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("I");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("^");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("v");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("<");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText(">");
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("R");
		footer.addView(btn);

		footer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(footer);

		startListening();

		mEmulatorView.setFocusable(true);
		mEmulatorView.setFocusableInTouchMode(true);
		mEmulatorView.requestFocus();
	}

	private void startListening() {
		int[] processId = new int[1];
		Process[] process = new Process[1];

		//createSubprocess(processId);
//        mShellRunning = true;
		try {
			process[0] = Runtime.getRuntime().exec(new String[]{"/system/bin/sh","-c","TERM=vt100 /data/data/su.xash.engine/files/gdb 2>&1"});
		mTermOut = process[0].getOutputStream();
		InputStream termIn = process[0].getInputStream();

		mEmulatorView.initialize(termIn, mTermOut);
		mEmulatorView.setTextSize(mFontSize);
		}
		catch(Exception e){ e.printStackTrace();}

		final int procId = processId[0];
		final Process process1 = process[0];

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
//              mShellRunning = false;
			}
		};

		Runnable watchForDeath = new Runnable() {

			public void run() {
				Log.i(Term.LOG_TAG, "waiting for: " + procId);
				
				int result = -1;
				try
				{result = process1.waitFor();
				}catch(Exception e){e.printStackTrace();}
			//int result = Exec.waitFor(procId);
				Log.i(Term.LOG_TAG, "Subprocess exited: " + result);
				handler.sendEmptyMessage(result);
			}

		};
		Thread watcher = new Thread(watchForDeath);
		watcher.start();

		//mTermOut = new FileOutputStream(mTermFd);
	
//        Process process;

		sendInitialCommand();
	}

	private void sendInitialCommand() {
		String initialCommand = mInitialCommand;
		if (initialCommand == null) {
			initialCommand = DEFAULT_INITIAL_COMMAND;
		}
		if (initialCommand.length() > 0) {
			write(initialCommand + '\r');
		}
	}

	private void restart() {
		startActivity(getIntent());
		finish();
	}

	private void write(String data) {
		try {
			byte[] data1 = data.getBytes();
			mTermOut.write(data1);
			mTermOut.flush();
			//mEmulatorView.write(data1, data1.length);
		} catch (IOException e) {
			// Ignore exception
			// We don't really care if the receiver isn't listening.
			// We just make a best effort to answer the query.
		}
	}


	@Override
	public void onPause() {

		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mEmulatorView.updateSize();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		return super.onKeyUp(keyCode, event);

	}

}