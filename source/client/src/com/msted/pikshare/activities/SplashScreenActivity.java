package com.msted.pikshare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.msted.pikshare.R;
import com.msted.pikshare.base.BaseActivity;

public class SplashScreenActivity extends BaseActivity {
	
	private Button mBtnSignup;
	private Button mBtnLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		mApplication.setSplashScreenActivity(this);			
		if (mPikShareService.isUserAuthenticated()) {			
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
