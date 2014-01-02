package com.msted.pikshare.datamodels;

import java.io.File;

public class PikFile {
	
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
	
	public PikFile() {}
	public void setIsVideo(boolean isVideo) { mIsVideo = isVideo; }
	public void setIsPicture(boolean isPicture) { mIsPicture = isPicture; }
	public void setOwnerUsername(String ownerUsername) { mOwnerUsername = ownerUsername; }
	public void setSentMessageId(String sentMessageId) { mSentMessageId = sentMessageId; }
	
	public PikFile(boolean isPicture, boolean isVideo, String ownerUsername, String sentMessageId, String filePath) {
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
		return o instanceof PikFile && ((PikFile) o).mId == mId;
	}
}
