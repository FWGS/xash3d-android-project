package su.xash.fwgslib;
import android.view.WindowManager;
import android.view.View;
import android.content.Context;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.util.Log;


public class FloatingLayout extends LinearLayout
{
	public static WindowManager gWindowManager;
	private WindowManager.LayoutParams mParams;
	public FloatingLayout(Context ctx)
	{
		super(ctx);
		if(gWindowManager == null)
			gWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, 0, PixelFormat.OPAQUE);
		mParams.gravity = Gravity.LEFT | Gravity.TOP;
		mParams.x = 10;
		mParams.y = 20;
		mParams.height = 240;
		mParams.width = 240;
		gWindowManager.addView(this, mParams);
	}
	private int mX, mY, mX1, mY1;
	private int mLX, mLY;
	private boolean mInside = true;
	public int mMoveMode = 1;
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
		int action = event.getAction();
		if(mMoveMode > 0 && action == MotionEvent.ACTION_MOVE)
		{
			int x = (int)event.getRawX();
			int y  = (int)event.getRawY();
			int dx = x - mLX;
			int dy = y - mLY;
			mLX = x;
			mLY = y;
			if(mMoveMode == 1)
			{
				mParams.x += dx;
				mParams.y += dy;
			}
			else if(mMoveMode == 2)
			{
				mParams.width += dx;
				mParams.height += dy;
				if(mParams.width < 200)
					mParams.width = 200;
				if(mParams.height < 50)
					mParams.height = 50;
			}
			gWindowManager.updateViewLayout(this, mParams);
			updatePos();
			return false;
		}
		if(action == MotionEvent.ACTION_UP)
			mMoveMode = 0;
		if(action != MotionEvent.ACTION_DOWN)
			return false;
		else
		{
			if(mX1 == 0)
				updatePos();
			int x = (int)event.getRawX();
			int y = (int)event.getRawY();
			mLX = x;
			mLY = y;
			boolean inside = x >= mX && x <= mX1 && y >= mY && y <= mY1;
			if(inside == mInside)
				return false;
			mInside = inside;
			mParams.flags = inside ? 0 :WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; 
			gWindowManager.updateViewLayout(this, mParams);
			Log.d("inside", "i " + inside);
			
			return false;
		}
	}
}