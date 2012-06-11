/*
 * (C) Copyright University of Cyprus. 2010-2011.
 *
 * Android API
 *
 * @version         : 1.0
 * @author : Costantinos Costa(costa.costantinos@gmail.com)
 * Project Supervision : Demetris Zeinalipour (dzeina@cs.ucy.ac.cy)
 * Computer Science Department , University of Cyprus
 *
 *
 */
/*
 * This is a spatio-temporal similarity search framework, coined SmartTrace.
 *Our framework can be utilized to promptly answer queries
 *of the form: “Report the objects (i.e., trajectories) that follow
 *a similar spatio-temporal motion to Q, where Q is some query
 *trajectory.” SmartTrace, relies on an in-situ data storage model,
 *where spatio-temporal data remains on the smartphone that
 *generated the given data, as well a state-of-the-art top-K query
 *processing algorithm, which exploits distributed trajectory similarity
 *measures in order to identify the correct answer promptly.
 *
 *Copyright (C) 2010 - 2011 Costantinos Costa
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation, either version 3 of the License, or
 *at your option) any later version.
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *GNU General Public License for more details.
 *Υou should have received a copy of the GNU General Public License
 *along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package smarttrace.settings;

import smarttrace.core.R;
import smarttrace.core.wifi.WifiEditTextBrowser;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class WifiOnOffPreferences extends PreferenceActivity {
	/** Called when the activity is first created. */
	String ListPreference;
	String editTextPreference;
	String ringtonePreference;
	String secondEditTextPreference;
	String customPref;
	SharedPreferences prefs;
	CheckBoxPreference onPref;
	CheckBoxPreference offPref;
	EditTextPreference etpUser;
	Preference OffCategory;
	Preference OnCategory;
	Preference InputFile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wifionoffprefers);
		// android:key="OffCategory"
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		OffCategory = (Preference) findPreference("wOffCategory");
		OnCategory = (Preference) findPreference("wOnCategory");
		InputFile = (Preference) findPreference("wCustFile");
		onPref = (CheckBoxPreference) findPreference("OnWifi");

		// when a click event is creating
		onPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				checkOn();
				return true;
			}

		});

		offPref = (CheckBoxPreference) findPreference("OffWifi");
		offPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				checkOff();
				return true;
			}

		});
		InputFile.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				// Launch OnOffPreferences activity
				Intent i = new Intent(WifiOnOffPreferences.this, WifiEditTextBrowser.class);
				startActivity(i);
				return false;
			}
		});
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		checkOff();
		checkOn();
	}

	public void checkOff() {
		if (offPref.isChecked()) {
			offPref.setChecked(true);
			OffCategory.setEnabled(true);
			onPref.setChecked(false);
			OnCategory.setEnabled(false);
			onPref.setEnabled(true);
		} else {
			onPref.setChecked(true);
			OnCategory.setEnabled(true);
			offPref.setChecked(false);
			OffCategory.setEnabled(false);
			offPref.setEnabled(true);
		}
	}

	public void checkOn() {
		if (onPref.isChecked()) {
			onPref.setChecked(true);
			OnCategory.setEnabled(true);
			offPref.setChecked(false);
			OffCategory.setEnabled(false);
			offPref.setEnabled(true);
		} else {
			offPref.setChecked(true);
			OffCategory.setEnabled(true);
			onPref.setChecked(false);
			OnCategory.setEnabled(false);
			onPref.setEnabled(true);
		}
	}
}
