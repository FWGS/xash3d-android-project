package su.xash.engine;
import android.util.*;
import android.os.*;
import android.content.*;
import android.view.WindowManager;
import android.view.View;
import android.widget.LinearLayout.*;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import su.xash.fwgslib.TermView;
import android.graphics.PixelFormat;
import android.view.Gravity;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;

class FloatingLayout extends LinearLayout
{
	FloatingLayout(Context ctx)
	{
		super(ctx);
	}
	private int mX, mY, mX1, mY1;
	public void updatePos()
	{
		int[] loc= new int[2];
		getLocationOnScreen(loc);
		mX = loc[0];
		mY = loc[1];
		mX1 = loc[0] + getWidth();
		mY1 = loc[1] + getHeight();
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		updatePos();
		int x = (int)event.getRawX();
		int y = (int)event.getRawY();
		boolean inside = x >= mX && x <= mX1 && y >= mY && y <= mY1;
		DebugService.gParams.flags = inside ? 0 :WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; 
		DebugService.gWindowManager.updateViewLayout(this, DebugService.gParams);
		Log.d("inside", "i " + inside);
		
		return false;
	}
}

public class DebugService extends XashService
{
	public static WindowManager gWindowManager;
	public static WindowManager.LayoutParams gParams;
	private TermView mTermView;
	private FloatingLayout mLayout;
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
	private void createLayout()
	{
		
	}

	@Override
	void startAction(Intent intent)
	{
		Log.i("DebugService", "DebugService started for pid "+ intent.getStringExtra("PID")+"!");
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
		mLayout = layout;

		gWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		gParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, 0, PixelFormat.OPAQUE);
		gParams.gravity = Gravity.LEFT | Gravity.TOP;
		gParams.x = 10;
		gParams.y = 20;
		gParams.height = 240;
		gParams.width = 240;
		gWindowManager.addView(layout, gParams);
		java.lang.Process process;
		try {
			process = Runtime.getRuntime().exec(new String[]{"/system/bin/sh","-c","TERM=vt100 cat 2>&1"});
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