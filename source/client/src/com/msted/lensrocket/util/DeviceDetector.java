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

import java.util.Arrays;
import java.util.List;

import com.msted.lensrocket.Constants;

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
