package com.msted.pikshare.adapters;

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

import com.msted.pikshare.R;
import com.msted.pikshare.datamodels.Pik;

public class PiksArrayAdapter extends ArrayAdapter<Pik> {
	private Context mContext;
	private List<Pik> mPiks;
	
	public PiksArrayAdapter(Context context, List<Pik> piks) {		
		super(context, R.layout.list_row_pik, piks);
		mContext = context;
		mPiks = piks;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Pik pik = mPiks.get(position);
		LayoutInflater inflater = (LayoutInflater) mContext
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.list_row_pik, parent, false);    
	    TextView lblFromUsername = (TextView) view.findViewById(R.id.lblFromUsername);
	    lblFromUsername.setText(pik.getFromUsername());
	    TextView lblDateSent = (TextView) view.findViewById(R.id.lblDateSent);
	    Date createDate = pik.getCreateDate();
	    lblDateSent.setText(DateFormat.getDateInstance().format(createDate));
	    TextView lblInstructions = (TextView) view.findViewById(R.id.lblInstructions);
	    ImageView imgIndicator = (ImageView) view.findViewById(R.id.imgIndicator);
	    if (pik.getType().equals("FriendRequest")) {
	    		if (pik.getHasUserSeen()) {
	    			imgIndicator.setImageResource(R.drawable.pik_accepted_friend_request);
		    		lblInstructions.setText(mContext.getResources().getString(R.string.instructions_accepted_friend_request));
	    		} else {
	    			imgIndicator.setImageResource(R.drawable.pik_friend_request);
		    		lblInstructions.setText(mContext.getResources().getString(R.string.instructions_friend_request));
	    		}
	    }
	    else if (pik.getType().equals("Pik")) {
	    		if (pik.getHasUserSeen()) {
	    			imgIndicator.setImageResource(R.drawable.pik_seen);
	    			lblInstructions.setText(mContext.getResources().getString(R.string.instructions_seen_pik));
	    		}
	    		else {
	    			imgIndicator.setImageResource(R.drawable.pik_not_seen);
	    			lblInstructions.setText(mContext.getResources().getString(R.string.instructions_unseen_pik));
	    		}
	    } else if (pik.getType().equals("SENT")) {
	    		if (pik.getAllUsersHaveSEen()) {
	    			imgIndicator.setImageResource(R.drawable.pik_sent_and_seen_message);
	    			lblInstructions.setText(mContext.getResources().getString(R.string.opened));
	    		}
	    		else {
	    			imgIndicator.setImageResource(R.drawable.pik_sent_message);
		    		if (pik.getDelivered())
		    			lblInstructions.setText(mContext.getResources().getString(R.string.delivered));
		    		else
		    			lblInstructions.setText(mContext.getResources().getString(R.string.sending));
	    		}	    		
	    }
	    return view;
	}
}
