package com.msted.pikshare.datamodels;

import com.msted.pikshare.R;

public class UserPreferences {
	@com.google.gson.annotations.SerializedName("username")
	private String mUsername;
	@com.google.gson.annotations.SerializedName("mobileNumber")
	private String mMobileNumber;
	@com.google.gson.annotations.SerializedName("email")
	private String mEmail;
	@com.google.gson.annotations.SerializedName("receiveFrom")
	private String mReceiveFrom;
	@com.google.gson.annotations.SerializedName("shareTo")
	private String mShareTo;
	@com.google.gson.annotations.SerializedName("id")
	private String mId;
	
	public UserPreferences getCopy() {
		UserPreferences copy = new UserPreferences();
		copy.mUsername = mUsername;
		copy.mMobileNumber = mMobileNumber;
		copy.mEmail = mEmail;
		copy.mReceiveFrom = mReceiveFrom;
		copy.mShareTo = mShareTo;
		copy.mId = mId;
		return copy;
	}
	
	public String getId() { return mId; } 
	public String getUsername() { return mUsername; }
	public String getMobileNumber() { return mMobileNumber; }
	public void setMobileNumber(String mobileNumber) { mMobileNumber = mobileNumber; }
	public String getEmail() { return mEmail; }
	public void setEmail(String email) { mEmail = email; }
	public String getReceiveFrom() { return mReceiveFrom; }
	public void setReceiveFrom(String receiveFrom) { mReceiveFrom  = receiveFrom; }
	public String getShareTo() { return mShareTo; }
	public void setShareTo(String shareTo) { mShareTo = shareTo; }
	
	public void setValueForPreference(int preference, String value) {
		switch (preference) {
			case R.string.email_address:
				this.setEmail(value);
			break;
			case R.string.receive_piks_from:
				this.setReceiveFrom(value);
				break;
			case R.string.share_stories_to:
				this.setShareTo(value);
				break;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof UserPreferences && ((UserPreferences) o).mId == mId;
	}
}
