package com.msted.pikshare;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHandler {
	private final static String TAG = "PreferencesHandler";
	private final static String PREFERENCES_NAME = "UserData";
	
	public static void SaveCameraPreference(Context context, int camera) {		
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putInt("cameraType", camera);
        preferencesEditor.commit();
	}
	
	public static int GetCameraPreference(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
		return settings.getInt("cameraType", 0);
	}
	
	public static void SaveFlashPreference(Context context, boolean flashOn) {		
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putBoolean("flashOn", flashOn);
        preferencesEditor.commit();
	}
	
	public static boolean GetFlashPreference(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
		return settings.getBoolean("flashOn", false);
	}
}
