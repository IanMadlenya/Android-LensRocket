package com.msted.pikshare.adapters;

import java.util.List;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.msted.pikshare.PikShareService;
import com.msted.pikshare.datamodels.Friend;

public class ViewFriendsListArrayAdapter extends ArrayAdapter<Friend> {
	private PikShareService mPikShareService;
	
	
	public ViewFriendsListArrayAdapter(Context context, 
			PikShareService pikShareService, List<Friend> friends) {
		super(context, 0, friends);
		mPikShareService = pikShareService;
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
	    text1.setText(mPikShareService.getLocalFriends().get(position).getToUsername());        
	    return view;
	}
}
