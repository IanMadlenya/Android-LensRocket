package com.msted.pikshare.datamodels;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class Friend {
	
	@Expose
	@com.google.gson.annotations.SerializedName("fromUserId")
	private String mFromUserId;
	@Expose
	@com.google.gson.annotations.SerializedName("toUserId")
	private String mToUserId;
	@Expose
	@com.google.gson.annotations.SerializedName("toUsername")
	private String mToUsername;
	@Expose
	@com.google.gson.annotations.SerializedName("displayName")
	private String mDisplayName;
	@Expose
	@com.google.gson.annotations.SerializedName("status")
	private String mStatus;
	@Expose
	@com.google.gson.annotations.SerializedName("createDate")	
	private Date mCreateDate;
	@Expose
	@com.google.gson.annotations.SerializedName("id")
	private String mId;
	
	//Fields to ignore for serialization (not exposed)
	private boolean mIsChecked;
	
	public void setChecked(boolean checked) { mIsChecked = checked; }
	public boolean getChecked() { return mIsChecked; }

	public Friend() {
		mIsChecked = false;		
	}

	public String getId() { return mId; } 
	public String getFromUserId() { return mFromUserId; }
	public String getToUserId() { return mToUserId; }
	public String getToUsername() { return mToUsername; }
	public String getDisplayName() {
		if (mDisplayName != null && !mDisplayName.equals(""))
			return mDisplayName;
		return mToUsername;
	}
	public String getStatus() { return mStatus; }
	public Date getCreateDate() { return mCreateDate; }	
	
	public static Friend getSelfFriend(String username, String userId) {
		Friend self = new Friend();
		self.mToUsername = username + " (me)";
		self.mToUserId = userId;
		self.mFromUserId = userId;
		self.mStatus = "SELF";
		return self;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Friend && ((Friend) o).mId == mId;
	}
}
