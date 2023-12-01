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
import android.graphics.drawable.*;
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
import java.util.List;
import java.security.MessageDigest;

import su.xash.engine.R;
import su.xash.engine.XashConfig;
import su.xash.fwgslib.*;

public class XashService extends Service
{
	public static XashNotification not;
	private static Service instance;

	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
	public Intent getNotificationIntent()
	{
		Intent r = new Intent(this, XashActivity.class);
		r.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		return r;
	}
	public Intent getExitIntent()
	{
		return new Intent(this, ExitButtonListener.class);
	}
	public int getExtraId()
	{
		return 0;
	}
	
	public static void exitAction()
	{
		if( instance!= null )
		{
			FWGSLib.cmp.stopForeground( instance, 2 );
			instance.stopSelf();
		}
		if(not != null)
			not.cancel();
		XashActivity.mEngineReady = false;
		XashBinding.nativeUnPause();
		XashBinding.nativeOnDestroy();
		if( XashActivity.mSurface != null )
			XashActivity.mSurface.engineThreadJoin();
		System.exit(0);
	}

	void startAction(Intent intent){}
	
	public static class ExitButtonListener extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.d("XashService", "Exit requested");
			XashService.exitAction();
		}
	}
	@Override
	public void onStart (Intent intent, 
                int startId)
	{
		Log.d("XashService", "Service Started (compat)");
		if( not != null )
			return;
		instance = this;
		not = XashNotification.getXashNotification(this);
		not.createNotification(this, getNotificationIntent(), getExitIntent(), getExtraId());
		startAction(intent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		Log.d("XashService", "Service Started");
		if( not != null )
			return 0;
		instance = this;
		not = XashNotification.getXashNotification(this);
		FWGSLib.cmp.startForeground(this,not.notificationId, not.createNotification(this, getNotificationIntent(), getExitIntent(), getExtraId()));
		startAction(intent);
		
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		//if(not != null)
			//not.cancel();
		Log.d("XashService", "Service Destroyed");
	}

	@Override
	public void onCreate()
	{
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) 
	{
		Log.e("XashService", "OnTaskRemoved");
		exitAction();
	}

	public static class XashNotification
	{
		public Notification notification;
		public int notificationId = 100;
		protected Context ctx;
		
		protected static NotificationManager nm;
	
		public Notification createNotification(Context context, Intent engineIntent, Intent exitIntent, int extraId)
		{
			ctx = context;
			notificationId += extraId;

			final PendingIntent pendingExitIntent = PendingIntent.getBroadcast(ctx, 0, exitIntent, 0);

			notification = new Notification(R.drawable.ic_statusbar, ctx.getString(R.string.app_name), System.currentTimeMillis());
			
			notification.contentView = new RemoteViews(ctx.getApplicationContext().getPackageName(), R.layout.notify);
			notification.contentView.setTextViewText(R.id.status_text, ctx.getString(R.string.app_name));
			notification.contentView.setOnClickPendingIntent(R.id.status_exit_button, pendingExitIntent);

			notification.contentIntent = PendingIntent.getActivity(ctx.getApplicationContext(), 0, engineIntent, 0);
			notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE;
			
			return notification;
		}
		
		public void setIcon(Bitmap bmp)
		{
			notification.contentView.setImageViewBitmap( R.id.status_image, bmp );
			nm.notify( notificationId, notification );
		}
		
		public void setText(String title)
		{
			notification.contentView.setTextViewText( R.id.status_text, title );
			nm.notify( notificationId, notification );
		}

		public void cancel()
		{
			nm.cancel(notificationId);
		}
		
		public static XashNotification getXashNotification(Context ctx)
		{
			nm = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			if( FWGSLib.sdk >= 26 )
				return new XashNotification_v26();
			else  if( FWGSLib.sdk >= 23 )
				return new XashNotification_v23();
			else
				return new XashNotification();
		}

	}
	
	private static class XashNotification_v23 extends XashNotification
	{
		protected Notification.Builder builder;
		
		@Override
		public Notification createNotification(Context context, Intent engineIntent, Intent exitIntent, int extraId)
		{
			ctx = context;
			notificationId += extraId;

			final PendingIntent pendingExitIntent = PendingIntent.getBroadcast(ctx, 0, exitIntent, 0);

			if(builder == null)
				builder = new Notification.Builder(ctx);
			
			notification = builder.setSmallIcon(R.drawable.ic_statusbar)
				.setLargeIcon(Icon.createWithResource(ctx, R.mipmap.ic_launcher))
				.setContentTitle(ctx.getString(R.string.app_name))
				.setContentText(ctx.getString(R.string.app_name))
				.setContentIntent(PendingIntent.getActivity(ctx.getApplicationContext(), 0, engineIntent, 0))
				.addAction(new Notification.Action.Builder(R.drawable.empty, ctx.getString(R.string.exit), pendingExitIntent).build())
				.setOngoing(true)
				.build();
			
			return notification;
		}
		
		@Override
		public void setIcon(Bitmap bmp)
		{
			notification = builder.setLargeIcon(bmp).build();		
			nm.notify( notificationId, notification );
		}
		
		@Override
		public void setText(String str)
		{
			notification = builder.setContentText(str).build();
			nm.notify( notificationId, notification );
		}
	}


	private static class XashNotification_v26 extends XashNotification_v23
	{
		private String CHANNEL_ID = "XashServiceChannel";
	
		private void createNotificationChannel()
		{
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			
			if(nm.getNotificationChannel(CHANNEL_ID) == null)
			{
				CharSequence name = ctx.getString(R.string.default_channel_name);
				String description = ctx.getString(R.string.default_channel_description);
				int importance = NotificationManager.IMPORTANCE_LOW;
			 
				NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
				channel.setDescription(description);
			
				// Register the channel with the system; you can't change the importance
				// or other notification behaviors after this
			
				nm.createNotificationChannel(channel);
			}
		}
		
		@Override
		public Notification createNotification(Context context, Intent mainIntent, Intent exitIntent, int extraId)
		{
			CHANNEL_ID = CHANNEL_ID + extraId;
			ctx = context;
			createNotificationChannel();
		
			builder = new Notification.Builder(ctx);
			builder.setChannelId(CHANNEL_ID);
			
			return super.createNotification(ctx, mainIntent, exitIntent, extraId);
		}
	}
};
