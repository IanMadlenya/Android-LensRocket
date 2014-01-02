package com.msted.pikshare.datamodels;

import java.util.Date;

public class Pik {
	@com.google.gson.annotations.SerializedName("fromUserId")
	private String mFromUserId;
	@com.google.gson.annotations.SerializedName("toUserId")
	private String mToUserId;
	@com.google.gson.annotations.SerializedName("fromUsername")
	private String mFromUsername;
	@com.google.gson.annotations.SerializedName("type")
	private String mType;
	@com.google.gson.annotations.SerializedName("createDate")
	private Date mCreateDate;
	@com.google.gson.annotations.SerializedName("updateDate")
	private Date mUpdateDate;
	@com.google.gson.annotations.SerializedName("timeToLive")
	private int mTimeToLive;
	@com.google.gson.annotations.SerializedName("userHasSeen")
	private boolean mUserHasSeen;
	@com.google.gson.annotations.SerializedName("delivered")
	private boolean mDelivered;
	@com.google.gson.annotations.SerializedName("isVideo")
	private boolean mIsVideo;
	@com.google.gson.annotations.SerializedName("isPicture")
	private boolean mIsPicture;
	@com.google.gson.annotations.SerializedName("pikFileId")
	private String mPikFileId;	
	@com.google.gson.annotations.SerializedName("allUsersHaveSeen")
	private boolean mAllUsersHaveSeen;
	@com.google.gson.annotations.SerializedName("id")
	private String mId;

	public Pik() {}

	public String getId() { return mId; } 
	public String getFromUserId() { return mFromUserId; }
	public String getToUserId() { return mToUserId; }
	public String getFromUsername() { return mFromUsername; }
	public String getType() { return mType; }
	public Date getCreateDate() { return mCreateDate; }	
	public Date getUpdateDate() { return mUpdateDate; }
	public int getTimeToLive() { return mTimeToLive; }
	public boolean getHasUserSeen() { return mUserHasSeen; }
	public boolean getDelivered() { return mDelivered; }
	public boolean getIsPicture() { return mIsPicture; }
	public boolean getIsVideo() { return mIsVideo; }
	public String getPikFileId() { return mPikFileId; }
	public boolean getAllUsersHaveSEen() { return mAllUsersHaveSeen; }
	public void setHasUserSeen(boolean hasUserSeen) { mUserHasSeen = hasUserSeen; }
	
	public static Pik newSentPik(String userId, String username, int timeToLive, boolean isPicture, boolean isVideo) {
		Pik sentPik = new Pik();
		sentPik.mFromUserId = userId;
		sentPik.mToUserId = userId;
		sentPik.mFromUsername = username;
		sentPik.mType = "SENT";
		sentPik.mTimeToLive = timeToLive;
		sentPik.mDelivered = false;
		sentPik.mIsPicture = isPicture;
		sentPik.mIsVideo = isVideo;
		sentPik.mAllUsersHaveSeen = false;
		sentPik.mCreateDate = new Date();
		sentPik.mUpdateDate = sentPik.mCreateDate;
		return sentPik;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Pik && ((Pik) o).mId == mId;
	}
}
