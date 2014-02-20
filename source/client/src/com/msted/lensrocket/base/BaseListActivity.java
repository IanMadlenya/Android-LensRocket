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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;

public class BaseListActivity extends ListActivity {

	protected LensRocketService mLensRocketService;
	protected BaseListActivity mActivity;
	protected LensRocketApplication mApplication;
	
	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, false);
	}
		
	protected void onCreate(Bundle savedInstanceState, boolean showTitleBar) {
		super.onCreate(savedInstanceState);
		if (!showTitleBar)
			requestWindowFeature(Window.FEATURE_NO_TITLE); 
		mActivity = this;		
		mApplication = (LensRocketApplication) getApplication();
		mApplication.setCurrentActivity(this);
		mLensRocketService = mApplication.getLensRocketService();
		mLensRocketService.setContext(this);
	}
	
	@Override
	protected void onResume() {
		mApplication.setIsApplicationActive(true);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mApplication.setIsApplicationActive(false);
		super.onPause();
	}
}
