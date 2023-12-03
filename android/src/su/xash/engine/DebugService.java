package su.xash.engine;
import android.util.*;
import android.os.*;
import android.content.*;
import android.view.View;
import android.widget.LinearLayout.*;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import su.xash.fwgslib.FWGSLib;
import su.xash.fwgslib.TermView;
import su.xash.fwgslib.FloatingLayout;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;
import android.view.KeyEvent;

public class DebugService extends XashService
{
	private TermView mTermView;
	private FloatingLayout mLayout;
	private int mPid;
	@Override
	public Intent getNotificationIntent()
	{
		Intent r = new Intent(this, XashActivity.class);
		r.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		return r;
	}
	@Override
	public Intent getExitIntent()
	{
		return new Intent(this, ExitButtonListener.class);
	}

	public int getExtraId()
	{
		return 1;
	}
	@Override
	public void onTaskRemoved(Intent rootIntent) 
	{
		Log.e("DebugService", "OnTaskRemoved");
	}

	@Override
	void startAction(Intent intent)
	{
		mPid = intent.getIntExtra("PID",0);
		Log.i("DebugService", "DebugService started for pid "+ mPid +"!");
		XashService.not.setText("XashDebug");
		mTermView = new TermView(this);
		FloatingLayout layout = new FloatingLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		EditText test = new EditText(this);
		 
		layout.addView(mTermView);
		//layout.addView(test);
		LayoutParams maxweight = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		maxweight.weight = 1;
		mTermView.setLayoutParams(maxweight);
		test.setLayoutParams(maxweight);
		LinearLayout footer = new LinearLayout(this);
		footer.setOrientation(LinearLayout.HORIZONTAL);
		Button btn;
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("M");
		btn.setOnTouchListener(new View.OnTouchListener(){
			@Override
					public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
					mLayout.mMoveMode = 1;
				return false;
			}
		});
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("C");
		btn.setOnClickListener(new View.OnClickListener(){
			@Override 
					public void onClick(View v)
			{
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(mTermView.getSelectedText(true));
			}
		});
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("P");
		btn.setOnClickListener(new View.OnClickListener(){
			@Override 
					public void onClick(View v)
			{
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				mTermView.sendText(clipboard.getText());
			}
		});
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("T");
		btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				mTermView.sendText("\t");
			}
		});
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("I");
		btn.setOnClickListener(new View.OnClickListener(){
			@Override 
					public void onClick(View v)
			{
				android.os.Process.sendSignal(mPid, 2); // SIGINT 
			}
		});
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("^");
		footer.addView(btn);
		btn.setOnClickListener(new View.OnClickListener(){
			@Override 
			public void onClick(View v)
			{
				mTermView.handleDPad(KeyEvent.KEYCODE_DPAD_UP, true);
			}
		});
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("v");
		btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				mTermView.handleDPad(KeyEvent.KEYCODE_DPAD_DOWN, true);
			}
		});
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("<");
		btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				mTermView.handleDPad(KeyEvent.KEYCODE_DPAD_LEFT, true);
			}
		});
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText(">");
		btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				mTermView.handleDPad(KeyEvent.KEYCODE_DPAD_RIGHT, true);
			}
		});
		footer.addView(btn);
		btn = new Button(this);
		btn.setLayoutParams(maxweight);
		btn.setText("R");
		btn.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.	ACTION_DOWN)
					mLayout.mMoveMode = 2;
				return false;
			}
		});
		footer.addView(btn);

		footer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		layout.addView(footer);
		mLayout = layout;

		java.lang.Process process;
		try {
			SharedPreferences pref = getSharedPreferences( "engine", 0 );
			String command = pref.getString("gdb_command", "cat 2>&1").replace( "{GDB}", "/data/data/su.xash.engine/files/gdb32" ).replace( "{PID}", String.valueOf( mPid )).replace( "{APP_PROCESS}", FWGSLib.getAppProcessPath( mPid ));
			process = Runtime.getRuntime().exec( new String[]{ "/system/bin/sh", "-c", command });
			OutputStream termOut = process.getOutputStream();
			InputStream termIn = process.getInputStream();
	
			mTermView.initialize(termIn, termOut);
			mTermView.setTextSize(18);
			mTermView.updateSize();
			mTermView.setFocusable(true);
			mTermView.setFocusableInTouchMode(true);
			mTermView.requestFocus();
			//InputMethodManager imm = ( InputMethodManager )getSystemService( Context.INPUT_METHOD_SERVICE );
			//imm.showSoftInput( mTermView, 0 );
		}
		catch(Exception e){ e.printStackTrace();}

	}
}