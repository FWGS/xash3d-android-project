package su.xash.engine;
import android.util.*;
import android.os.*;
import android.content.*;
public class DebugService extends XashService
{
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
	@Override
	void startAction(Intent intent)
	{
		Log.i("DebugService", "DebugService started for pid "+ intent.getStringExtra("PID")+"!");
		XashService.not.setText("XashDebug");
	}
}