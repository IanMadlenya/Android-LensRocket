package com.msted.pikshare.base;

import com.msted.pikshare.PikShareApplication;
import com.msted.pikshare.PikShareService;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;

public class BaseListActivity extends ListActivity {

	protected PikShareService mPikShareService;
	protected BaseListActivity mActivity;
	protected PikShareApplication mApplication;
	
	@Override	
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, false);
	}
		
	protected void onCreate(Bundle savedInstanceState, boolean showTitleBar) {
		super.onCreate(savedInstanceState);
		if (!showTitleBar)
			requestWindowFeature(Window.FEATURE_NO_TITLE); 
		mActivity = this;		
		mApplication = (PikShareApplication) getApplication();
		mApplication.setCurrentActivity(this);
		mPikShareService = mApplication.getPikShareService();
		mPikShareService.setContext(this);
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
