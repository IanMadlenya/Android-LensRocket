package com.msted.pikshare;

public final class Constants {
	public static short MIN_PASSWORD_LENGTH = 7;
	public static short MIN_USERNAME_LENGTH = 4;
	
	public static int MEDIA_TYPE_IMAGE = 1;
	public static int MEDIA_TYPE_VIDEO = 2;
	
	public static String BROADCAST_FRIENDS_UPDATED = "friends.updated";
	public static String BROADCAST_PIKS_UPDATED = "piks.updated";
	public static String BROADCAST_PIK_SENT = "pik.sent";
	public static String BROADCAST_USER_PREFERENCES_UPDATED = "pik.sent";
	
	public static String FRIENDS_UPDATE_STATUS = "friends.update.status";
	public static String PIKS_UPDATE_STATUS = "piks.update.status";
	
	public static String MOBILE_SERVICE_URL = "https://<YOUR-MOBLIE-SERVICE-NAME>.azure-mobile.net/";
	public static String MOBILE_SERVICE_APPLICATION_KEY = "<YOUR-MOBILE-SERVICE-APPLICATION-KEY>";
	
	public static int REQUEST_CODE_SEND_TO_FRIENDS = 1001;
		
	public static int RESULT_CODE_PIK_SENT = 9009;
	
	//Push notifications
	public static String SENDER_ID = "<YOUR-GCM-SENDER-ID>";
	public static String NOTIFICATION_HUB_CONNECTION_STRING = "<YOUR-NOTIFICATION-HUB-LISTEN-SHARED-ACCESS-SIGNATURE>";
	public static String NOTIFICATIN_HUB_NAME = "<YOUR-NOTIFICATOIN-HUB-NAME>";
	
	public static enum CameraUIMode {
        UI_MODE_PRE_PICTURE,
		UI_MODE_TAKING_PICTURE,
		UI_MODE_REVIEW_PICTURE,
		UI_MODE_REVIEW_VIDEO,
		UI_MODE_TAKING_VIDEO,
		UI_MODE_REPLYING
    }
	
	public static enum PikType {
		PIK_TYPE_FRIEND_REQUEST_ACCEPTED,
		PIK_TYPE_FRIEND_REQUEST_UNACCEPTED,
		PIK_TYPE_PIK_SEEN,
		PIK_TYPE_PIK_UNSEEN,
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
