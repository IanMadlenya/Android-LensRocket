package com.msted.pikshare;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.msted.pikshare.util.PikShareLogger;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean mIsReviewing = false;
    

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
        		PikShareLogger.d(TAG, "Surface created");
            mCamera.setPreviewDisplay(holder);    			
            mCamera.setDisplayOrientation(90);
            if (mIsReviewing == false) {
            		PikShareLogger.d(TAG, "Starting preview");
            		mCamera.startPreview();
            }            
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    } 

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview should be released in the activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
        		
            mCamera.setPreviewDisplay(mHolder);
            PikShareLogger.d(TAG, "Starting preview");
            if (mIsReviewing == false)            	
            		mCamera.startPreview();
            else {            		
            }
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }        
    }
    
    public boolean getIsReviewing() { return mIsReviewing; }
    public void setIsReviewing(boolean isReviewing) { mIsReviewing = isReviewing; }    
}
