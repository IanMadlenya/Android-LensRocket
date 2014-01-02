package com.msted.pikshare.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.msted.pikshare.R;

public class PikShareAlert {
	
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
