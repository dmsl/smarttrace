/*
 * (C) Copyright University of Cyprus. 2010-2011.
 *
 * Android Server API
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
package smarttrace.core.gps;

import smarttrace.core.R;
import smarttrace.filebrowser.AndroidFileBrowser;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

//Create a custom dialog to keep the browsing path
public class EditTextBrowser extends Activity {

	final public static String PATH = "Path";
	final public static String SDCARD =  Environment.getExternalStorageDirectory().getPath();
	Button btnOk;
	Button btnCancel;
	Button btnBrowse;
	private SharedPreferences mPrefs;
	public static EditText txb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_layout);
		btnOk = (Button) findViewById(R.id.btnOk);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnBrowse = (Button) findViewById(R.id.btnBrowse);
		txb = (EditText) findViewById(R.id.txbFile);
		txb.setScrollContainer(true);
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Stop();
			}
		});
		btnBrowse.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(EditTextBrowser.this,
						smarttrace.filebrowser.AndroidFileBrowser.class);
				AndroidFileBrowser.flagMainOrwifi=true;
				startActivity(i);
			}
		});

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		MainActivity.custFilePath = mPrefs.getString(PATH,SDCARD);

		txb.setText(MainActivity.custFilePath);
		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity.custFilePath = txb.getText().toString();
				Stop();
			}
		});
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		MainActivity.custFilePath = savedInstanceState.getString(PATH);
		if (!TextUtils.isEmpty(MainActivity.custFilePath)) {
			// Update lbResult
			EditTextBrowser.txb.setText(savedInstanceState.getString(PATH));
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		/**************************************************
		 * Here we do _temporary_ save all critical data, like our
		 * selectedProduct.
		 **************************************************/
		outState.putString(PATH, MainActivity.custFilePath);
		super.onSaveInstanceState(outState);
	}

	void Stop() {
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putString(PATH, MainActivity.custFilePath);
		ed.commit();
		finish();
	}

}
