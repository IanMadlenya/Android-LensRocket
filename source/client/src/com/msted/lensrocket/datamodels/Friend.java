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
