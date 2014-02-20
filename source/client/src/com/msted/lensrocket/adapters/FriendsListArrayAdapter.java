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

package com.msted.lensrocket.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.msted.lensrocket.activities.FriendsListActivity;
import com.msted.lensrocket.datamodels.Friend;
import com.msted.lensrocket.R;

public class FriendsListArrayAdapter extends ArrayAdapter<Friend> {
	private FriendsListActivity mContext;
	private List<Friend> mFriends;
	
	public FriendsListArrayAdapter(FriendsListActivity context, List<Friend> friends) {		
		super(context, R.layout.list_row_friend, friends);
		mContext = context;
		mFriends = friends;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) mContext
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.list_row_friend, parent, false);
	    TextView lblUsername = (TextView) view.findViewById(R.id.lblUsername);
	    TextView lblInfo = (TextView) view.findViewById(R.id.lblInfo);
	    //Check to make sure we're not redrawing too fast
	    if (mFriends.size() <= position) {
	    		lblUsername.setText("");
	    		lblInfo.setText("");
			return view;
	    }
		Friend friend = mFriends.get(position);
	    
	    lblUsername.setText(friend.getDisplayName());
	    
	    if (friend.getStatus().equalsIgnoreCase("requested")) {
	    		lblInfo.setText(mContext.getResources().getString(R.string.pending));
	    } else {
	    		lblInfo.setText(friend.getToUsername());
	    }
	    return view;
	}
}
