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

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msted.lensrocket.Constants;
import com.msted.lensrocket.base.BaseActivity;
import com.msted.lensrocket.util.NoNetworkConnectivityException;
import com.msted.lensrocket.util.LensRocketAlert;
import com.msted.lensrocket.util.LensRocketRegisterResponse;
import com.msted.lensrocket.util.TextValidator;
import com.msted.lensrocket.R;

public class SignupActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {
	private final String TAG = "SignupActivity";
	private TextView mLblDisclaimer;
	private EditText mTxtBirthday;
	private EditText mTxtEmail;
	private EditText mTxtPassword;
	private Button mBtnSignup;
	private boolean mDateIsInFuture = false;
	private ProgressBar mProgressSignup;
	private Calendar mSelectedDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);		
		mLblDisclaimer = (TextView) findViewById(R.id.lblDisclaimer);
		mLblDisclaimer.setText(Html.fromHtml(getResources().getString(R.string.sign_up_disclaimer)));
		mLblDisclaimer.setMovementMethod(LinkMovementMethod.getInstance());		
		mTxtBirthday = (EditText) findViewById(R.id.txtBirthday);
		mTxtBirthday.setClickable(true);
		mTxtBirthday.setOnClickListener(birthdayListener);		
		mBtnSignup = (Button) findViewById(R.id.btnNext);		
		mTxtEmail = (EditText) findViewById(R.id.txtEmail);
		mTxtPassword = (EditText) findViewById(R.id.txtPassword);
		mProgressSignup = (ProgressBar) findViewById(R.id.progressLogin);		
		mTxtEmail.addTextChangedListener(new TextValidator(mTxtEmail) {			
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
		
		mBtnSignup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isValid()) {
					mBtnSignup.setVisibility(View.GONE);
					mProgressSignup.setVisibility(View.VISIBLE);					
					mLensRocketService.registerUser(mTxtPassword.getText().toString(), 
						mTxtBirthday.getText().toString(), mTxtEmail.getText().toString(), new ApiOperationCallback<LensRocketRegisterResponse>() {
							@Override
							public void onCompleted(
									LensRocketRegisterResponse response,
									Exception exc, ServiceFilterResponse arg2) {
								if (exc != null || response.Error != null) {
									mBtnSignup.setVisibility(View.VISIBLE);
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
									mLensRocketService.setUserAndSaveData(response);
									finish();
									startActivity(new Intent(getApplicationContext(), SelectUsernameActivity.class));
								}
							}															
						});					
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	private OnClickListener birthdayListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			DialogFragment newFragment = new DatePickerFragment();
		    newFragment.show(getFragmentManager(), "datePicker");	 
		}
	};
	
	public static class DatePickerFragment extends DialogFragment { 		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);		
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), (SignupActivity) getActivity(), year, month, day);
		}
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		//TODO: consider formatting for region (i.e. MMDDYYYY vs DDMMYYYY)		
		mTxtBirthday.setText(month+1 + "/" + day + "/" + year);		
		mSelectedDate = Calendar.getInstance();
		mSelectedDate.set(year, month, day);		
		if (mSelectedDate.after(Calendar.getInstance())) {
			LensRocketAlert.showToast(mActivity, R.string.born_in_future);
			mTxtBirthday.setError("Date must be in the past");
			mDateIsInFuture = true;
		} else {
			mTxtBirthday.setError(null);
			mDateIsInFuture = false;
		}		
		checkValid();
	}	
	
	private void checkValid() {
		if (this.isValid()) {
			mBtnSignup.setBackgroundResource(R.drawable.sign_up_button_style);
		} else {
			mBtnSignup.setBackgroundResource(R.drawable.splash_screen_button_inactive);
		}
	}
	
	private boolean isValid() {
		if (mDateIsInFuture || mTxtBirthday.getText().toString().equals(""))
			return false;
		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mTxtEmail.getText().toString()).matches()) {
			return false;
		}
		if (mTxtPassword.getText().toString().length() < Constants.MIN_PASSWORD_LENGTH)
			return false;		
		return true;
	}
}
