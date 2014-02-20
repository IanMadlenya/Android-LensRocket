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
import com.msted.lensrocket.base.BaseActivity;
import com.msted.lensrocket.util.NoNetworkConnectivityException;
import com.msted.lensrocket.util.LensRocketAlert;
import com.msted.lensrocket.util.LensRocketResponse;
import com.msted.lensrocket.util.TextValidator;
import com.msted.lensrocket.R;

public class SelectUsernameActivity extends BaseActivity {
	
	private final String TAG = "SelectUsernameActivity";
	private EditText mTxtUsername;
	private Button mBtnNext;
	private ProgressBar mProgressSignup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_username);		
		mTxtUsername = (EditText) findViewById(R.id.txtUsername);
		mBtnNext = (Button) findViewById(R.id.btnNext);
		mProgressSignup = (ProgressBar) findViewById(R.id.progressLogin);		
		mBtnNext.setOnClickListener(nextListener);
		mTxtUsername.addTextChangedListener(new TextValidator(mTxtUsername) {			
			@Override
			public void validate(TextView textView, String text) {
				checkValid();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	private OnClickListener nextListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			if (isValid()) {
				mBtnNext.setVisibility(View.GONE);
				mProgressSignup.setVisibility(View.VISIBLE);				
				//Save username to record if it doens't already exist, otherwise
				//show error "username is already taken" with OK button				
				//if everything is good, finish all existing activites and go into the app
				mLensRocketService.saveUsername(mTxtUsername.getText().toString(), new ApiOperationCallback<LensRocketResponse>() {
					@Override
					public void onCompleted(LensRocketResponse response,
							Exception exc, ServiceFilterResponse arg2) {
						if (exc != null || response.Error != null) {
							mBtnNext.setVisibility(View.VISIBLE);
							mProgressSignup.setVisibility(View.GONE);													
							if (exc != null) {
								if (NoNetworkConnectivityException.class.isInstance(exc))
									return;
								LensRocketAlert.showSimpleErrorDialog(mActivity, exc.getCause().getMessage());
							}
							else {
								LensRocketAlert.showSimpleErrorDialog(mActivity, response.Error);
							}
						} else {
							mLensRocketService.saveUsername(mTxtUsername.getText().toString());							
							//TODO: show dialog with following details:
							//	title: Confirm Number
							//  body:  Help your friends find you by confirming your number!\n\nPlease send a text message to confirm your phone number.  Standard SMS rates apply.
							//  left button: No thanks
							//  right button: Send							
							//Finish all existing activities (including splashscreen)													
							Intent intent = new Intent(mActivity, AccessFriendsActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);							
							startActivity(intent);
							finish();
							mApplication.getSplashScreenActivity().finish();
						}						
					}													
				});												
			} else {
				mTxtUsername.setError("Username is invalid");
			}
		}
	};
	
	//Check that the username is valid
	private boolean isValid() {
		return (!mTxtUsername.getText().toString().equals("") &&
				mTxtUsername.getText().length() > 3);
	}
	
	private void checkValid() {
		if (this.isValid()) {
			mBtnNext.setBackgroundResource(R.drawable.sign_up_button_style);
		} else {
			mBtnNext.setBackgroundResource(R.drawable.splash_screen_button_inactive);
		}
	}
}
