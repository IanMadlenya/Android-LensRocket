package com.msted.pikshare.util;

import java.util.Arrays;
import java.util.List;

import com.msted.pikshare.Constants;

import android.os.Build;

public class DeviceDetector {

	public static String getDeviceName() {
	  String manufacturer = Build.MANUFACTURER;
	  String model = Build.MODEL;
	  if (model.startsWith(manufacturer)) {
	    return model.toUpperCase();
	  } else {
	    return manufacturer.toUpperCase() + "-" + model.toUpperCase();
	  }
	}
	
	public static boolean isDeviceS3() {
		List<String> s3ModelList = Arrays.asList(Constants.s3ModelNames);
		return s3ModelList.contains(getDeviceName());
	}
}
