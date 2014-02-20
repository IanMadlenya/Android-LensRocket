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

package com.msted.lensrocket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.msted.lensrocket.base.BaseActivity;
import com.msted.lensrocket.R;

public class SplashScreenActivity extends BaseActivity {
	
	private Button mBtnSignup;
	private Button mBtnLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		mApplication.setSplashScreenActivity(this);			
		if (mLensRocketService.isUserAuthenticated()) {			
			Intent intent = new Intent(mActivity, RecordActivity.class);
			startActivity(intent);
			finish();
		}		
		setContentView(R.layout.activity_splash_screen);
		mBtnSignup = (Button) findViewById(R.id.btnSignUp);
		mBtnLogin = (Button) findViewById(R.id.btnLogin);
		mBtnSignup.setOnClickListener(signupListener);
		mBtnLogin.setOnClickListener(loginListener);
	}
	
	private OnClickListener signupListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			startActivity(new Intent(getApplicationContext(), SignupActivity.class));
		}
	};
	
	private OnClickListener loginListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			startActivity(new Intent(getApplicationContext(), LoginActivity.class));
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
