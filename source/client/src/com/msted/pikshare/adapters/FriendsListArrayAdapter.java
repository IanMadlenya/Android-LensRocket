package com.msted.pikshare.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.msted.pikshare.R;
import com.msted.pikshare.activities.FriendsListActivity;
import com.msted.pikshare.datamodels.Friend;

public class FriendsListArrayAdapter extends ArrayAdapter<Friend> {
	private FriendsListActivity mContext;
	private List<Friend> mFriends;
	
	public FriendsListArrayAdapter(FriendsListActivity context, List<Friend> friends) {		
		super(context, R.layout.list_row_friend, friends);
		mContext = context;
		mFriends = friends;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		Friend friend = mFriends.get(position);
		LayoutInflater inflater = (LayoutInflater) mContext
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.list_row_friend, parent, false);    
	    TextView lblUsername = (TextView) view.findViewById(R.id.lblUsername);
	    lblUsername.setText(friend.getDisplayName());
	    TextView lblInfo = (TextView) view.findViewById(R.id.lblInfo);
	    if (friend.getStatus().equalsIgnoreCase("requested")) {
	    		lblInfo.setText(mContext.getResources().getString(R.string.pending));
	    } else {
	    		lblInfo.setText(friend.getToUsername());
	    }
	    return view;
	}
}
