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
import smarttrace.core.gps.MainActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WifiPreferences extends PreferenceActivity {
	private Toast toast;
	private EditTextPreference etpUser;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wprefers);
		etpUser= (EditTextPreference) findPreference("wusrName");
		toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		View textView = toast.getView();
		LinearLayout lay = new LinearLayout(this);
		lay.setOrientation(LinearLayout.HORIZONTAL);
		ImageView view = new ImageView(this);
		view.setImageResource(R.drawable.warning);
		lay.addView(view);
		lay.addView(textView);
		toast.setView(lay);
		etpUser.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				//
				// Check for any change in preferences
				
				String username = newValue.toString();
						if (username.length() <= 0 || username.contains(" \t\n\b\"\')({}[];")) {
							toast.setText("Invalid Username " + username);
							toast.show();
							SharedPreferences.Editor ed = preference.getEditor();
							ed.putString("wusrName", "");
							ed.commit();
							toast.setText("Invalid Username");
							toast.show();
						} else {
							MainActivity.isUsernameSet = true;
							toast.setText("Thank you " + username);
							toast.show();
						}
				return true;
			}
		});
	}

}
