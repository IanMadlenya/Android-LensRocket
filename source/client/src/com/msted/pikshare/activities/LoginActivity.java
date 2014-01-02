package com.msted.pikshare.activities;

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
import com.msted.pikshare.R;
import com.msted.pikshare.Constants;
import com.msted.pikshare.base.BaseActivity;
import com.msted.pikshare.util.NoNetworkConnectivityException;
import com.msted.pikshare.util.PikShareAlert;
import com.msted.pikshare.util.PikShareLogger;
import com.msted.pikshare.util.PikShareRegisterResponse;
import com.msted.pikshare.util.TextValidator;

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
			mPikShareService.loginUser(mTxtUsername.getText().toString(), 
					mTxtPassword.getText().toString(), new ApiOperationCallback<PikShareRegisterResponse>() {		
				@Override
				public void onCompleted(PikShareRegisterResponse response, Exception exc,
						ServiceFilterResponse arg2) {
					PikShareLogger.i(TAG, "onCompleted login");
					if (exc != null || response.Error != null) {
						mBtnLogin.setVisibility(View.VISIBLE);
						mProgressLogin.setVisibility(View.GONE);
						if (exc != null) {
							if (NoNetworkConnectivityException.class.isInstance(exc))
								return;
							PikShareAlert.showSimpleErrorDialog(mActivity, exc.getCause().getMessage());
						}
						else
							PikShareAlert.showSimpleErrorDialog(mActivity, response.Error);									
					} else {
						mPikShareService.setUserAndSaveData(response);							
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
			mBtnLogin.setBackgroundResource(R.drawable.second_sign_up_button_style);
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
