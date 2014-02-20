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

import java.util.Date;

public class Rocket {
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
	@com.google.gson.annotations.SerializedName("rocketFileId")
	private String mRocketFileId;	
	@com.google.gson.annotations.SerializedName("allUsersHaveSeen")
	private boolean mAllUsersHaveSeen;
	@com.google.gson.annotations.SerializedName("id")
	private String mId;

	public Rocket() {}

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
	public String getRocketFileId() { return mRocketFileId; }
	public boolean getAllUsersHaveSEen() { return mAllUsersHaveSeen; }
	public void setHasUserSeen(boolean hasUserSeen) { mUserHasSeen = hasUserSeen; }
	
	public static Rocket newSentRocket(String userId, String username, int timeToLive, boolean isPicture, boolean isVideo) {
		Rocket sentRocket = new Rocket();
		sentRocket.mFromUserId = userId;
		sentRocket.mToUserId = userId;
		sentRocket.mFromUsername = username;
		sentRocket.mType = "SENT";
		sentRocket.mTimeToLive = timeToLive;
		sentRocket.mDelivered = false;
		sentRocket.mIsPicture = isPicture;
		sentRocket.mIsVideo = isVideo;
		sentRocket.mAllUsersHaveSeen = false;
		sentRocket.mCreateDate = new Date();
		sentRocket.mUpdateDate = sentRocket.mCreateDate;
		return sentRocket;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Rocket && ((Rocket) o).mId == mId;
	}
}
