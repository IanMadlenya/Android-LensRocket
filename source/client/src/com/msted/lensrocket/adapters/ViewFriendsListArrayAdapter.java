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

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.msted.lensrocket.LensRocketService;
import com.msted.lensrocket.datamodels.Friend;

public class ViewFriendsListArrayAdapter extends ArrayAdapter<Friend> {
	private LensRocketService mLensRocketService;
	
	
	public ViewFriendsListArrayAdapter(Context context, 
			LensRocketService lensRocketService, List<Friend> friends) {
		super(context, 0, friends);
		mLensRocketService = lensRocketService;
	}
	
	public ViewFriendsListArrayAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext()
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.list_content, parent, false);    
	    TextView text1 = (TextView) view.findViewById(R.id.text1);
	    text1.setText(mLensRocketService.getLocalFriends().get(position).getToUsername());        
	    return view;
	}
}
