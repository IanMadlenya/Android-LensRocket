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

import com.msted.lensrocket.base.BaseActivity;
import com.msted.lensrocket.util.LensRocketAlert;
import com.msted.lensrocket.R;

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
		LensRocketAlert.showSimpleErrorDialog(this, "Todo: Import friends");
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
