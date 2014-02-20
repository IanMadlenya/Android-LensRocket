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
