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

package com.msted.lensrocket;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.msted.lensrocket.util.LensRocketLogger;

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
        		LensRocketLogger.d(TAG, "Surface created");
            mCamera.setPreviewDisplay(holder);    			
            mCamera.setDisplayOrientation(90);
            if (mIsReviewing == false) {
            		LensRocketLogger.d(TAG, "Starting preview");
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
            LensRocketLogger.d(TAG, "Starting preview");
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
