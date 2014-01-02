package com.msted.pikshare;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.msted.pikshare.R;
import com.msted.pikshare.util.PikShareLogger;

public class PikShareBroadcastReceiver extends BroadcastReceiver {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private Context mContext;
	private final String TAG = "PikShareBroadcastReceiver";
	private PikShareService mPikShareService;
	private PikShareApplication mPikShareApplication;

	@Override
	public void onReceive(Context context, Intent intent) {				
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        mContext = context;
        mPikShareApplication = (PikShareApplication) mContext.getApplicationContext(); 
		mPikShareService = mPikShareApplication.getPikShareService();
        
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            sendNotification("Send error: " + intent.getExtras().toString());
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages on server: " + 
                    intent.getExtras().toString());
        } else {
        		PikShareLogger.i(TAG, "Message: " + intent.getStringExtra("message"));
        		String message = intent.getStringExtra("message");
        		processPush(message);
        }
        setResultCode(Activity.RESULT_OK);
	}
	
	private void processPush(String message) {
		PikShareLogger.i(TAG, "Process Push");
		if (message.equals("Friend request received")) {
			PikShareLogger.i(TAG, "FRR");
			if (mPikShareApplication.getIsApplicationActive()) {
				PikShareLogger.i(TAG, "GF");
				mPikShareService.getPiks();
				//TODO:  If we show friend requests in the Friends list, we can 
				//uncomment this to trigger a refresh
				//mPikShareService.getFriends();
			}
			else {
				sendNotification("Friend request received");
			}
		} else if (message.equals("Pik received")) {
			if (mPikShareApplication.getIsApplicationActive())
				mPikShareService.getPiks();
			else {
				sendNotification("Pik received");
			}
		} else {
			sendNotification("Message received: " + message);
		}
	}
	
	private void sendNotification(String msg) {			
		mNotificationManager = (NotificationManager)
	              mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Activity activity = (Activity) mPikShareService.getActivityContext();
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				new Intent(activity, Activity.class), 0);
		PikShareLogger.i(TAG, "Push received: " + msg);
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(mContext)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("PikShare Message")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);
		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}
