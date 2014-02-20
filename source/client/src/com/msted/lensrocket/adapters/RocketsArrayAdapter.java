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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.msted.lensrocket.datamodels.Rocket;
import com.msted.lensrocket.R;

public class RocketsArrayAdapter extends ArrayAdapter<Rocket> {
	private Context mContext;
	private List<Rocket> mRockets;
	
	public RocketsArrayAdapter(Context context, List<Rocket> rockets) {		
		super(context, R.layout.list_row_rocket, rockets);
		mContext = context;
		mRockets = rockets;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Rocket rocket = mRockets.get(position);
		LayoutInflater inflater = (LayoutInflater) mContext
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.list_row_rocket, parent, false);    
	    TextView lblFromUsername = (TextView) view.findViewById(R.id.lblFromUsername);
	    lblFromUsername.setText(rocket.getFromUsername());
	    TextView lblDateSent = (TextView) view.findViewById(R.id.lblDateSent);
	    Date createDate = rocket.getCreateDate();
	    lblDateSent.setText(DateFormat.getDateInstance().format(createDate));
	    TextView lblInstructions = (TextView) view.findViewById(R.id.lblInstructions);
	    ImageView imgIndicator = (ImageView) view.findViewById(R.id.imgIndicator);
	    //imgIndicator.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
	    if (rocket.getType().equals("FriendRequest")) {
	    		if (rocket.getHasUserSeen()) {
	    			imgIndicator.setImageResource(R.drawable.rocket_accepted_friend_request);
		    		lblInstructions.setText(mContext.getResources().getString(R.string.instructions_accepted_friend_request));
	    		} else {
	    			imgIndicator.setImageResource(R.drawable.rocket_friend_request);
		    		lblInstructions.setText(mContext.getResources().getString(R.string.instructions_friend_request));
	    		}
	    }
	    else if (rocket.getType().equals("Rocket")) {
	    		if (rocket.getHasUserSeen()) {
	    			imgIndicator.setImageResource(R.drawable.rocket_seen);
	    			lblInstructions.setText(mContext.getResources().getString(R.string.instructions_seen_rocket));
	    		}
	    		else {
	    			imgIndicator.setImageResource(R.drawable.rocket_not_seen);
	    			lblInstructions.setText(mContext.getResources().getString(R.string.instructions_unseen_rocket));
	    		}
	    } else if (rocket.getType().equals("SENT")) {
	    		if (rocket.getAllUsersHaveSEen()) {
	    			imgIndicator.setImageResource(R.drawable.rocket_sent_and_seen_message);
	    			lblInstructions.setText(mContext.getResources().getString(R.string.opened));
	    		}
	    		else {
	    			imgIndicator.setImageResource(R.drawable.rocket_sent_message);
		    		if (rocket.getDelivered())
		    			lblInstructions.setText(mContext.getResources().getString(R.string.delivered));
		    		else
		    			lblInstructions.setText(mContext.getResources().getString(R.string.sending));
	    		}	    		
	    }
	    return view;
	}
}
