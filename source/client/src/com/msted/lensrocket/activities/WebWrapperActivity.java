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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;

import com.msted.lensrocket.Constants;
import com.msted.lensrocket.R;
import com.msted.lensrocket.util.LensRocketLogger;

public class WebWrapperActivity extends Activity {

	private final String TAG = "WebWrapperActivity";
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		setContentView(R.layout.activity_web_wrapper);
		
		mWebView = (WebView)findViewById(R.id.webView);
		
		Intent passedIntent = getIntent();
		String url = passedIntent.getStringExtra("url");
		String titleText = passedIntent.getStringExtra("title");
		if (url != null && !url.equals("")) {
			mWebView.loadUrl(url);
		} else {
			Uri uriData = getIntent().getData();
			String endpoint = uriData.getHost().toLowerCase();
			if (endpoint.equals("terms")) {
				mWebView.loadUrl(Constants.TERMS_AND_CONDITIONS_URL);
				titleText = getResources().getString(R.string.terms_of_use);
			} else if (endpoint.equals("privacy")) {
				mWebView.loadUrl(Constants.PRIVACY_POLICY_URL);
				titleText = getResources().getString(R.string.privacy_policy);
			}
		}
		
		setupActionBar(titleText);
	}

	private void setupActionBar(String titleText) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Hide icon in action bar
		//getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle(titleText);
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.web_wrapper, menu);
//		return true;
//	}

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
			//NavUtils.navigateUpFromSameTask(this);
			//Calling finish here so we go back to the review screen
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
