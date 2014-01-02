package com.msted.pikshare.activities;

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
import com.msted.pikshare.Constants;
import com.msted.pikshare.R;
import com.msted.pikshare.adapters.PiksArrayAdapter;
import com.msted.pikshare.base.BaseActivity;
import com.msted.pikshare.datamodels.Pik;
import com.msted.pikshare.util.NoNetworkConnectivityException;
import com.msted.pikshare.util.PikShareAlert;
import com.msted.pikshare.util.PikShareLogger;
import com.msted.pikshare.util.PikShareResponse;

public class PiksListActivity extends BaseActivity implements PullToRefreshAttacher.OnRefreshListener {
	
	private final String TAG = "PiksListActivity";
	private ListView mLvPiks;
	private PiksArrayAdapter mAdapter;	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private boolean mIsViewingPicture;
	private boolean mIsViewingVideo;
	private Dialog mViewingDialog;
	private ImageView mImagePicture;
	private VideoView mVideoView;
	private GestureDetector mGestureDetector;
	private int mTappedRowPosition = -1;
	private PullToRefreshLayout mLayoutPiks;
	private File mVideoFile;
	private RelativeLayout mVideoLayout;	
	private Bitmap mBitmapHolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);		
		super.onCreate(savedInstanceState, true);
		setContentView(R.layout.activity_piks_list);
		// Show the Up button in the action bar.
		setupActionBar();
		mLvPiks = (ListView) findViewById(R.id.lvPiks);
		mLvPiks.setEmptyView(findViewById(android.R.id.empty));
		mLayoutPiks = (PullToRefreshLayout) findViewById(R.id.layoutPiks);
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
		mLayoutPiks.setPullToRefreshAttacher(mPullToRefreshAttacher, this);		
		mGestureDetector = new GestureDetector(this, new GestureListener());
		mAdapter = new PiksArrayAdapter(this,  mPikShareService.getLocalPiks());
		mLvPiks.setAdapter(mAdapter);			
		mLvPiks.setOnItemClickListener(pikClickListener);
		mLvPiks.setOnItemLongClickListener(pikLongClickListener);
		
		mLvPiks.setOnTouchListener(new OnTouchListener() {			
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
	
	private OnItemClickListener pikClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mTappedRowPosition = position;
		}		
	};
	
	@Override
    public void onRefreshStarted(View view) {
		PikShareLogger.i(TAG, "onRefreshStarted");
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
                mPikShareService.getPiks();
                // Notify PullToRefreshAttacher that the refresh has finished
                mPullToRefreshAttacher.setRefreshComplete();
            }
        }.execute();		
    }
	
	private OnItemLongClickListener pikLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			final Pik pik = mPikShareService.getLocalPiks().get(position);
			if (pik.getType().equals("FriendRequest")) {
				//Friend and update the pik
				mPikShareService.acceptFriendRequest(pik, new ApiOperationCallback<PikShareResponse>() {					
					@Override
					public void onCompleted(PikShareResponse response, Exception ex,
							ServiceFilterResponse serviceFilterResponse) {
						PikShareLogger.i(TAG, "Response received");
						if (ex != null || response.Error != null) {																
							if (ex != null) {
								if (NoNetworkConnectivityException.class.isInstance(ex))
									return;	
								PikShareAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
							}
							else 
								PikShareAlert.showToast(mActivity, response.Error);
						} else {
							mPikShareService.getFriends();
							int position = mPikShareService.getLocalPiks().indexOf(pik);
							View view = mLvPiks.getChildAt(position);
							ImageView imgIndicator = (ImageView) view.findViewById(R.id.imgIndicator);
							imgIndicator.setImageResource(R.drawable.pik_accepted_friend_request);
						}
					}
				});
			} else if (pik.getType().equals("Pik")) {				
				if (pik.getHasUserSeen()) {
					//Do nothing, they should double tap to reply
				} else {
					//Get SharedAccessSignature for pik
					mPikShareService.getPikForRecipient(pik, new ApiOperationCallback<PikShareResponse>() {
						@Override
						public void onCompleted(PikShareResponse response,
								Exception ex, ServiceFilterResponse serviceFilterResponse) {
							if (ex != null || response.Error != null) {								
								if (ex != null)  {
									if (NoNetworkConnectivityException.class.isInstance(ex))
										return;								
									PikShareAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
								}
								else
									PikShareAlert.showSimpleErrorDialog(mActivity, response.Error);																	
							} else {
								PikShareLogger.d(TAG, response.PikUrl);
								//Display the pik depending on the type
								mViewingDialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar);
								if (pik.getIsPicture()) {									
									mIsViewingPicture = true;
									if (mImagePicture == null) 
										mImagePicture = new ImageView(mActivity);
									new DownloadPikPictureTask().execute(response.PikUrl);									
									mViewingDialog.setContentView(mImagePicture);									
									mViewingDialog.show();									
									//Start countdown								
									startCountdownTimer(pik);
									
								} else if (pik.getIsVideo()) {
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
												PikShareLogger.i(TAG, "video view prepared");
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
									new DownloadPikVideoTask(pik).execute(response.PikUrl);									
								}
							}
						}
					});
				}
			}
			return false;
		}		
	};
	
	private void startCountdownTimer(final Pik pik) {
		int position = mPikShareService.getLocalPiks().indexOf(pik);
		if (pik == null)
			PikShareLogger.i(TAG, "Pik is null");
		PikShareLogger.i(TAG, "Position: " + position);
		if (mLvPiks == null)
			PikShareLogger.i(TAG, "mlvpiks is null");
		View view = mLvPiks.getChildAt(position);		
		final TextView lblTime = (TextView) view.findViewById(R.id.lblTime);
		//Only start a countdown if we haven't already
		if (lblTime.getText().equals("")) {
			final ImageView imgIndicator = (ImageView) view.findViewById(R.id.imgIndicator);
			final TextView lblInstructions = (TextView) view.findViewById(R.id.lblInstructions);
			int timeToLive = pik.getTimeToLive();
			lblTime.setText(timeToLive + "");			
			new CountDownTimer(timeToLive * 1000, 1000) {
				public void onTick(long millisUntilFinished) {
					lblTime.setText(millisUntilFinished / 1000 + "");
				}
				
				public void onFinish() {
					imgIndicator.setImageResource(R.drawable.pik_seen);
					lblTime.setText(R.string.empty_string);
					lblInstructions.setText(R.string.instructions_seen_pik);
					pik.setHasUserSeen(true);
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
	
	private class DownloadPikPictureTask extends AsyncTask<String, Void, Bitmap> {
		public DownloadPikPictureTask() { }		
		@Override
		protected Bitmap doInBackground(String... pikPictureUrl) {
			try {
				InputStream in = new java.net.URL(pikPictureUrl[0]).openStream();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				options.inScaled = false;
				mBitmapHolder = BitmapFactory.decodeStream(in, null, options);
				in.close();				
			} catch (Exception ex) {
				PikShareLogger.e(TAG, "Error pulling down pik for url: " + pikPictureUrl[0]);				
			}
			return mBitmapHolder;
		}
		
		protected void onPostExecute(Bitmap pikImage) {
			mImagePicture.setImageBitmap(pikImage);			
		}
	}	
	
	private class DownloadPikVideoTask extends AsyncTask<String, Void, File> {
		private Pik mPik;		
		public DownloadPikVideoTask(final Pik pik) { mPik = pik; }		
		@Override
		protected File doInBackground(String... pikVideoUrl) {
			try {
				InputStream in = new java.net.URL(pikVideoUrl[0]).openStream();
				mVideoFile = new File(Environment.getExternalStoragePublicDirectory(
			              Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name));
			    // Create the storage directory if it does not exist
			    if (! mVideoFile.exists()){
			        if (! mVideoFile.mkdirs()){
			            PikShareLogger.d(TAG, "failed to create directory");
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
				PikShareLogger.d(TAG, ex.getMessage());
				PikShareLogger.e(TAG, "Error pulling down pik video for url: " + pikVideoUrl[0]);				
			}
			return mVideoFile;
		}
		
		protected void onPostExecute(File pikVideo) {
			PikShareLogger.d(TAG, "Set video URI");
			mVideoView.setVideoPath(pikVideo.getAbsolutePath());			
			mVideoView.start();
			PikShareLogger.d(TAG, "Add View");
			mVideoLayout.addView(mVideoView);
			mViewingDialog.setContentView(mVideoLayout);
			mViewingDialog.show();			
			startCountdownTimer(mPik);			
		}
	}

	@Override
	protected void onResume() {		
		mIsViewingPicture = false;
		mIsViewingVideo = false;
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_PIKS_UPDATED);
		filter.addAction(Constants.BROADCAST_PIK_SENT);
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
			if (intent.getAction().equals(Constants.BROADCAST_PIKS_UPDATED)) {
				boolean wasSuccess = intent.getBooleanExtra(Constants.PIKS_UPDATE_STATUS, false);
				if (wasSuccess) {
					mAdapter.clear();			
					for (Pik pik : mPikShareService.getLocalPiks()) {
						mAdapter.add(pik);
					}		
					PikShareLogger.i(TAG, "Refresh complete");
				} else {
					PikShareAlert.showToast(mActivity, R.string.error_getting_piks);
				}
				mPullToRefreshAttacher.setRefreshComplete();
				mPullToRefreshAttacher.setRefreshing(false);
			} else if (intent.getAction().equals(Constants.BROADCAST_PIK_SENT)) {
				mPikShareService.getPiks();
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
		getMenuInflater().inflate(R.menu.piks_list, menu);
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
				Pik tappedPik = mPikShareService.getLocalPiks().get(mTappedRowPosition);
				if (tappedPik.getType().equalsIgnoreCase("pik") && tappedPik.getHasUserSeen()) {
					PikShareLogger.i(TAG, "DoubleTap row: " + mTappedRowPosition);
					Intent intent = new Intent(mActivity, RecordActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					intent.putExtra("isReply", true);					
					intent.putExtra("replyToUserId", tappedPik.getFromUserId());
					startActivity(intent);					
				}
			}			
			return super.onDoubleTap(e);
		}
	}

}
