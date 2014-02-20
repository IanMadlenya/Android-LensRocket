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

public final class Constants {
	public static short MIN_PASSWORD_LENGTH = 7;
	public static short MIN_USERNAME_LENGTH = 4;
	
	public static int MEDIA_TYPE_IMAGE = 1;
	public static int MEDIA_TYPE_VIDEO = 2;
	
	public static String BROADCAST_FRIENDS_UPDATED = "friends.updated";
	public static String BROADCAST_ROCKETS_UPDATED = "rockets.updated";
	public static String BROADCAST_ROCKET_SENT = "rocket.sent";
	public static String BROADCAST_USER_PREFERENCES_UPDATED = "rocket.sent";
	
	public static String FRIENDS_UPDATE_STATUS = "friends.update.status";
	public static String ROCKETS_UPDATE_STATUS = "rockets.update.status";
	
	public static String MOBILE_SERVICE_URL = "https://<YOUR-MOBLIE-SERVICE-NAME>.azure-mobile.net/";
	public static String MOBILE_SERVICE_APPLICATION_KEY = "<YOUR-MOBILE-SERVICE-APPLICATION-KEY>";
	//Push notifications
	public static String SENDER_ID = "<YOUR-GCM-SENDER-ID>";
	public static String NOTIFICATION_HUB_CONNECTION_STRING = "<YOUR-NOTIFICATION-HUB-LISTEN-SHARED-ACCESS-SIGNATURE>";
	public static String NOTIFICATIN_HUB_NAME = "<YOUR-NOTIFICATOIN-HUB-NAME>";	
	
	public static int REQUEST_CODE_SEND_TO_FRIENDS = 1001;
		
	public static int RESULT_CODE_ROCKET_SENT = 9009;
	
	// Enter the urls for your own T&C and Privacy pages here
	public static String TERMS_AND_CONDITIONS_URL = "http://lensrocket.azurewebsites.net/terms.html";
	public static String PRIVACY_POLICY_URL = "http://lensrocket.azurewebsites.net/privacy.html";
	
	public static enum CameraUIMode {
        UI_MODE_PRE_PICTURE,
		UI_MODE_TAKING_PICTURE,
		UI_MODE_REVIEW_PICTURE,
		UI_MODE_REVIEW_VIDEO,
		UI_MODE_TAKING_VIDEO,
		UI_MODE_REPLYING
    }
	
	public static enum RocketType {
		ROCKET_TYPE_FRIEND_REQUEST_ACCEPTED,
		ROCKET_TYPE_FRIEND_REQUEST_UNACCEPTED,
		ROCKET_TYPE_ROCKET_SEEN,
		ROCKET_TYPE_ROCKET_UNSEEN,
	}
	
	/** This constant is used to keep track of all Galaxy S3 variants **/
	public static String s3ModelNames[] = { "XXXXXXXXXXXXXXXX", // Place holder
            "SAMSUNG-SGH-I747", // AT&T
            "SAMSUNG-SGH-T999", // T-Mobile
            "SAMSUNG-SGH-N064", // Japan
            "SAMSUNG-SCH-R530", // US Cellular
            "SAMSUNG-SCH-I535", // Verizon
            "SAMSUNG-SPH-L710", // Sprint
            "SAMSUNG-GT-I9300", // International
            "SGH-I747", // AT&T
            "SGH-T999", // T-Mobile
            "SGH-N064", // Japan
            "SCH-R530", // US Cellular
            "SCH-I535", // Verizon
            "SPH-L710", // Sprint
            "GT-I9300"  // International
   };
}
