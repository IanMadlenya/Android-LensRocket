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

package com.msted.lensrocket.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.msted.lensrocket.R;

public class LensRocketAlert {
	
	public static void showSimpleErrorDialog(Activity context, String errorMessage) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		alertBuilder.setMessage(errorMessage)
					.setTitle(R.string.error);
		alertBuilder.setPositiveButton(R.string.ok, null);
		AlertDialog dialog = alertBuilder.create();
		dialog.show();
	}
	
	public static void showToast(Context context, int resourceId) {
		showToast(context, context.getResources().getString(resourceId));
	}
	
	public static void showToast(Context context, int resourceId, boolean centerText, boolean centerScreen) {
		showToast(context, context.getResources().getString(resourceId), centerText, centerScreen);
	}
	
	public static void showToast(Context context, String text, boolean centerText, boolean centerScreen) {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		if (centerText) {
			LinearLayout layout = (LinearLayout) toast.getView();
			TextView tv = (TextView) layout.getChildAt(0);
			tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		}
		if (centerScreen) {
			toast.setGravity(Gravity.CENTER, 0, 0);
		}
		toast.show();
	}
	
	public static void showToast(Context context, String text) {
		showToast(context, text, true, false);
	}
}
