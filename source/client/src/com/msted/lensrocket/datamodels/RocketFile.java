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

package com.msted.lensrocket.datamodels;

import java.io.File;

public class RocketFile {
	
	@com.google.gson.annotations.SerializedName("isVideo")
	private boolean mIsVideo;
	@com.google.gson.annotations.SerializedName("isPicture")
	private boolean mIsPicture;
	@com.google.gson.annotations.SerializedName("ownerUsername")
	private String mOwnerUsername;
	@com.google.gson.annotations.SerializedName("sentMessageId")
	private String mSentMessageId;
	@com.google.gson.annotations.SerializedName("fileName")
	private String mFileName;
	@com.google.gson.annotations.SerializedName("blobPath")
	private String mBlobPath;
	@com.google.gson.annotations.SerializedName("id")
	private String mId;
	
	public RocketFile() {}
	public void setIsVideo(boolean isVideo) { mIsVideo = isVideo; }
	public void setIsPicture(boolean isPicture) { mIsPicture = isPicture; }
	public void setOwnerUsername(String ownerUsername) { mOwnerUsername = ownerUsername; }
	public void setSentMessageId(String sentMessageId) { mSentMessageId = sentMessageId; }
	
	public RocketFile(boolean isPicture, boolean isVideo, String ownerUsername, String sentMessageId, String filePath) {
		mIsVideo = isVideo;
		mIsPicture = isPicture;
		mOwnerUsername = ownerUsername;
		mSentMessageId = sentMessageId;
		File file = new File(filePath);		
		mFileName = file.getName();
	}
	
	public String getId() { return mId; } 
	public String getBlobPath() { return mBlobPath; }
	
	@Override
	public boolean equals(Object o) {
		return o instanceof RocketFile && ((RocketFile) o).mId == mId;
	}
}
