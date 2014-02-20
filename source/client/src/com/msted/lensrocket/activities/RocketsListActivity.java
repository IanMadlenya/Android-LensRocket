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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.VideoView;

import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msted.lensrocket.Constants;
import com.msted.lensrocket.adapters.RocketsArrayAdapter;
import com.msted.lensrocket.base.BaseActivity;
import com.msted.lensrocket.datamodels.Rocket;
import com.msted.lensrocket.util.NoNetworkConnectivityException;
import com.msted.lensrocket.util.LensRocketAlert;
import com.msted.lensrocket.util.LensRocketLogger;
import com.msted.lensrocket.util.LensRocketResponse;
import com.msted.lensrocket.R;

public class RocketsListActivity extends BaseActivity implements PullToRefreshAttacher.OnRefreshListener {
	
	private final String TAG = "RocketsListActivity";
	private ListView mLvRockets;
	private RocketsArrayAdapter mAdapter;	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private boolean mIsViewingPicture;
	private boolean mIsViewingVideo;
	private Dialog mViewingDialog;
	private ImageView mImagePicture;
	private VideoView mVideoView;
	private GestureDetector mGestureDetector;
	private int mTappedRowPosition = -1;
	private PullToRefreshLayout mLayoutRockets;
	private File mVideoFile;
	private RelativeLayout mVideoLayout;	
	private Bitmap mBitmapHolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);		
		super.onCreate(savedInstanceState, true);
		setContentView(R.layout.activity_rockets_list);
		// Show the Up button in the action bar.
		setupActionBar();
		mLvRockets = (ListView) findViewById(R.id.lvRockets);
		mLvRockets.setEmptyView(findViewById(android.R.id.empty));
		mLayoutRockets = (PullToRefreshLayout) findViewById(R.id.layoutRockets);
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
		mLayoutRockets.setPullToRefreshAttacher(mPullToRefreshAttacher, this);		
		mGestureDetector = new GestureDetector(this, new GestureListener());
		mAdapter = new RocketsArrayAdapter(this,  mLensRocketService.getLocalRockets());
		mLvRockets.setAdapter(mAdapter);			
		mLvRockets.setOnItemClickListener(rocketClickListener);
		mLvRockets.setOnItemLongClickListener(rocketLongClickListener);
		
		mLvRockets.setOnTouchListener(new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (mIsViewingPicture || mIsViewingVideo) {
						mViewingDialog.dismiss();
						mIsViewingPicture = false;
						mIsViewingVideo = false;
						if (mImagePicture != null) {
							mImagePicture = null;
						} else if (mVideoView != null) {
							mVideoView.stopPlayback();
							mVideoView = null;
						}						
					}
				}
				return false;
			}
		});
	}
	
	private OnItemClickListener rocketClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mTappedRowPosition = position;
		}		
	};
	
	@Override
    public void onRefreshStarted(View view) {
		LensRocketLogger.i(TAG, "onRefreshStarted");
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                mLensRocketService.getRockets();
                // Notify PullToRefreshAttacher that the refresh has finished
                mPullToRefreshAttacher.setRefreshComplete();
            }
        }.execute();		
    }
	
	private OnItemLongClickListener rocketLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			final Rocket rocket = mLensRocketService.getLocalRockets().get(position);
			if (rocket.getType().equals("FriendRequest")) {
				//Friend and update the rocket
				mLensRocketService.acceptFriendRequest(rocket, new ApiOperationCallback<LensRocketResponse>() {					
					@Override
					public void onCompleted(LensRocketResponse response, Exception ex,
							ServiceFilterResponse serviceFilterResponse) {
						LensRocketLogger.i(TAG, "Response received");
						if (ex != null || response.Error != null) {																
							if (ex != null) {
								if (NoNetworkConnectivityException.class.isInstance(ex))
									return;	
								LensRocketAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
							}
							else 
								LensRocketAlert.showToast(mActivity, response.Error);
						} else {
							mLensRocketService.getFriends();
							int position = mLensRocketService.getLocalRockets().indexOf(rocket);
							View view = mLvRockets.getChildAt(position);
							ImageView imgIndicator = (ImageView) view.findViewById(R.id.imgIndicator);
							imgIndicator.setImageResource(R.drawable.rocket_accepted_friend_request);
							final TextView lblInstructions = (TextView) view.findViewById(R.id.lblInstructions);
							lblInstructions.setText(getResources().getString(R.string.instructions_accepted_friend_request));
						}
					}
				});
			} else if (rocket.getType().equals("Rocket")) {				
				if (rocket.getHasUserSeen()) {
					//Do nothing, they should double tap to reply
				} else {
					//Get SharedAccessSignature for rocket
					mLensRocketService.getRocketForRecipient(rocket, new ApiOperationCallback<LensRocketResponse>() {
						@Override
						public void onCompleted(LensRocketResponse response,
								Exception ex, ServiceFilterResponse serviceFilterResponse) {
							if (ex != null || response.Error != null) {								
								if (ex != null)  {
									if (NoNetworkConnectivityException.class.isInstance(ex))
										return;								
									LensRocketAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
								}
								else
									LensRocketAlert.showSimpleErrorDialog(mActivity, response.Error);																	
							} else {
								LensRocketLogger.d(TAG, response.RocketUrl);
								//Display the rocket depending on the type
								mViewingDialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar);
								if (rocket.getIsPicture()) {									
									mIsViewingPicture = true;
									if (mImagePicture == null) 
										mImagePicture = new ImageView(mActivity);
									new DownloadRocketPictureTask().execute(response.RocketUrl);									
									mViewingDialog.setContentView(mImagePicture);									
									mViewingDialog.show();									
									//Start countdown								
									startCountdownTimer(rocket);
									
								} else if (rocket.getIsVideo()) {
									mIsViewingVideo = true;
									if (mVideoView == null) {
										mVideoView = new VideoView(mActivity);
									}
									mVideoLayout = new RelativeLayout(mActivity);
									RelativeLayout.LayoutParams layoutParams = new 
											RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
													LayoutParams.MATCH_PARENT);									
									layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
									layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
									layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
									layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
									mVideoView.setLayoutParams(layoutParams);									
									mVideoView.setOnPreparedListener(new OnPreparedListener() {				
											@Override
											public void onPrepared(MediaPlayer mp) {
												LensRocketLogger.i(TAG, "video view prepared");
												mp.setLooping(true);											
											}
										});
									
									/**
									 * Downloading the video before playing it.  While the
									 * video view does allow you to set the video URI to a
									 * URL for the actual video, playback on some devices / 
									 * streaming the videos created on some devices does not work.
									 * For example, the MP4 files created by the Galaxy S3 will
									 * not stream to any device tested.  I believe this has to do 
									 * with the placement of the moov atom as described on this page
									 * http://developer.android.com/guide/appendix/media-formats.html
									 */
									new DownloadRocketVideoTask(rocket).execute(response.RocketUrl);									
								}
							}
						}
					});
				}
			}
			return false;
		}		
	};
	
	private void startCountdownTimer(final Rocket rocket) {
		int position = mLensRocketService.getLocalRockets().indexOf(rocket);
		if (rocket == null)
			LensRocketLogger.i(TAG, "Rocket is null");
		LensRocketLogger.i(TAG, "Position: " + position);
		if (mLvRockets == null)
			LensRocketLogger.i(TAG, "mlvrockets is null");
		View view = mLvRockets.getChildAt(position);		
		final TextView lblTime = (TextView) view.findViewById(R.id.lblTime);
		//Only start a countdown if we haven't already
		if (lblTime.getText().equals("")) {
			final ImageView imgIndicator = (ImageView) view.findViewById(R.id.imgIndicator);
			final TextView lblInstructions = (TextView) view.findViewById(R.id.lblInstructions);
			int timeToLive = rocket.getTimeToLive();
			lblTime.setText(timeToLive + "");			
			new CountDownTimer(timeToLive * 1000, 1000) {
				public void onTick(long millisUntilFinished) {
					lblTime.setText(millisUntilFinished / 1000 + "");
				}
				
				public void onFinish() {
					imgIndicator.setImageResource(R.drawable.rocket_seen);
					lblTime.setText(R.string.empty_string);
					lblInstructions.setText(R.string.instructions_seen_rocket);
					rocket.setHasUserSeen(true);
					if (mViewingDialog.isShowing())
						mViewingDialog.dismiss();
					if (mBitmapHolder != null && !mBitmapHolder.isRecycled())
						mBitmapHolder.recycle();					
					if (mVideoFile != null) 
						mVideoFile.delete();
				}
			}.start();
		}
	}
	
	private class DownloadRocketPictureTask extends AsyncTask<String, Void, Bitmap> {
		public DownloadRocketPictureTask() { }		
		@Override
		protected Bitmap doInBackground(String... rocketPictureUrl) {
			try {
				InputStream in = new java.net.URL(rocketPictureUrl[0]).openStream();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				options.inScaled = false;
				mBitmapHolder = BitmapFactory.decodeStream(in, null, options);
				in.close();				
			} catch (Exception ex) {
				LensRocketLogger.e(TAG, "Error pulling down rocket for url: " + rocketPictureUrl[0]);				
			}
			return mBitmapHolder;
		}
		
		protected void onPostExecute(Bitmap rocketImage) {
			mImagePicture.setImageBitmap(rocketImage);			
		}
	}	
	
	private class DownloadRocketVideoTask extends AsyncTask<String, Void, File> {
		private Rocket mRocket;		
		public DownloadRocketVideoTask(final Rocket rocket) { mRocket = rocket; }		
		@Override
		protected File doInBackground(String... rocketVideoUrl) {
			try {
				InputStream in = new java.net.URL(rocketVideoUrl[0]).openStream();
				mVideoFile = new File(Environment.getExternalStoragePublicDirectory(
			              Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name));
			    // Create the storage directory if it does not exist
			    if (! mVideoFile.exists()){
			        if (! mVideoFile.mkdirs()){
			            LensRocketLogger.d(TAG, "failed to create directory");
			            return null;
			        }
			    }
			    mVideoFile = new File(mVideoFile.getPath() + File.separator + "tempvid.mp4");
				FileOutputStream fos = new FileOutputStream(mVideoFile);
				byte[] buffer = new byte[1024];
				int len1 = 0;
				while ((len1 = in.read(buffer)) != -1) {
					fos.write(buffer, 0, len1);
				}
				fos.close();
				in.close();			
			} catch (Exception ex) {
				LensRocketLogger.d(TAG, ex.getMessage());
				LensRocketLogger.e(TAG, "Error pulling down rocket video for url: " + rocketVideoUrl[0]);				
			}
			return mVideoFile;
		}
		
		protected void onPostExecute(File rocketVideo) {
			LensRocketLogger.d(TAG, "Set video URI");
			mVideoView.setVideoPath(rocketVideo.getAbsolutePath());			
			mVideoView.start();
			LensRocketLogger.d(TAG, "Add View");
			mVideoLayout.addView(mVideoView);
			mViewingDialog.setContentView(mVideoLayout);
			mViewingDialog.show();			
			startCountdownTimer(mRocket);			
		}
	}

	@Override
	protected void onResume() {		
		mIsViewingPicture = false;
		mIsViewingVideo = false;
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_ROCKETS_UPDATED);
		filter.addAction(Constants.BROADCAST_ROCKET_SENT);
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
			if (intent.getAction().equals(Constants.BROADCAST_ROCKETS_UPDATED)) {
				boolean wasSuccess = intent.getBooleanExtra(Constants.ROCKETS_UPDATE_STATUS, false);
				if (wasSuccess) {
					mAdapter.clear();			
					for (Rocket rocket : mLensRocketService.getLocalRockets()) {
						mAdapter.add(rocket);
					}		
					LensRocketLogger.i(TAG, "Refresh complete");
				} else {
					LensRocketAlert.showToast(mActivity, R.string.error_getting_rockets);
				}
				mPullToRefreshAttacher.setRefreshComplete();
				mPullToRefreshAttacher.setRefreshing(false);
			} else if (intent.getAction().equals(Constants.BROADCAST_ROCKET_SENT)) {
				mLensRocketService.getRockets();
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
		getMenuInflater().inflate(R.menu.rockets_list, menu);
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
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
			return true;		
		case R.id.menuSettings:			
			Intent intent = new Intent(mActivity, SettingsActivity.class);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		NavUtils.navigateUpFromSameTask(this);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}
	
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	};
	
	public class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mTappedRowPosition > -1) {				
				Rocket tappedRocket = mLensRocketService.getLocalRockets().get(mTappedRowPosition);
				if (tappedRocket.getType().equalsIgnoreCase("rocket") && tappedRocket.getHasUserSeen()) {
					LensRocketLogger.i(TAG, "DoubleTap row: " + mTappedRowPosition);
					Intent intent = new Intent(mActivity, RecordActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					intent.putExtra("isReply", true);					
					intent.putExtra("replyToUserId", tappedRocket.getFromUserId());
					startActivity(intent);					
				}
			}			
			return super.onDoubleTap(e);
		}
	}

}
