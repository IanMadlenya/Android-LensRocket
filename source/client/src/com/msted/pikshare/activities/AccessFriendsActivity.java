package com.msted.pikshare.activities;

import com.msted.pikshare.R;
import com.msted.pikshare.base.BaseActivity;
import com.msted.pikshare.util.PikShareAlert;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

public class AccessFriendsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState, true);
		setContentView(R.layout.activity_access_friends);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.access_friends, menu);
		return true;
	}
	
	public void tappedAllowAccess(View view) {
		PikShareAlert.showSimpleErrorDialog(this, "Todo: Import friends");
	}
	
	public void tappedSkip(View view) {
		goToCamera();
	}
	
	@Override
	public void onBackPressed() {
		goToCamera();
	}
	
	private void goToCamera() {
		Intent intent = new Intent(mActivity, RecordActivity.class);
		startActivity(intent);
		finish();	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			goToCamera();
			return true;
		case R.id.menuSkip:		
			goToCamera();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
