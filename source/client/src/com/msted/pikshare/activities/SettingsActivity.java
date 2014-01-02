package com.msted.pikshare.activities;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.msted.pikshare.PikShareApplication;
import com.msted.pikshare.PikShareService;
import com.msted.pikshare.R;
import com.msted.pikshare.R.menu;
import com.msted.pikshare.R.string;
import com.msted.pikshare.R.xml;
import com.msted.pikshare.datamodels.UserPreferences;
import com.msted.pikshare.util.NoNetworkConnectivityException;
import com.msted.pikshare.util.PikShareAlert;
import com.msted.pikshare.util.PikShareLogger;

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
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, mSettingsFragment)
		.commit();
	}	
	
	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		private final String TAG = "SettingsActivity";
		private PikShareService mPikShareService;
		private boolean mIsResettingValue;
		
		@Override
		public void onResume() {
			super.onResume();
			PikShareLogger.d(TAG, "settings fragment on resume");
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
			addPreferencesFromResource(R.xml.preferences);
			PikShareApplication app = (PikShareApplication) getActivity().getApplication();
			mPikShareService = app.getPikShareService();			
			initializeToDefaults();
			Preference logoutPref = (Preference) findPreference(getString(R.string.log_out));
			logoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					mPikShareService.logout(true);
					return true;
				}
			});			
		}

		@Override
		public void onSharedPreferenceChanged(
				final SharedPreferences sharedPreferences, final String key) {
			PikShareLogger.d(TAG, "Preference changed for key: " + key);
			if (mIsResettingValue) {
				mIsResettingValue = false;
				return;
			}			
			final Preference myPref = findPreference(key);						
			final Resources resources = getActivity().getResources();
			final UserPreferences localPreferences = mPikShareService.getLocalPreferences();
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
					PikShareAlert.showToast(getActivity(), "That email address is invalid!");
					mIsResettingValue = true;
					editor.commit();
					EditTextPreference editPref = (EditTextPreference) myPref;
					editPref.setText(oldValue);
					return;
				}
			} else if (key == resources.getString(R.string.receive_piks_from)) {
				preferenceId = R.string.receive_piks_from;
				oldValue = localPreferences.getReceiveFrom();
				newValue = sharedPreferences.getString(key, "");
			} else if (key == resources.getString(R.string.share_stories_to)) {
				preferenceId = R.string.share_stories_to;
				oldValue = localPreferences.getShareTo();
				newValue = sharedPreferences.getString(key, "");
			}
			localPreferences.setValueForPreference(preferenceId, newValue);
			myPref.setSummary(sharedPreferences.getString(key, ""));			
			mPikShareService.updatePreferences(localPreferences, new TableOperationCallback<UserPreferences>() {				
				@Override
				public void onCompleted(UserPreferences resultsPreferences, Exception ex,
						ServiceFilterResponse serviceFilterResponse) {	
					//Display error	
					if (ex != null) {
						PikShareLogger.i(TAG, "Errror in callback");
						if (NoNetworkConnectivityException.class.isInstance(ex))
							return;	
						mIsResettingValue = true;
						UserPreferences backupPrefs = mPikShareService.getBackupPreferences();
						String error = ex.getCause().getMessage();						
						//Check for unexpected 500 errors
						if (error.contains("500")) {
							PikShareAlert.showToast(mPikShareService.getActivityContext(), "There was an error.  Please try again later.");
						} else {
							PikShareAlert.showToast(mPikShareService.getActivityContext(), ex.getCause().getMessage().replace("\"", ""));
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
							PikShareAlert.showToast(getActivity(), "Setting updated");
					}
				}
			});
		}
		
		private void initializeToDefaults() {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			//Set Username
			Preference usernamePref = (Preference) findPreference(getString(R.string.username));
			usernamePref.setSummary(mPikShareService.getUsername());
			usernamePref.setSelectable(false);
			//Set Email
			EditTextPreference emailPref = (EditTextPreference) findPreference(getString(R.string.email_address));
			emailPref.setSummary(sharedPrefs.getString(getActivity().getResources().getString(R.string.email_address), ""));
			//Set receive from and share to
			Preference receiveFromPref = (Preference) findPreference(getString(R.string.receive_piks_from));
			receiveFromPref.setSummary(sharedPrefs.getString(getActivity().getResources().getString(R.string.receive_piks_from), ""));
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
