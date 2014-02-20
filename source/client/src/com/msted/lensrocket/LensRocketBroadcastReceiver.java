/*
Created by Chris Risner
Copyright (c) Microsoft Corporation
All Rights Reserved
Apache 2.0 License
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */

package com.msted.lensrocket;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.msted.lensrocket.util.LensRocketLogger;
import com.msted.lensrocket.R;

public class LensRocketBroadcastReceiver extends BroadcastReceiver {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private Context mContext;
	private final String TAG = "LensRocketBroadcastReceiver";
	private LensRocketService mLensRocketService;
	private LensRocketApplication mLensRocketApplication;

	@Override
	public void onReceive(Context context, Intent intent) {				
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        mContext = context;
        mLensRocketApplication = (LensRocketApplication) mContext.getApplicationContext(); 
		mLensRocketService = mLensRocketApplication.getLensRocketService();
        
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            sendNotification("Send error: " + intent.getExtras().toString());
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages on server: " + 
                    intent.getExtras().toString());
        } else {
        		LensRocketLogger.i(TAG, "Message: " + intent.getStringExtra("message"));
        		String message = intent.getStringExtra("message");
        		processPush(message);
        }
        setResultCode(Activity.RESULT_OK);
	}
	
	private void processPush(String message) {
		LensRocketLogger.i(TAG, "Process Push");
		if (message.equals("Friend request received")) {
			LensRocketLogger.i(TAG, "FRR");
			if (mLensRocketApplication.getIsApplicationActive()) {
				LensRocketLogger.i(TAG, "GF");
				mLensRocketService.getRockets();
				//TODO:  If we show friend requests in the Friends list, we can 
				//uncomment this to trigger a refresh
				//mLensRocketService.getFriends();
			}
			else {
				sendNotification("Friend request received");
			}
		} else if (message.equals("Rocket received")) {
			if (mLensRocketApplication.getIsApplicationActive())
				mLensRocketService.getRockets();
			else {
				sendNotification("Rocket received");
			}
		} else {
			sendNotification("Message received: " + message);
		}
	}
	
	private void sendNotification(String msg) {			
		mNotificationManager = (NotificationManager)
	              mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Activity activity = (Activity) mLensRocketService.getActivityContext();
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				new Intent(activity, Activity.class), 0);
		LensRocketLogger.i(TAG, "Push received: " + msg);
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(mContext)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("LensRocket Message")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);
		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}
