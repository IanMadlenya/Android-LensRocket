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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.StatusLine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponseCallback;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;
import com.msted.lensrocket.activities.SplashScreenActivity;
import com.msted.lensrocket.datamodels.Friend;
import com.msted.lensrocket.datamodels.Rocket;
import com.msted.lensrocket.datamodels.RocketFile;
import com.msted.lensrocket.datamodels.UserPreferences;
import com.msted.lensrocket.util.NetworkUtilities;
import com.msted.lensrocket.util.NoNetworkConnectivityException;
import com.msted.lensrocket.util.LensRocketAlert;
import com.msted.lensrocket.util.LensRocketLogger;
import com.msted.lensrocket.util.LensRocketRegisterResponse;
import com.msted.lensrocket.util.LensRocketResponse;
import com.msted.lensrocket.R;

public class LensRocketService {		
	private final String 		TAG = "LensRocketService";
	private Context 				mContext;
	private String 				mUsername;
	private String 				mEmail;	
	private String[] 			mTempRecipientUserIds;
	private GoogleCloudMessaging mGcm;
	private NotificationHub      mHub;
	private String 				mRegistrationId;
	private UserPreferences 		mUserPrefs;
	private UserPreferences 		mBackupPrefs;
	private int     				mCheckCount;
	
	//Mobile Services objects
	private MobileServiceClient 					mClient;
	private MobileServiceTable<Friend> 			mFriendTable;	
	private MobileServiceTable<Rocket> 				mRocketTable;
	private MobileServiceTable<RocketFile> 			mRocketFileTable;
	private MobileServiceTable<UserPreferences> 	mUserPreferencesTable;
	
	//Local data
	private List<Friend> 	mFriends;
	private List<String> 	mFriendNames;
	private List<Rocket> 		mRockets;
	
	/***************************************************************/
	/** Constructors **/
	/***************************************************************/
	
	public LensRocketService(Context context) {
		mContext = context;
		try {
			mClient = new MobileServiceClient(Constants.MOBILE_SERVICE_URL,					
					Constants.MOBILE_SERVICE_APPLICATION_KEY, mContext)
					.withFilter(new MyServiceFilter());
			
			mFriendTable = mClient.getTable("Friends", Friend.class);
			mRocketTable = mClient.getTable("Messages", Rocket.class);
			mRocketFileTable = mClient.getTable("RocketFile", RocketFile.class);
			mUserPreferencesTable = mClient.getTable(UserPreferences.class);
			
			mFriends = new ArrayList<Friend>();
			mFriendNames = new ArrayList<String>();
			mRockets = new ArrayList<Rocket>();
			
			mCheckCount = 0;
		} catch (MalformedURLException e) {
			Log.e(TAG, "There was an error creating the Mobile Service.  Verify the URL");
		}
	}
	
	/***************************************************************/
	/** Auth Methods **/
	/***************************************************************/
	
	/**
	 * Checks to see if we have userId and token stored on the device and sets them if so
	 * @return
	 */
	public boolean isUserAuthenticated() {			
		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
		if (settings != null) {
			String userId = settings.getString("userid", null);
			String token = settings.getString("token", null);
			String username = settings.getString("username", null);
			String email = settings.getString("email", null);
			String registrationId = settings.getString("registrationId", null);
			if (userId != null && !userId.equals("")) {
				setUserData(userId, token, username, email, registrationId);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a new MobileServiceUser using a userId and token passed in.
	 * Also sets the current provider
	 * @param userId
	 * @param token
	 */
	public void setUserData(String userId, String token, String username, String email, String registrationId) {
		MobileServiceUser user = new MobileServiceUser(userId);
		user.setAuthenticationToken(token);
		mClient.setCurrentUser(user);		
		mUsername = username;
		mEmail = email;
		mRegistrationId = registrationId;
	}
	
	/***
	 * Pulls the user ID and token out of a json object from the server
	 * @param jsonObject
	 */
	public void setUserAndSaveData(JsonElement jsonData) {
		JsonObject userData = jsonData.getAsJsonObject();
		String userId = userData.get("userId").getAsString();
		String token = userData.get("token").getAsString();
		String email = userData.get("email").getAsString();
		setUserData(userId, token, null, email, null);	
		saveUserData();
	}
	
	public void setUserAndSaveData(LensRocketRegisterResponse registerData) {
		String userId = registerData.userId;
		String token = registerData.token;		
		String username = registerData.username;
		String email = registerData.email;
		setUserData(userId, token, username, email, null);	
		saveUserData();
	}
	
	/**
	 * Saves userId and token to SharedPreferences.
	 * NOTE:  This is not secure and is just used as a storage mechanism.  In reality, you would want to 
	 * come up with a more secure way of storing this information.
	 */
	public void saveUserData() {
		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putString("userid", mClient.getCurrentUser().getUserId());
        preferencesEditor.putString("token", mClient.getCurrentUser().getAuthenticationToken());
        preferencesEditor.putString("username", mUsername);
        preferencesEditor.putString("email", mEmail);
        preferencesEditor.commit();
	}
	
	/**
	 * Saves username SharedPreferences.
	 * NOTE:  This is not secure and is just used as a storage mechanism.  In reality, you would want to 
	 * come up with a more secure way of storing this information.
	 */
	public void saveUsername(String username) {
		mUsername = username;
		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putString("username", username);        
        preferencesEditor.commit();
	}
	
	/**
	 * Register the user if they're creating a custom auth account
	 * @param password
	 * @param email
	 * @param dob
	 * @param callback
	 */
	public void registerUser(String password, String dob,
			String email,
			ApiOperationCallback<LensRocketRegisterResponse> callback) {
		JsonObject newUser = new JsonObject();
		newUser.addProperty("password", password);
		newUser.addProperty("email", email);
		newUser.addProperty("dob", dob);			
		mClient.invokeApi("Register", newUser, LensRocketRegisterResponse.class, callback);
	}
	
	public void loginUser(String emailOrUsername, String password, ApiOperationCallback<LensRocketRegisterResponse> callback) {
		JsonObject user = new JsonObject();
		user.addProperty("emailOrUsername", emailOrUsername);
		user.addProperty("password", password);
		mClient.invokeApi("Login", user, LensRocketRegisterResponse.class, callback);
	}
	
	public void saveUsername(String username, ApiOperationCallback<LensRocketResponse> callback) {
		JsonObject user = new JsonObject();
		user.addProperty("username", username);
		user.addProperty("email", mEmail);
		mClient.invokeApi("SaveUsername", user, LensRocketResponse.class, callback);
	}
	
	public void logout(boolean shouldRedirectToLogin) {
		//Clear values
		mFriendNames.clear();
		mFriends.clear();
		mRockets.clear();
		//Clear the cookies so they won't auto login to a provider again
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();			
		//Clear the user id and token from the shared preferences
		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.clear();
        preferencesEditor.commit();	
        
        //Clear settings shared preferences
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (settingsPrefs != null) {
        		preferencesEditor = settingsPrefs.edit();
        		preferencesEditor.clear();
        		preferencesEditor.commit();
        }
		mClient.logout();			
		//Take the user back to the splash screen activity to relogin if requested
		if (shouldRedirectToLogin) {
			Intent logoutIntent = new Intent(mContext, SplashScreenActivity.class);
			logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(logoutIntent);		
		}
	}
	
	/***************************************************************/
	/** Local Properties**/
	/***************************************************************/
	
	public void setContext(Context context) {
		mClient.setContext(context);
	}
	
	public UserPreferences getLocalPreferences() { return mUserPrefs; }
 	public UserPreferences getBackupPreferences() { return mBackupPrefs; }
	
	public String getUserId() {
		return mClient.getCurrentUser().getUserId();
	}
	
	public String getUsername() {
		return mUsername; 
	}
	
	public String getEmail() {
		return mEmail;
	}
	
	public List<Friend> getLocalFriends() {
		return mFriends;
	}

	public List<Rocket> getLocalRockets() {
		return mRockets;
	}
	
	public List<String> getLocalRocketUsernames() {
		List<String> mRocketNames = new ArrayList<String>();
		for (int i = 0; i < mRockets.size(); i++) {
			mRocketNames.add(mRockets.get(i).getFromUsername() + ":ttl: " + mRockets.get(i).getTimeToLive());
		}
		return mRocketNames;
	}
	
	public List<String> getLocalFriendNames() {
		mFriendNames = new ArrayList<String>();
		LensRocketLogger.i(TAG, "Processing " + mFriends.size() + " friends");
		for (int i = 0; i < mFriends.size(); i++) {
			mFriendNames.add(mFriends.get(i).getToUsername());
		}
		return mFriendNames;
	}
	
	public void increaseCheckCount() { mCheckCount++; }
	public void decreaseCheckCount() { mCheckCount--; }
	public int  getCheckCount() { return mCheckCount; }
	public void uncheckFriends() {
		mCheckCount = 0;
		for (Friend friend : mFriends) {
			friend.setChecked(false);
		}
	}
	
	public Activity getActivityContext() {
		return (Activity) mClient.getContext();
	}
	
	/***************************************************************/
	/** Friends **/
	/***************************************************************/
	
	public void acceptFriendRequest(Rocket friendRequestRocket, 
			ApiOperationCallback<LensRocketResponse> callback) {		
		mClient.invokeApi("AcceptFriendRequest", friendRequestRocket, LensRocketResponse.class, callback);
	}
	
	public void requestFriend(String username, ApiOperationCallback<LensRocketRegisterResponse> callback) {
		JsonObject friendRequest = new JsonObject();	
		friendRequest.addProperty("username", username);
		mClient.invokeApi("RequestFriend", friendRequest, LensRocketRegisterResponse.class, callback);
	}
	
	public void getFriends() {
		mFriendTable.where().execute(new TableQueryCallback<Friend>() {			
			@Override
			public void onCompleted(List<Friend> results, int count, Exception ex,
					ServiceFilterResponse response) {
				boolean wasSuccess = false;
				if (ex != null) {
					if (NoNetworkConnectivityException.class.isInstance(ex))
						return;
					LensRocketLogger.e(TAG, "Error getting friends: " + ex.getCause().getMessage());
				} else {
					LensRocketLogger.i(TAG, "Friends received");
					wasSuccess = true;
					mFriends = results;
					//Insert self as friend
					Friend self = Friend.getSelfFriend(mUsername, mClient.getCurrentUser().getUserId());
					mFriends.add(0, self);					
					//Broadcast that we've updated our friends list
					Intent broadcast = new Intent();
					broadcast.putExtra(Constants.FRIENDS_UPDATE_STATUS, wasSuccess);
					broadcast.setAction(Constants.BROADCAST_FRIENDS_UPDATED);
					mContext.sendBroadcast(broadcast);					
				}				
			}
		});
	}
	
	/***************************************************************/
	/** Rockets **/
	/***************************************************************/
	
	public void sendRocketsToRecipients(Rocket sentRocket, String[] recipientUserIds,
 			RocketFile savedRocketFile) {
		JsonObject sendRocketsRequest = new JsonObject();
		String serializedRecipients = new Gson().toJson(recipientUserIds);
		LensRocketLogger.d(TAG, "Recipients: " + serializedRecipients);
		if (recipientUserIds.length == 0) {
			LensRocketLogger.e(TAG, "There are no recipient user ids.  INVESTIGATE!");
		}
		
		sendRocketsRequest.add("recipients", new JsonPrimitive(serializedRecipients));
		sendRocketsRequest.addProperty("timeToLive", sentRocket.getTimeToLive());
		sendRocketsRequest.addProperty("fromUserId", sentRocket.getFromUserId());
		sendRocketsRequest.addProperty("fromUsername", sentRocket.getFromUsername());
		sendRocketsRequest.addProperty("isPicture", sentRocket.getIsPicture());
		sendRocketsRequest.addProperty("isVideo", sentRocket.getIsVideo());
		sendRocketsRequest.addProperty("originalSentRocketId", sentRocket.getId());
		sendRocketsRequest.addProperty("rocketFileId", savedRocketFile.getId());
		mClient.invokeApi("SendRocketToFriends", sendRocketsRequest, LensRocketResponse.class, new ApiOperationCallback<LensRocketResponse>() {
			@Override
			public void onCompleted(LensRocketResponse response, Exception ex,
					ServiceFilterResponse serviceFilterResponse) {
				//callback: broadcast to receiver that messages sent
				Intent broadcast = new Intent();
				broadcast.setAction(Constants.BROADCAST_ROCKET_SENT);
				
				if (ex != null || response.Error != null) {										
					//Display error						
					if (ex != null) {
						if (NoNetworkConnectivityException.class.isInstance(ex))
							return;
						LensRocketLogger.e(TAG, "Unexpected error sending rockets: " + ex.getCause().getMessage());
					}
					else 
						LensRocketLogger.e(TAG,  "Error sending rockets: " + response.Error);
					broadcast.putExtra("Success", false);
				} else {
					broadcast.putExtra("Success", true);
				}
				mContext.sendBroadcast(broadcast);		
			}
		});
	}
 	
 	public void getRocketForRecipient(Rocket rocket, ApiOperationCallback<LensRocketResponse> callback) {
 		mClient.invokeApi("getRocketForRecipient", rocket, LensRocketResponse.class, callback);
 	}
	
	public void getRockets() {
		LensRocketLogger.i(TAG, "Getting rockets from server");
		mRocketTable.where().execute(new TableQueryCallback<Rocket>() {			
			@Override
			public void onCompleted(List<Rocket> results, int count, Exception ex,
					ServiceFilterResponse response) {
				boolean wasSuccess = false;
				if (ex != null) {
					if (NoNetworkConnectivityException.class.isInstance(ex))
						return;
					LensRocketLogger.e(TAG, "Error getting rocket: " + ex.getCause().getMessage());
				} else {
					LensRocketLogger.i(TAG, "Rockets received");
					wasSuccess = true;
					mRockets = results;														
				}	
				LensRocketLogger.i(TAG, "Sending broadcast");
				Intent broadcast = new Intent();
				broadcast.putExtra(Constants.ROCKETS_UPDATE_STATUS, wasSuccess);
				broadcast.setAction(Constants.BROADCAST_ROCKETS_UPDATED);
				mContext.sendBroadcast(broadcast);	
			}
		});
	}
	
	
	
	public boolean sendRocket(final String fileFullPath, final boolean isPicture, final boolean isVideo, int selectedSeconds) {
		if (!NetworkUtilities.isNetworkOnline(mContext)) {
			LensRocketAlert.showSimpleErrorDialog(getActivityContext(), "You must be connected to the internet for this to work.");
			return false;
		}
		//Get User IDs
		mTempRecipientUserIds = new String[mCheckCount];		
		int count = 0;
		for (Friend friend : mFriends) {
			if (friend.getChecked())
				mTempRecipientUserIds[count++] = friend.getToUserId();
		}
		//add new message to local rockets
		final Rocket sentRocket = Rocket.newSentRocket(mClient.getCurrentUser().getUserId(), mUsername, selectedSeconds, isPicture, isVideo);
		mRockets.add(0, sentRocket);
		//save new rocket as from
		mRocketTable.insert(sentRocket, new TableOperationCallback<Rocket>() {			
			@Override
			public void onCompleted(final Rocket rocketReturned, Exception ex, ServiceFilterResponse serviceFilterResponse) {
				if (ex != null) {
					LensRocketLogger.e(TAG, "Error inserting rocket: " + ex.getMessage());
				}
				if (rocketReturned == null) {
					LensRocketLogger.i(TAG, "Rocket returned is null");
				} else
					LensRocketLogger.i(TAG, "Rocket returned ID: " + rocketReturned.getId());
				//Todo: check to make sure this works right.  was looking (using indexOf) for the
				//sentRocket object but that occasionally returned a -1 for index
				//Now just assuming we're still dealing with entry 0
				mRockets.set(0, rocketReturned);
				RocketFile rocketFile = new RocketFile(isPicture, isVideo, mUsername, rocketReturned.getId(), fileFullPath);
				mRocketFileTable.insert(rocketFile, new TableOperationCallback<RocketFile>() {					
					@Override
					public void onCompleted(RocketFile rocketFileReturned, Exception ex,
							ServiceFilterResponse serviceFilterResponse) {
						//Upload our file to blob storage
						(new BlobUploaderTask(rocketFileReturned.getBlobPath(), 
								fileFullPath, isPicture, isVideo, 
								mTempRecipientUserIds, rocketReturned, rocketFileReturned)).execute();
					}
				});
			}
		});	
		
		return true;
	}
	
	/***************************************************************/
	/** Preferences **/
	/***************************************************************/
 	
 	public void getPreferences() {
		mUserPreferencesTable.where().execute(new TableQueryCallback<UserPreferences>() {
			@Override
			public void onCompleted(List<UserPreferences> results, int count,
					Exception ex, ServiceFilterResponse serverFilterResponse) {
				if (ex != null) {
					if (NoNetworkConnectivityException.class.isInstance(ex))
						return;
					LensRocketLogger.e(TAG, "Error getting user preferences: " + ex.getCause().getMessage());
				} else {
					if (results == null || results.size() == 0) {
						LensRocketLogger.e(TAG, "Error getting user preferences: No results returned");
						return;
					} else {				
						mUserPrefs = results.get(0);
						mBackupPrefs = mUserPrefs.getCopy();
						//Update local shared preferences with preferences pulled down
						SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
						SharedPreferences.Editor preferencesEditor = settingsPrefs.edit();
						preferencesEditor.putString(mContext.getResources().getString(R.string.email_address), mUserPrefs.getEmail());
						preferencesEditor.commit();
						//Broadcast that we've updated our user preferences
						LensRocketLogger.i(TAG, "Preferences downloaded");
						Intent broadcast = new Intent();
						broadcast.setAction(Constants.BROADCAST_USER_PREFERENCES_UPDATED);
						mContext.sendBroadcast(broadcast);					
					}
				}
			}			
		});
	}
 	
 	public void updatePreferences(UserPreferences prefs, TableOperationCallback<UserPreferences> callback) {
 		mUserPrefs = prefs;
 		mUserPreferencesTable.update(mUserPrefs, callback);
 	}
	
	/***************************************************************/
	/** Service Filter **/
	/***************************************************************/
	
	/**
	 * Intercepts requests to and responses from our Mobile Service
	 * @author chrisner
	 *
	 */
	private class MyServiceFilter implements ServiceFilter {		
		@Override
		public void handleRequest(final ServiceFilterRequest request, final NextServiceFilterCallback nextServiceFilterCallback,
				final ServiceFilterResponseCallback responseCallback) {						
			if (!NetworkUtilities.isNetworkOnline(mContext)) {	
				getActivityContext().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LensRocketAlert.showSimpleErrorDialog(getActivityContext(), "You must be connected to the internet for this to work.");									
					}
				});					
				responseCallback.onResponse(null, new NoNetworkConnectivityException());
				return;
			}						
			nextServiceFilterCallback.onNext(request, new ServiceFilterResponseCallback() {				
				@Override
				public void onResponse(ServiceFilterResponse response, Exception ex) {
					if (ex == null) { 
						StatusLine status = response.getStatus();
						int statusCode = status.getStatusCode();		
						if (statusCode == 401) {
							//Kick user out 
							LensRocketLogger.i(TAG, "401 received, forcing logout");
							//TODO force logout
						}
					} else if (ex.getCause() != null) {
						if (UnknownHostException.class.isInstance(ex.getCause())) {
							LensRocketLogger.e(TAG, "UnknownHost");							
							getActivityContext().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									LensRocketAlert.showSimpleErrorDialog(getActivityContext(), "You must be connected to the internet for this to work.");									
								}
							});														

						} else {
							LensRocketLogger.e(TAG, "Error in handle request: " + ex.getCause().getMessage());
						}
					} else
						LensRocketLogger.e(TAG, "Error in handle request: " + ex.getMessage());					
					if (responseCallback != null)  responseCallback.onResponse(response, ex);
				}
			});
		}
	}

	/***************************************************************/
	/** Blob uploading background task **/
	/***************************************************************/
	
	/***
 	 * Handles uploading a blob to a specified url
 	 */
 	private class BlobUploaderTask extends AsyncTask<Void, Void, Boolean> {
	    private String mBlobUrl;
	    private String mFilePath;
	    private boolean mIsPicture, mIsVideo;
	    private String[] mRecipientUserIds;
	    private Rocket mRocket;
	    private RocketFile mRocketFile;
	    public BlobUploaderTask(String blobUrl, String filePath, boolean isPicture, boolean isVideo,
	    							String[] recipientUserIds, Rocket rocketReturned, RocketFile rocketFileReturned) {
	    		mBlobUrl = blobUrl;
	    		mFilePath = filePath;
	    		mIsPicture = isPicture;
	    		mIsVideo = isVideo;
	    		mRecipientUserIds = recipientUserIds;
	    		mRocket = rocketReturned;
	    		mRocketFile = rocketFileReturned;
	    }

	    @Override
	    protected Boolean doInBackground(Void... params) {	         
		    	try {
		    		//Get the rocket data
				FileInputStream fis = new FileInputStream(mFilePath);
				int bytesRead = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				while ((bytesRead = fis.read(b)) != -1) {
					bos.write(b, 0, bytesRead);
				}
				byte[] bytes = bos.toByteArray();
				fis.close();
				// Post our rocket data (byte array) to the server
				URL url = new URL(mBlobUrl.replace("\"", ""));
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("PUT");
				if (mIsPicture)
					urlConnection.addRequestProperty("Content-Type", "image/jpeg");
				else if (mIsVideo)
					urlConnection.addRequestProperty("Content-Type", "video/mp4");
				urlConnection.setRequestProperty("Content-Length", ""+ bytes.length);
				// Write image data to server
				DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
				wr.write(bytes);
				wr.flush();
				wr.close();
				int response = urlConnection.getResponseCode();
				//If we successfully uploaded, return true
				if (response == 201
						&& urlConnection.getResponseMessage().equals("Created")) {
					return true;
				}
		    	} catch (Exception ex) {
		    		Log.e(TAG, ex.getMessage());
		    	}
	        return false;	    	
	    }

	    @Override
	    protected void onPostExecute(Boolean uploaded) {
	        if (uploaded) {
	        		LensRocketLogger.i(TAG, "Upload successful");	   									
        			//delete local file
	        		File file = new File(mFilePath);
	        		if (!file.delete()) {
	        			LensRocketLogger.e(TAG, "Unable to delete file");
	        		}
				//callback: send messages to each recipient user id
	        		sendRocketsToRecipients(mRocket, mRecipientUserIds, mRocketFile);
	        }
	    }
 	}
 	
 	/***************************************************************/
	/** Push Notifications **/
	/***************************************************************/
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void registerForPush() {
 		mGcm = GoogleCloudMessaging.getInstance(mContext);
 		mHub = new NotificationHub(Constants.NOTIFICATIN_HUB_NAME, Constants.NOTIFICATION_HUB_CONNECTION_STRING, mContext);
 		new AsyncTask() {
 		      @Override
 		      protected Object doInBackground(Object... params) {
 		         try {
 		        	 	LensRocketLogger.i(TAG, "Registering for push notifications");
 		            String regId = mGcm.register(Constants.SENDER_ID);
 		            LensRocketLogger.i(TAG, "Registration ID: " + regId);
 		            if (!regId.equals(mRegistrationId)) {
 		            		LensRocketLogger.i(TAG, "Registerin with NotHubs");
 		            		mRegistrationId = regId;
 		            		SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
 		            		SharedPreferences.Editor preferencesEditor = settings.edit();
 		            		preferencesEditor.putString("registrationId", mRegistrationId);        
 		            		preferencesEditor.commit();
 		            		
 		            		mHub.registerTemplate(mRegistrationId, "messageTemplate", "{\"data\":{\"message\":\"$(message)\"}, \"collapse_key\":\"$(collapse_key)\"}", mClient.getCurrentUser().getUserId(), "AllUsers", "AndroidUser");
 		            }
 		         } catch (Exception e) {
 		        	 	LensRocketLogger.e(TAG, "Unable to register for push notifications: " + e.getMessage());
 		            return e;
 		         }
 		         return null;
 		     }
 		   }.execute(null, null, null);
 	}
 	
 	
}
