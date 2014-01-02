package com.msted.pikshare.util;

import android.util.Log;

public class PikShareLogger {
	public static void i(String tag, String msg) {
		Log.i(tag, msg);
	}
	
	public static void e(String tag, String msg) {
		Log.e(tag, msg);
	}
	
	public static void d(String tag, String msg) {
		Log.d(tag, msg);
	}
}
