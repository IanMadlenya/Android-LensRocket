package com.msted.pikshare.activities;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Filter.FilterListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msted.pikshare.Constants;
import com.msted.pikshare.R;
import com.msted.pikshare.adapters.FriendsListArrayAdapter;
import com.msted.pikshare.base.BaseActivity;
import com.msted.pikshare.datamodels.Friend;
import com.msted.pikshare.util.NoNetworkConnectivityException;
import com.msted.pikshare.util.PikShareAlert;
import com.msted.pikshare.util.PikShareLogger;
import com.msted.pikshare.util.PikShareRegisterResponse;

public class FriendsListActivity extends BaseActivity {
	
	private final String TAG = "FriendsListActivity";
	private ListView mLvFriends;
	private FriendsListArrayAdapter mAdapter;
	private LinearLayout mLayoutAddFriend;
	private TextView mLblNewFriendName;
	private String mCurrentName;
	private ImageButton mBtnAddFriend;
	private SearchView mSearchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState, true);
		setContentView(R.layout.activity_friends_list);
		setupActionBar();
		
		mLayoutAddFriend = (LinearLayout) findViewById(R.id.layoutAddFriend);
		mLayoutAddFriend.setVisibility(View.GONE);		
		mLblNewFriendName = (TextView) findViewById(R.id.lblNewFriendName);
		mBtnAddFriend = (ImageButton) findViewById(R.id.btnAddFriend);		
		mLvFriends = (ListView) findViewById(R.id.lvFriends);
		mLvFriends.setOverScrollMode(View.OVER_SCROLL_NEVER);		
		mAdapter = new FriendsListArrayAdapter(this,  mPikShareService.getLocalFriends());
		mLvFriends.setAdapter(mAdapter);
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
			PikShareLogger.i(TAG, "Broadcast received");
			boolean wasSuccess = intent.getBooleanExtra(Constants.FRIENDS_UPDATE_STATUS, false);
			if (wasSuccess) {						
				mAdapter.clear();
				for (Friend friend : mPikShareService.getLocalFriends()) {
					mAdapter.add(friend);
				}		
			} else {
				PikShareAlert.showToast(mActivity, R.string.error_getting_friends);
			}
		}
	};

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.friends_list, menu);		
		// Associate searchable configuration with the SearchView
	    SearchManager searchManager =
	           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    mSearchView =
	            (SearchView) menu.findItem(R.id.menuSearch).getActionView();
	    mSearchView.setSearchableInfo(
	            searchManager.getSearchableInfo(getComponentName()));	    
	    mSearchView.setOnQueryTextListener(new OnQueryTextListener() {			
	    		@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(final String newText) {
				PikShareLogger.i(TAG, "Text: " + newText);
				mCurrentName = newText;
				mBtnAddFriend.setEnabled(true);
				mBtnAddFriend.setVisibility(View.VISIBLE);				
				mAdapter.getFilter().filter(newText, new FilterListener() {					
					@Override
					public void onFilterComplete(int count) {
						if (mAdapter.getCount() > 0) 
							mLvFriends.setVisibility(View.VISIBLE);
						else
							mLvFriends.setVisibility(View.GONE);
					
						if (!mCurrentName.equals("")) {
							mLayoutAddFriend.setVisibility(View.VISIBLE);
						} else {
							mLayoutAddFriend.setVisibility(View.GONE);
						}					
						if (mPikShareService.getLocalFriendNames().contains(newText))
							mLayoutAddFriend.setVisibility(View.GONE);
					}
				});
				mLblNewFriendName.setText(mCurrentName);								
				return true;
			}
		});		
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
			NavUtils.navigateUpFromSameTask(this);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
			return true;
		case R.id.menuSearch:
			
			return true;
		case R.id.menuAddFriends:
			PikShareLogger.i(TAG, "Need to implement adding friends");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		PikShareLogger.i(TAG, "backpressed");
				
		//Fix for issue affecting galaxy nexus where
		//query filter doesn't empty on back pressed
		if (mCurrentName != null && !mCurrentName.equals("")) {
			mCurrentName = "";
			PikShareLogger.i(TAG, "Not iconified");
			mSearchView.setIconified(true);
			return;
		}

		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);				
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            PikShareLogger.i(TAG, "Search for: " + query);
        }
	}
	
	public void tappedAddFriend(View view) {
		PikShareAlert.showToast(this, "Adding " + mCurrentName +"...");
		mBtnAddFriend.setEnabled(false);
		mPikShareService.requestFriend(mCurrentName, new ApiOperationCallback<PikShareRegisterResponse>() {			
			@Override
			public void onCompleted(PikShareRegisterResponse response, Exception ex,
					ServiceFilterResponse arg2) {
				PikShareLogger.i(TAG, "Response received");
				if (ex != null || response.Error != null) {
					mBtnAddFriend.setEnabled(true);									
					if (ex != null) {
						if (NoNetworkConnectivityException.class.isInstance(ex))
							return;
						PikShareAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
					}
					else 
						PikShareAlert.showToast(mActivity, response.Error);
				} else {
					PikShareAlert.showToast(mActivity, response.Status);
					mBtnAddFriend.setVisibility(View.GONE);
					mPikShareService.getFriends();					
				}
			}
		});
	}
}
