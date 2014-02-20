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

package com.msted.lensrocket.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.msted.lensrocket.Constants;
import com.msted.lensrocket.LensRocketApplication;
import com.msted.lensrocket.LensRocketService;
import com.msted.lensrocket.R;
import com.msted.lensrocket.datamodels.UserPreferences;
import com.msted.lensrocket.util.LensRocketAlert;
import com.msted.lensrocket.util.LensRocketLogger;
import com.msted.lensrocket.util.NoNetworkConnectivityException;

public class SettingsActivity extends Activity {
	//private final String TAG = "SettingsActivity";
	private SettingsFragment mSettingsFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();
		mSettingsFragment = new SettingsFragment();
		
		Display display = getWindowManager().getDefaultDisplay();
	    DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

	    float density  = getResources().getDisplayMetrics().density;
	    float dpHeight = outMetrics.heightPixels / density;
	    float dpWidth  = outMetrics.widthPixels / density;
	    
	    int actionBarHeight;
	    TypedValue tv = new TypedValue();
	    if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
	    {
	        actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
	    }
		
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, mSettingsFragment)
		.commit();
	}	
	
	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		private final String TAG = "SettingsActivity";
		private LensRocketService mLensRocketService;
		private boolean mIsResettingValue;
		private Context mActivity;
		
		@Override
		public void onResume() {
			super.onResume();
			LensRocketLogger.d(TAG, "settings fragment on resume");
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}
		 
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);			
			mIsResettingValue = false;
			mActivity = getActivity();
			addPreferencesFromResource(R.xml.preferences);
			LensRocketApplication app = (LensRocketApplication) getActivity().getApplication();
			mLensRocketService = app.getLensRocketService();			
			initializeToDefaults();
			
						
			
			//getActivity().setTheme(R.style.ActionBarStyle);
			
			Preference privacyPref = (Preference) findPreference(getString(R.string.privacy_policy));
			privacyPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent webIntent = new Intent(getActivity(), WebWrapperActivity.class);
					webIntent.putExtra("url", Constants.PRIVACY_POLICY_URL);
					webIntent.putExtra("title", getResources().getString(R.string.privacy_policy));
					startActivity(webIntent);
					return true;
				}
			});
			
			Preference termsPref = (Preference) findPreference(getString(R.string.terms_of_use));
			termsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent webIntent = new Intent(getActivity(), WebWrapperActivity.class);
					webIntent.putExtra("url", Constants.TERMS_AND_CONDITIONS_URL);
					webIntent.putExtra("title", getResources().getString(R.string.terms_of_use));
					startActivity(webIntent);
					return true;
				}
			});
			
			Preference logoutPref = (Preference) findPreference(getString(R.string.log_out));
			logoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					mLensRocketService.logout(true);
					return true;
				}
			});			
		}

		@Override
		public void onSharedPreferenceChanged(
				final SharedPreferences sharedPreferences, final String key) {
			LensRocketLogger.d(TAG, "Preference changed for key: " + key);
			if (mIsResettingValue) {
				mIsResettingValue = false;
				return;
			}			
			final Preference myPref = findPreference(key);						
			final Resources resources = getActivity().getResources();
			final UserPreferences localPreferences = mLensRocketService.getLocalPreferences();
			String oldValue = "";
			String newValue = "";
			int preferenceId = 0;			
			if (key == resources.getString(R.string.email_address)) {
				preferenceId = R.string.email_address;
				oldValue = localPreferences.getEmail();
				newValue = sharedPreferences.getString(key, "");
				if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newValue).matches()) {
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(key, oldValue);
					LensRocketAlert.showToast(getActivity(), "That email address is invalid!");
					mIsResettingValue = true;
					editor.commit();
					EditTextPreference editPref = (EditTextPreference) myPref;
					editPref.setText(oldValue);
					return;
				}
			} else if (key == resources.getString(R.string.receive_rockets_from)) {
				preferenceId = R.string.receive_rockets_from;
				oldValue = localPreferences.getReceiveFrom();
				newValue = sharedPreferences.getString(key, "");
			} else if (key == resources.getString(R.string.share_stories_to)) {
				preferenceId = R.string.share_stories_to;
				oldValue = localPreferences.getShareTo();
				newValue = sharedPreferences.getString(key, "");
			}
			localPreferences.setValueForPreference(preferenceId, newValue);
			myPref.setSummary(sharedPreferences.getString(key, ""));			
			mLensRocketService.updatePreferences(localPreferences, new TableOperationCallback<UserPreferences>() {				
				@Override
				public void onCompleted(UserPreferences resultsPreferences, Exception ex,
						ServiceFilterResponse serviceFilterResponse) {	
					//Display error	
					if (ex != null) {
						LensRocketLogger.i(TAG, "Error in callback");
						if (NoNetworkConnectivityException.class.isInstance(ex))
							return;	
						mIsResettingValue = true;
						UserPreferences backupPrefs = mLensRocketService.getBackupPreferences();
						String error = ex.getCause().getMessage();						
						//Check for unexpected 500 errors
						if (error.contains("500")) {
							LensRocketAlert.showToast(mLensRocketService.getActivityContext(), "There was an error.  Please try again later.");
						} else {
							LensRocketAlert.showToast(mLensRocketService.getActivityContext(), ex.getCause().getMessage().replace("\"", ""));
						}
						SharedPreferences.Editor editor = sharedPreferences.edit();
						String oldValue = "";
						if (key == resources.getString(R.string.email_address))
							oldValue = backupPrefs.getEmail();
						editor.putString(key, oldValue);						
						editor.commit();
						myPref.setSummary(oldValue);						
						if (EditTextPreference.class.isInstance(myPref)) {
							EditTextPreference editPref = (EditTextPreference) myPref;
							editPref.setText(oldValue);	
						}						
					} else {
						if (getActivity() != null)
							LensRocketAlert.showToast(getActivity(), "Setting updated");
					}
				}
			});
		}
		
		private void initializeToDefaults() {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			//Set Username
			Preference usernamePref = (Preference) findPreference(getString(R.string.username));
			usernamePref.setSummary(mLensRocketService.getUsername());
			usernamePref.setSelectable(false);
			//Set Email
			EditTextPreference emailPref = (EditTextPreference) findPreference(getString(R.string.email_address));
			emailPref.setSummary(sharedPrefs.getString(getActivity().getResources().getString(R.string.email_address), ""));
			//Set receive from and share to
			Preference receiveFromPref = (Preference) findPreference(getString(R.string.receive_rockets_from));
			receiveFromPref.setSummary(sharedPrefs.getString(getActivity().getResources().getString(R.string.receive_rockets_from), ""));
			Preference shareToPref = (Preference) findPreference(getString(R.string.share_stories_to));
			shareToPref.setSummary(sharedPrefs.getString(getActivity().getResources().getString(R.string.share_stories_to), ""));			
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
