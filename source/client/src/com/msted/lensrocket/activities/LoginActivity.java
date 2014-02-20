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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msted.lensrocket.Constants;
import com.msted.lensrocket.base.BaseActivity;
import com.msted.lensrocket.util.NoNetworkConnectivityException;
import com.msted.lensrocket.util.LensRocketAlert;
import com.msted.lensrocket.util.LensRocketLogger;
import com.msted.lensrocket.util.LensRocketRegisterResponse;
import com.msted.lensrocket.util.TextValidator;
import com.msted.lensrocket.R;

public class LoginActivity extends BaseActivity {
	
	private final String TAG = "LoginActivity";
	private EditText mTxtUsername;
	private EditText mTxtPassword;
	private Button mBtnLogin;
	private ProgressBar mProgressLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mTxtUsername = (EditText) findViewById(R.id.txtUsername);
		mTxtPassword = (EditText) findViewById(R.id.txtPassword);
		mBtnLogin = (Button) findViewById(R.id.btnLogin);
		mProgressLogin = (ProgressBar) findViewById(R.id.progressLogin);
		mTxtUsername.addTextChangedListener(new TextValidator(mTxtUsername) {			
			@Override
			public void validate(TextView textView, String text) {
				checkValid();
			}
		});
		mTxtPassword.addTextChangedListener(new TextValidator(mTxtPassword) {			
			@Override
			public void validate(TextView textView, String text) {
				checkValid();				
			}
		});
		mBtnLogin.setOnClickListener(loginListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	private OnClickListener loginListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			mBtnLogin.setVisibility(View.GONE);
			mProgressLogin.setVisibility(View.VISIBLE);		
			mLensRocketService.loginUser(mTxtUsername.getText().toString(), 
					mTxtPassword.getText().toString(), new ApiOperationCallback<LensRocketRegisterResponse>() {		
				@Override
				public void onCompleted(LensRocketRegisterResponse response, Exception exc,
						ServiceFilterResponse arg2) {
					LensRocketLogger.i(TAG, "onCompleted login");
					if (exc != null || response.Error != null) {
						mBtnLogin.setVisibility(View.VISIBLE);
						mProgressLogin.setVisibility(View.GONE);
						if (exc != null) {
							if (NoNetworkConnectivityException.class.isInstance(exc))
								return;
							LensRocketAlert.showSimpleErrorDialog(mActivity, exc.getCause().getMessage());
						}
						else
							LensRocketAlert.showSimpleErrorDialog(mActivity, response.Error);									
					} else {
						mLensRocketService.setUserAndSaveData(response);							
						finish();
						Intent intent = new Intent(mActivity, RecordActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);						
					}					
				}
			});
		}
	};
	
	private void checkValid() {
		if (this.isValid()) {
			mBtnLogin.setBackgroundResource(R.drawable.sign_up_button_style);
		} else {
			mBtnLogin.setBackgroundResource(R.drawable.splash_screen_button_inactive);
		}
	}
	
	private boolean isValid() {
		if (mTxtUsername.getText().toString().length() < Constants.MIN_USERNAME_LENGTH)
			return false;
		if (mTxtPassword.getText().toString().length() < Constants.MIN_PASSWORD_LENGTH)
			return false;
		if (mTxtPassword.getText().toString().equals(""))
			return false;		
		return true;
	}
}
