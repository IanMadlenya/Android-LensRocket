package com.msted.pikshare.base;

import com.msted.pikshare.PikShareApplication;
import com.msted.pikshare.PikShareService;
import com.msted.pikshare.util.PikShareLogger;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public abstract class BaseActivity extends Activity {

	protected PikShareService mPikShareService;
	protected BaseActivity mActivity;
	protected PikShareApplication mApplication;
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
		PikShareLogger.i(TAG, this.toString());
		mApplication = (PikShareApplication) getApplication();
		mApplication.setCurrentActivity(this);
		mPikShareService = mApplication.getPikShareService();
				
	}
	
	@Override
	protected void onResume() {
		mApplication.setIsApplicationActive(true);
		mPikShareService.setContext(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mApplication.setIsApplicationActive(false);
		super.onPause();
	}
}
