package com.msted.pikshare.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.msted.pikshare.R;
import com.msted.pikshare.activities.SendToFriendsActivity;
import com.msted.pikshare.datamodels.Friend;

public class SendToFriendsArrayAdapter extends ArrayAdapter<Friend> {
	private SendToFriendsActivity mContext;
	private List<Friend> mFriends;
	
	public SendToFriendsArrayAdapter(SendToFriendsActivity context, List<Friend> friends) {		
		super(context, R.layout.list_row_send_to_friend, friends);
		mContext = context;
		mFriends = friends;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		Friend friend = mFriends.get(position);
		LayoutInflater inflater = (LayoutInflater) mContext
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.list_row_send_to_friend, parent, false);    
	    TextView lblUsername = (TextView) view.findViewById(R.id.lblUsername);
	    lblUsername.setText(friend.getToUsername());	    
	    CheckBox cbSelected = (CheckBox) view.findViewById(R.id.cbSelected);
	    if (cbSelected != null) {
	    		cbSelected.setChecked(friend.getChecked());
	    }	    
	    cbSelected.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mContext.updateRowCheck(position, isChecked);
			}
		});	    
	    return view;
	}
}
