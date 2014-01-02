package com.msted.pikshare;

import com.msted.pikshare.activities.SplashScreenActivity;

import android.app.Activity;
import android.app.Application;

public class PikShareApplication extends Application {
	private PikShareService mPikShareService;
	private Activity mCurrentActivity;
	private SplashScreenActivity mSplashScreenActivity;
	private boolean mIsApplicationActive = false;
	
	public PikShareApplication() {}
	
	public PikShareService getPikShareService() {
		if (mPikShareService == null) {
			mPikShareService = new PikShareService(this);
		}
		return mPikShareService;
	}	
	
	public void setCurrentActivity(Activity activity) {
		mCurrentActivity = activity;
	}
	
	public Activity getCurrentActivity() {
		return mCurrentActivity;
	}
	
	public void setSplashScreenActivity(SplashScreenActivity activity) {
		mSplashScreenActivity = activity;
	}
	
	public SplashScreenActivity getSplashScreenActivity() {
		return mSplashScreenActivity;
	}
	
	public void setIsApplicationActive(boolean isApplicationActive) {
		mIsApplicationActive = isApplicationActive;
	}
	
	public boolean getIsApplicationActive() { return mIsApplicationActive; }
}
