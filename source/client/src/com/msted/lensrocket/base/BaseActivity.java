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

package com.msted.lensrocket.base;

import com.msted.lensrocket.LensRocketApplication;
import com.msted.lensrocket.LensRocketService;
import com.msted.lensrocket.util.LensRocketLogger;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public abstract class BaseActivity extends Activity {

	protected LensRocketService mLensRocketService;
	protected BaseActivity mActivity;
	protected LensRocketApplication mApplication;
	private final String TAG = "BaseActivity";
	
	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, false);
	}
		
	protected void onCreate(Bundle savedInstanceState, boolean showTitleBar) {		
		super.onCreate(savedInstanceState);
		if (!showTitleBar)
			requestWindowFeature(Window.FEATURE_NO_TITLE); 
		mActivity = this;
		LensRocketLogger.i(TAG, this.toString());
		mApplication = (LensRocketApplication) getApplication();
		mApplication.setCurrentActivity(this);
		mLensRocketService = mApplication.getLensRocketService();
				
	}
	
	@Override
	protected void onResume() {
		mApplication.setIsApplicationActive(true);
		mLensRocketService.setContext(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mApplication.setIsApplicationActive(false);
		super.onPause();
	}
}
