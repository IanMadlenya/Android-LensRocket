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

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.msted.lensrocket.Constants;
import com.msted.lensrocket.adapters.SendToFriendsArrayAdapter;
import com.msted.lensrocket.base.BaseActivity;
import com.msted.lensrocket.datamodels.Friend;
import com.msted.lensrocket.util.LensRocketAlert;
import com.msted.lensrocket.util.LensRocketLogger;
import com.msted.lensrocket.R;

public class SendToFriendsActivity extends BaseActivity {
	private String TAG = "SendToFriendsActivity";
	private String mFileFullPath;
	private boolean mReviewingPicture;
	private boolean mReviewingVideo;
	private int mSelectedSeconds;
	private boolean mIsReply;
	private String mReplyToUserId;
	private RelativeLayout mLayoutShareNames;
	private ImageButton mBtnSendToFriends;
	private TextView mLblShareNames;
	private List<String> mSelectedUsernames;	
	private ListView mLvFriends;
	private SendToFriendsArrayAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState, true);
		setContentView(R.layout.activity_send_to_friends);
		setupActionBar();		
		mLayoutShareNames = (RelativeLayout) findViewById(R.id.layoutShareNames);
		mLblShareNames = (TextView) findViewById(R.id.lblShareNames);
		mBtnSendToFriends = (ImageButton) findViewById(R.id.btnSendToFriends);		
		Intent intent = getIntent();
		if (intent != null) {
			mFileFullPath = intent.getStringExtra("filePath");
			mReviewingPicture = intent.getBooleanExtra("isPicture", false);
			mReviewingVideo = intent.getBooleanExtra("isVideo", false);
			mSelectedSeconds = intent.getIntExtra("timeToLive", 0);
			mIsReply = intent.getBooleanExtra("isReply", false);
			mReplyToUserId = intent.getStringExtra("replyToUserId");
		}		
		mLvFriends = (ListView) findViewById(R.id.lvFriends);
		mAdapter = new SendToFriendsArrayAdapter(this,  mLensRocketService.getLocalFriends());
		mLvFriends.setAdapter(mAdapter);			
		mLvFriends.setOnItemClickListener(friendClickListener);	
		mSelectedUsernames = new ArrayList<String>();		
		if (mIsReply) {
			mLensRocketService.increaseCheckCount();
		}		
		if (mLensRocketService.getCheckCount() > 0) {
			animateSendPanel(true);
			for (Friend friend : mLensRocketService.getLocalFriends()) {
				if (mIsReply) {
					if (friend.getToUserId().equals(mReplyToUserId))
						friend.setChecked(true);
				}
				if (friend.getChecked()) {
					mSelectedUsernames.add(friend.getToUsername());
				}
			}
			updateShareLabel();
		}
	}
	
	private OnItemClickListener friendClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			LensRocketLogger.i(TAG, "onClick");
			CheckBox cbSelected = (CheckBox) view.findViewById(R.id.cbSelected);
			cbSelected.setChecked(!cbSelected.isChecked());	
		}		
	};
	
	public void updateRowCheck(int position, boolean isChecked) {
		Friend friend = mLensRocketService.getLocalFriends().get(position); 
		friend.setChecked(isChecked);
		if (isChecked) {
			mLensRocketService.increaseCheckCount();
			mSelectedUsernames.add(friend.getToUsername());
			if (mLensRocketService.getCheckCount() == 1) {
				LensRocketLogger.i(TAG, "up");
				animateSendPanel(true);				
			}
		} else {
			mLensRocketService.decreaseCheckCount();
			mSelectedUsernames.remove(friend.getToUsername());
			if (mLensRocketService.getCheckCount() == 0) {
				LensRocketLogger.i(TAG, "down");
				animateSendPanel(false);				
			}
		}
		updateShareLabel();
	}
	
	private void animateSendPanel(boolean animateUp) {
		if (animateUp) {
			mBtnSendToFriends.setEnabled(true);
			Animation bottomUp = AnimationUtils.loadAnimation(mActivity, R.anim.slide_up_dialog);
			mLayoutShareNames.startAnimation(bottomUp);
			mLayoutShareNames.setVisibility(View.VISIBLE);
		} else {
			mBtnSendToFriends.setEnabled(false);
			Animation bottomUp = AnimationUtils.loadAnimation(mActivity, R.anim.slide_down_dialog);
			mLayoutShareNames.startAnimation(bottomUp);
			mLayoutShareNames.setVisibility(View.GONE);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Hide icon in action bar
		getActionBar().setDisplayShowHomeEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_to_friends, menu);
		return true;
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
			//NavUtils.navigateUpFromSameTask(this);
			//Calling finish here so we go back to the review screen
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_FRIENDS_UPDATED);
		registerReceiver(receiver, filter);
		super.onResume();	
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			boolean wasSuccess = intent.getBooleanExtra(Constants.FRIENDS_UPDATE_STATUS, false);
			if (wasSuccess) {
				mAdapter.clear();			
				for (Friend friend : mLensRocketService.getLocalFriends()) {
					mAdapter.add(friend);
				}		
				LensRocketLogger.i(TAG, "Refresh complete");
			} else {
				LensRocketAlert.showToast(mActivity, R.string.error_getting_friends);
			}
		}
	};
	
	private void updateShareLabel() {
		if (mSelectedUsernames.size() < 3) {
			String joinedNames = TextUtils.join(", ", mSelectedUsernames);
			mLblShareNames.setText(joinedNames);
		} else {
			mLblShareNames.setText("Friends selected");
		}	
	}
	
	public void tappedSendToFriends(View view) {
		if (mLensRocketService.sendRocket(mFileFullPath, mReviewingPicture, mReviewingVideo,mSelectedSeconds)) {
			mLensRocketService.uncheckFriends();
			setResult(Constants.RESULT_CODE_ROCKET_SENT);
			finish();
		}
	}

}
