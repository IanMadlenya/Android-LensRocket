package com.msted.pikshare.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.msted.pikshare.CameraPreview;
import com.msted.pikshare.Constants;
import com.msted.pikshare.Constants.CameraUIMode;
import com.msted.pikshare.PreferencesHandler;
import com.msted.pikshare.R;
import com.msted.pikshare.base.BaseActivity;
import com.msted.pikshare.util.DeviceDetector;
import com.msted.pikshare.util.NetworkUtilities;
import com.msted.pikshare.util.PikShareAlert;
import com.msted.pikshare.util.PikShareLogger;

public class RecordActivity extends BaseActivity implements NumberPicker.OnValueChangeListener {
	
	private final String TAG = "RecordActivity";
	private Camera mCamera;
	private CameraPreview mCameraPreview;
	private VideoView mVideoView;
	private ImageView mImageView;
	private MediaRecorder mMediaRecorder;
	private ImageButton mBtnSwitchCamera;
	private ImageButton mBtnFlash;
	private ImageButton mBtnTakePicture;
	private ImageButton mBtnPiks;
	private ImageButton mBtnFriends;
	private ImageButton mBtnSend;
	private ImageButton mBtnDelete;
	private TextView mLblTime;
	private RelativeLayout mLayoutTime;
	private int mCameraNumber;
	private boolean mFlashOn;
	private boolean mTakingVideo;
	private boolean mReviewingPicture;
	private boolean mReviewingVideo;
	private String  mVideoFileName;
	private String  mFileFullPath;
	private File mMediaStorageDir;
	private FrameLayout mFrameLayout;
	private byte[] mPictureData;
	private boolean mIsScrolling;
	private int     mSecondsSelected;
	private boolean mIsReply;
	private boolean mIsSending = false;
	private String mReplyToUserId;
	private Bitmap mBitmapHolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_record);
		mBtnSwitchCamera = (ImageButton) findViewById(R.id.btnSwitchCameras);
		mBtnFlash = (ImageButton) findViewById(R.id.btnFlash);
		mBtnTakePicture = (ImageButton) findViewById(R.id.btnTakePicture);
		mBtnPiks = (ImageButton) findViewById(R.id.btnPiks);
		mBtnFriends = (ImageButton) findViewById(R.id.btnFriends);
		mVideoView = (VideoView) findViewById(R.id.videoView);
		mImageView = (ImageView) findViewById(R.id.pictureView2);
		mBtnSend = (ImageButton) findViewById(R.id.btnSend);
		mBtnDelete = (ImageButton) findViewById(R.id.btnDelete);
		mLayoutTime = (RelativeLayout) findViewById(R.id.layoutTime);		
		mLblTime = (TextView) findViewById(R.id.lblTime);		
		mBtnTakePicture.setOnClickListener(takePictureListener);
		mBtnTakePicture.setOnLongClickListener(takeVideoListener);
		mBtnTakePicture.setOnTouchListener(touchListener);
		if (NetworkUtilities.isNetworkOnline(mActivity)) {
			mPikShareService.getFriends();			
			mPikShareService.getPiks();
			mPikShareService.getPreferences();
		} else {
			PikShareAlert.showSimpleErrorDialog(mActivity, "You should connect to the internet and rerun PikShare.");
		}
		mTakingVideo = false;
		mReviewingPicture = false;
		mReviewingVideo = false;
		mIsScrolling = false;
		mSecondsSelected = 3;		
		mLblTime.setText(mSecondsSelected + "");
		//TODO: Consider moving to background task
		mPikShareService.registerForPush();
	}
	
	private OnClickListener takePictureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			PikShareLogger.i(TAG, "TakePic");			
			mCamera.takePicture(null, null, mPictureCallback);
			//Hide UI
			setUIMode(Constants.CameraUIMode.UI_MODE_TAKING_PICTURE);
		}	
	};
	
	private void setUIMode(Constants.CameraUIMode uiMode) {
		switch (uiMode) {
		case UI_MODE_REPLYING:
			mBtnFlash.setVisibility(View.VISIBLE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPiks.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.VISIBLE);
			mBtnTakePicture.setVisibility(View.VISIBLE);
			mBtnSend.setVisibility(View.GONE);
			mBtnDelete.setVisibility(View.GONE);
			mLayoutTime.setVisibility(View.GONE);			
			mCameraPreview.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.GONE);
			mImageView.setVisibility(View.GONE);
			break;
		case UI_MODE_PRE_PICTURE:
			mBtnFlash.setVisibility(View.VISIBLE);
			mBtnFriends.setVisibility(View.VISIBLE);
			mBtnPiks.setVisibility(View.VISIBLE);
			mBtnSwitchCamera.setVisibility(View.VISIBLE);
			mBtnTakePicture.setVisibility(View.VISIBLE);
			mBtnSend.setVisibility(View.GONE);
			mBtnDelete.setVisibility(View.GONE);
			mLayoutTime.setVisibility(View.GONE);			
			mCameraPreview.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.GONE);
			mImageView.setVisibility(View.GONE);
			break;
		case UI_MODE_REVIEW_PICTURE:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPiks.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			mBtnSend.setVisibility(View.VISIBLE);
			mBtnDelete.setVisibility(View.VISIBLE);
			mLayoutTime.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.GONE);						
			break;
		case UI_MODE_REVIEW_VIDEO:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPiks.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			mBtnSend.setVisibility(View.VISIBLE);
			mBtnDelete.setVisibility(View.VISIBLE);
			mLayoutTime.setVisibility(View.VISIBLE);
			if (mVideoView == null)
				mVideoView = new VideoView(this);		
			mVideoView.setVideoPath(mFileFullPath);
			mCameraPreview.setVisibility(View.GONE);
			mVideoView.setVisibility(View.VISIBLE);		    
		    mVideoView.setOnPreparedListener(new OnPreparedListener() {				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);					
				}
			});
			mVideoView.start();
			break;
		case UI_MODE_TAKING_PICTURE:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPiks.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			mBtnSend.setVisibility(View.GONE);
			break;
		case UI_MODE_TAKING_VIDEO:
			mBtnFlash.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);	
			mBtnSend.setVisibility(View.GONE);
			break;
		}
	}
	
	private OnLongClickListener takeVideoListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			PikShareLogger.i(TAG, "Video start");			
			mTakingVideo = true;
			if (prepareVideoRecorder()) {				
				mMediaRecorder.start();
				setUIMode(Constants.CameraUIMode.UI_MODE_TAKING_VIDEO);
			} else {
				//TODO: show an error to the user
				releaseMediaRecorder();
			}
			return true;
		}		
	};
	
	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;			
		}
	}
	
	private boolean prepareVideoRecorder() {
		/**
		 * This code fixes an issue with the Galaxy S3 where you need to
		 * lock and release the camera in order for recording video to work
		 * Unfortunately, this breaks play back of videos on the Galaxy Nexus
		 * The suggested fix was found here:
		 * http://stackoverflow.com/questions/12696318/video-display-is-garbled-when-recording-on-galaxy-s3
		 **/
		if (DeviceDetector.isDeviceS3()) {
			mCamera.stopPreview();
			mCamera.lock();
			mCamera.release();
			mCamera = Camera.open(mCameraNumber);
			mCamera.setDisplayOrientation(90);
		}
		mMediaRecorder = new MediaRecorder();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);		
		mMediaRecorder.setProfile(CamcorderProfile.get(mCameraNumber, CamcorderProfile.QUALITY_HIGH));		
		mMediaRecorder.setOutputFile(getOutputMediaFile(Constants.MEDIA_TYPE_VIDEO).toString());
		mMediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());
		mMediaRecorder.setOrientationHint(90);		
		DisplayMetrics metrics = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		PikShareLogger.d(TAG, "Width: " + metrics.widthPixels);
		PikShareLogger.d(TAG, "Height: " + metrics.heightPixels);
		mMediaRecorder.setOnErrorListener(new OnErrorListener() {							
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				PikShareLogger.e(TAG, "MediaRecorder error");				
			}
		});				
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException ex) {
			PikShareLogger.e(TAG, "IllegalStateException preparing MediaRecorder: " + ex.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException ex) {
			PikShareLogger.e(TAG, "IOException preparing MediaRecorder: " + ex.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}
	
	private OnTouchListener touchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
	        } else if (event.getAction() == MotionEvent.ACTION_UP) {	        		
	        		if (mTakingVideo) {
	        			PikShareLogger.i(TAG, "Finished video");
	        			mTakingVideo = false;
	        			mReviewingVideo = true;
	        			try {
		        			mMediaRecorder.stop();		        			
		        			releaseMediaRecorder();	        			
		        			setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_VIDEO);
	        			} catch (RuntimeException ex) {
	        				PikShareLogger.e(TAG, "Error stopping media recorder");
	        				PikShareAlert.showToast(mActivity, R.string.video_recording_failed, true, true);
	        				setUIMode(Constants.CameraUIMode.UI_MODE_PRE_PICTURE);
	        			}
	        		}
	        }
			return false;
		}		
	};
	
	/**
	 * Method to rotate bitmap as found here:
	 * http://stackoverflow.com/questions/13430895/capture-photo-rotate-90-degree-in-samsung-mobile
	 * @param bitmap
	 * @param degrees
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
	    Matrix matrix = new Matrix();
	    if (degrees != 0) {
	        // rotate clockwise
	        matrix.postRotate(degrees, (float) bitmap.getWidth() / 2,
	                (float) bitmap.getHeight() / 2);
	    }
	    try {
	        Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
	                bitmap.getHeight(), matrix, true);
	        if (bitmap != b2) {
	            bitmap.recycle();
	            bitmap = b2;
	        }
	    } catch (OutOfMemoryError ex) {
	        // We have no memory to rotate. Return the original bitmap.
	    }
	    return bitmap;
	}
	
	private PictureCallback mPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			PikShareLogger.i(TAG, "pic taken");
			if (mTakingVideo) {
				mReviewingVideo = true;
				setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_VIDEO);
			}
			else {
				/** 
				 * The Galaxy S3 has an issue where it doens't properly rotate
				 * the photos like other devices do.  This code will check
				 * to see if the current device is an S3 and if so, will
				 * manually rotate the bitmap.  It's a significant amount of 
				 * processing though that should be moved to a background thread
				 * if possible. 
				 **/
				//TODO: try to move this to a background thread
		       if (DeviceDetector.isDeviceS3()) {										
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.RGB_565;
					options.inScaled = false;				
					Bitmap newTempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
					if (newTempBitmap.getWidth() > newTempBitmap.getHeight()) {
						PikShareLogger.e(TAG, "Width greater than height!");
						newTempBitmap = rotateBitmap(newTempBitmap, 90);
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						newTempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
						data = stream.toByteArray();		
						newTempBitmap.recycle();
					}
		       }
		       /** END OF SAMSUNG GALAXY S3 FIX **/				
				mReviewingPicture = true;
				mPictureData = data;
				setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_PICTURE);				
				File pictureFile = getOutputMediaFile(Constants.MEDIA_TYPE_IMAGE);
				if (pictureFile == null) {
					PikShareLogger.d(TAG, "Error creating media file, check storage permissions");
				}
				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException ex) {
					PikShareLogger.d(TAG, "File not found: " + ex.getMessage());
				} catch (IOException ex) {
					PikShareLogger.d(TAG, "Error accessing file: " + ex.getMessage());
				}
			}
		}
	};
	
	//This method will be fired if RecordActivity is opened from an intent
	//This helps because it will be called if it's opened a second time from
	//intent which is the case when the user double taps a pik to reply
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mIsReply = intent.getBooleanExtra("isReply", false);
		if (mIsReply) {
			mReplyToUserId = intent.getStringExtra("replyToUserId"); 
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		PikShareLogger.i(TAG, "onResume");		
		int numberOfCams = Camera.getNumberOfCameras();
		mCameraNumber = 0;		
		if (numberOfCams <2 )
			mBtnSwitchCamera.setVisibility(View.GONE);
		else 
			mCameraNumber = PreferencesHandler.GetCameraPreference(getApplicationContext());
		mCamera = getCameraInstance(mCameraNumber);		
		mCameraPreview = new CameraPreview(this, mCamera);
		mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mFrameLayout.addView(mCameraPreview);
		mFlashOn = PreferencesHandler.GetFlashPreference(getApplicationContext());
		Camera.Parameters params = mCamera.getParameters();
		List<String> flashModes = params.getSupportedFlashModes();
		params.setRotation(90);
		if (mFlashOn) {
			mBtnFlash.setImageResource(R.drawable.device_access_flash_on);
		} else {
			mBtnFlash.setImageResource(R.drawable.device_access_flash_off);
		}
		if (flashModes == null || flashModes.size() == 0) {
			mBtnFlash.setVisibility(View.GONE);
		} else {
			setCameraFlash(params);
		}
		if (mReviewingPicture) {		
			if (mIsSending) {
				mIsSending = false;
				mCameraPreview.setIsReviewing(true);
				if (mBitmapHolder == null || mBitmapHolder.isRecycled()) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.RGB_565;
					options.inScaled = false;
					mBitmapHolder = BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length, options);
				}
				mImageView.setImageBitmap(mBitmapHolder);
				mImageView.setVisibility(View.VISIBLE);
			}
			//TODO: Set preview to show picture from file path
			PikShareLogger.i(TAG, "Path: " + mFileFullPath);			
			setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_PICTURE);
		} else if (mReviewingVideo) {
			mVideoView.setVideoPath(mFileFullPath);
			mVideoView.start();
			mVideoView.setVisibility(View.VISIBLE);
			mCameraPreview.setVisibility(View.GONE);
			setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_VIDEO);
		} else if (mIsReply){
			setUIMode(Constants.CameraUIMode.UI_MODE_REPLYING);
		} else {
			setUIMode(Constants.CameraUIMode.UI_MODE_PRE_PICTURE);
		}		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_PIKS_UPDATED);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		PikShareLogger.i(TAG, "onPause");
		mCameraPreview.getHolder().removeCallback(mCameraPreview);
		mCamera.release();
		unregisterReceiver(receiver);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			if (intent.getAction().equals(Constants.BROADCAST_PIKS_UPDATED)) {
				boolean wasSuccess = intent.getBooleanExtra(Constants.PIKS_UPDATE_STATUS, false);
				if (wasSuccess) {					
				} else {
					PikShareAlert.showToast(mActivity, R.string.error_getting_piks, true, true);
				}				
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	public Camera getCameraInstance(int cameraNumber) {
		Camera camera = null;
		try { 
			camera = Camera.open(cameraNumber);
		} catch (Exception ex) {
			PikShareLogger.e(TAG, ex.getMessage());
		}
		return camera;
	}
	
	public void tappedFlash(View view) {
		mFlashOn = !mFlashOn;
		PreferencesHandler.SaveFlashPreference(getApplicationContext(), mFlashOn);		
		if (mFlashOn) {
			mBtnFlash.setImageResource(R.drawable.device_access_flash_on);
		} else {
			mBtnFlash.setImageResource(R.drawable.device_access_flash_off);
		}
		Camera.Parameters params = mCamera.getParameters();
		setCameraFlash(params);
	}
	
	public void tappedSwitchCamera(View view) {
		mCamera.stopPreview();
		mCamera.release();
		if (mCameraNumber == 0)
			mCameraNumber = 1;
		else
			mCameraNumber = 0;
		mCamera = getCameraInstance(mCameraNumber);
		PreferencesHandler.SaveCameraPreference(getApplicationContext(), mCameraNumber);
		mCameraPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.removeAllViews();
		preview.addView(mCameraPreview);		
		Camera.Parameters params = mCamera.getParameters();
		List<String> flashModes = params.getSupportedFlashModes();
		if (flashModes == null || flashModes.size() == 0) {
			mBtnFlash.setVisibility(View.GONE);
		} else {
			mBtnFlash.setVisibility(View.VISIBLE);
			setCameraFlash(params);
		}
	}
	
	private void setCameraFlash(Camera.Parameters parameters) {
		if (mFlashOn) {
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			mCamera.setParameters(parameters);
		} else {
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(parameters);
		}			
	}
	
	public void tappedPiks(View view) {
		startActivity(new Intent(getApplicationContext(), PiksListActivity.class));		
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}
	
	public void tappedFriendsList(View view) {
		startActivity(new Intent(getApplicationContext(), FriendsListActivity.class));		
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}
	
	public void tappedSend(View view) {
		mIsSending = true;
		Intent intent = new Intent(mActivity, SendToFriendsActivity.class);
		intent.putExtra("filePath", mFileFullPath);
		intent.putExtra("isPicture", mReviewingPicture);
		intent.putExtra("isVideo", mReviewingVideo);
		intent.putExtra("timeToLive", mSecondsSelected);
		intent.putExtra("isReply", mIsReply);
		intent.putExtra("replyToUserId", mReplyToUserId);
		startActivityForResult(intent, Constants.REQUEST_CODE_SEND_TO_FRIENDS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		PikShareLogger.d(TAG, "onActivityResult");
		if (requestCode == Constants.REQUEST_CODE_SEND_TO_FRIENDS){
			if (resultCode == Constants.RESULT_CODE_PIK_SENT) {
				mImageView.setVisibility(View.GONE);
				setUIMode(CameraUIMode.UI_MODE_PRE_PICTURE);
				Intent intent = new Intent(mActivity, PiksListActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	public void tappedDelete(View view) {
		if (mBitmapHolder != null && !mBitmapHolder.isRecycled())
			mBitmapHolder.recycle();
		returnToCameraPreview();
	}
	
	public void tappedTime(View view) {
		DialogFragment newFragment = new NumberPickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}
	
	public void setIsScrolling(boolean isScrolling) {
		mIsScrolling = isScrolling;
	}
	
	public boolean getIsScrolling() {
		return mIsScrolling;
	}
	
	public static class NumberPickerFragment extends DialogFragment {		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			RecordActivity activity = (RecordActivity) getActivity();
			NumberPicker picker = new NumberPicker(activity);
			picker.setMinValue(1);
			picker.setMaxValue(10);
			picker.setWrapSelectorWheel(false);
			picker.setValue(activity.getSelectedSeconds());
			AlertDialog.Builder builder;
			builder = new AlertDialog.Builder(activity);
			builder.setView(picker);
			final AlertDialog dialog = builder.create();
			//Place the dialog at the bottom of the screen
			Window window = dialog.getWindow();			
			WindowManager.LayoutParams wlp = window.getAttributes();
			wlp.gravity = Gravity.BOTTOM;
			window.setAttributes(wlp);			
			//Record if we're scrolling for key down detection logic
			picker.setOnScrollListener(new OnScrollListener() {				
				@Override
				public void onScrollStateChange(NumberPicker view, int scrollState) {
					RecordActivity activity = (RecordActivity) getActivity();
					switch (scrollState) {
					case OnScrollListener.SCROLL_STATE_FLING:
						activity.setIsScrolling(true);
						break;
					case OnScrollListener.SCROLL_STATE_IDLE:
						activity.setIsScrolling(false);
						break;
					case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
						activity.setIsScrolling(true);
						break;
					}					
				}
			});
			picker.setOnValueChangedListener((RecordActivity) getActivity());
			picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
			picker.setDisplayedValues(getResources().getStringArray(R.array.share_seconds_list));
			picker.setOnTouchListener(new OnTouchListener() {				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					RecordActivity activity = (RecordActivity) getActivity();
					if (event.getAction() == MotionEvent.ACTION_UP) {
						if (!activity.getIsScrolling()) {
							//Check to see if they have clicked into the 
							//area specific to the middle row
							//This is a little hacky but NumberPicker's
							//onClickListener doesn't fire as expected
							float ry = event.getY();
							if (ry > 128 && ry < 256) {
								dialog.dismiss();
							}							
						}
					} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
						PikShareLogger.i("TEST", "down");
					}						
					return false;
				}
			});
			return dialog;			
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mReviewingPicture || mReviewingVideo) {
			returnToCameraPreview();
			if (mBitmapHolder != null && !mBitmapHolder.isRecycled())
				mBitmapHolder.recycle();
		} else { 						
			PikShareLogger.i(TAG, "back");
			if (mIsReply) {
				PikShareLogger.i(TAG, "back-reply");
				Intent intent = new Intent(mActivity, PiksListActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);				
		        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
			} else {			
				PikShareLogger.i(TAG, "back-finish");
				finish();
			}
		}
	}
	
	private void returnToCameraPreview() {
		if (mReviewingVideo) {
			mVideoView.stopPlayback();
			//Fix for S3 video recorder from here:
			//http://stackoverflow.com/questions/12696318/video-display-is-garbled-when-recording-on-galaxy-s3			
			mCamera.lock();
			mCamera.release();
			mCamera = getCameraInstance(mCameraNumber);
			mCameraPreview = new CameraPreview(this, mCamera);
			mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
			mFrameLayout.addView(mCameraPreview);									
			mCameraPreview.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.GONE);					
		}
		
		File file = new File(mFileFullPath);
		mFileFullPath = "";
		if (!file.delete()) {
			PikShareLogger.e(TAG, "Unable to delete file");
		}		
		//Ensure friends won't be checked when we return to them
		mPikShareService.uncheckFriends();
		mCamera.startPreview();
		mReviewingPicture = false;
		mReviewingVideo = false;
		if (mIsReply) {
			setUIMode(Constants.CameraUIMode.UI_MODE_REPLYING);
		} else {
			setUIMode(Constants.CameraUIMode.UI_MODE_PRE_PICTURE);
		}
	}	

	/** Create a File for saving an image or video */
	private  File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.		
	    mMediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name));
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.
	    // Create the storage directory if it does not exist
	    if (! mMediaStorageDir.exists()){
	        if (! mMediaStorageDir.mkdirs()){
	            PikShareLogger.d(TAG, "failed to create directory");
	            return null;
	        }
	    }
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == Constants.MEDIA_TYPE_IMAGE){
	    		String imageFileName = "IMG_"+ timeStamp + ".jpg";
	    		mFileFullPath = mMediaStorageDir.getPath() + File.separator +
	    		        imageFileName;
	        mediaFile = new File(mFileFullPath);
	    } else if(type == Constants.MEDIA_TYPE_VIDEO) {
	    		mVideoFileName = "VID_"+ timeStamp + ".mp4";
	    		mFileFullPath = mMediaStorageDir.getPath() + File.separator +
	    		        mVideoFileName;
	        mediaFile = new File(mFileFullPath);	        
	    } else {
	        return null;
	    }
	    return mediaFile;
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		PikShareLogger.i(TAG, "New value: " + newVal);
		mSecondsSelected = newVal;
		mLblTime.setText(newVal + "");
	}	
	
	public int getSelectedSeconds() {
		return mSecondsSelected;
	}
}
