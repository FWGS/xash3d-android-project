package su.xash.fwgslib;
import android.view.View;
import android.view.ViewTreeObserver;
import android.graphics.Rect;
import android.widget.FrameLayout;
import android.app.Activity;
import android.util.Log;

public class AndroidBug5497Workaround 
{
	// For more information, see https://code.google.com/p/android/issues/detail?id=5497
	// To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

	private View mChildOfContent;
	private int usableHeightPrevious;
	private FrameLayout.LayoutParams frameLayoutParams;

	public AndroidBug5497Workaround( Activity activity ) 
	{
		FrameLayout content = ( FrameLayout )activity.findViewById( android.R.id.content );
		mChildOfContent = content.getChildAt( 0 );
		mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener( ) 
		{
			public void onGlobalLayout() 
			{
				possiblyResizeChildOfContent();
			}
		});
		frameLayoutParams = ( FrameLayout.LayoutParams )mChildOfContent.getLayoutParams();
	}
	public void onKeyboardToggle(boolean visible, int heightUsable, int heightDiff)
	{
		Log.d("AndroidBug5497Workaround", "visible:" + visible);
	}

	private void possiblyResizeChildOfContent() 
	{
		int usableHeightNow = computeUsableHeight();
		if( usableHeightNow != usableHeightPrevious ) 
		{
			int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
			int heightDifference = usableHeightSansKeyboard - usableHeightNow;
			boolean visible = heightDifference > ( usableHeightSansKeyboard / 4 );
			if( visible ) 
			{
				// keyboard probably just became visible
				frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
			} 
			else 
			{
				// keyboard probably just became hidden
				frameLayoutParams.height = usableHeightSansKeyboard;
			}

			
			mChildOfContent.requestLayout();
			onKeyboardToggle(visible, usableHeightSansKeyboard, heightDifference);
			usableHeightPrevious = usableHeightNow;
		}
	}
	
	private int computeUsableHeight() 
	{
		Rect r = new Rect();
		mChildOfContent.getWindowVisibleDisplayFrame( r );
		return r.bottom - r.top;
	}
}
