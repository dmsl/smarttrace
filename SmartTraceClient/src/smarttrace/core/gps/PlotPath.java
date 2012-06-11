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
package smarttrace.core.gps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import smarttrace.core.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

//Public class to plot the path
public class PlotPath extends Activity {
	private boolean AlreadyRead = false;
	myPoint[] trajectory = null;
	private File inFile = null;
	BufferedWriter writer = null;
	BufferedReader reader = null;

	Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		toast = Toast.makeText(PlotPath.this, "", Toast.LENGTH_LONG);
		View textView = toast.getView();
		LinearLayout lay = new LinearLayout(PlotPath.this);
		lay.setOrientation(LinearLayout.HORIZONTAL);
		ImageView view = new ImageView(PlotPath.this);
		view.setImageResource(R.drawable.warning);
		lay.addView(view);
		lay.addView(textView);
		toast.setView(lay);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// check for any change in preferences
		if (MainActivity.ClientTrajectory == null)
			MainActivity.ClientTrajectory = new ArrayList<myPoint>();
		if (!MainActivity.useTrace) {
			if (MainActivity.ClientTrajectory.size() != 0) {
				appClose();
			} else {
				toast.setText("Press GPS button to collect coordinates");
				toast.show();
				appClose();
			}
			return;
		}
		try {
			inFile = new File(MainActivity.custFilePath);
			if (!inFile.exists()) {
				toast.setText("Provide path/filename for input file that exists.\n(menu preferences)\n");
				toast.show();
				appClose();
				return;
			}
			if (MainActivity.custFilePath.equals("n/a") || MainActivity.custFilePath.equals("")) {
				toast.setText("Using default trajectory file (GPS data).");
				toast.show();
				inFile = new File(MainActivity.outpFileloc);
			} else {
				toast.setText("Using trajectory file: " + MainActivity.custFilePath + ".");
				toast.show();
				appClose();
			}

			reader = new BufferedReader(new FileReader(inFile));

		} catch (Exception e) {
			toast.setText("Error when opening file:" + e.getMessage());
			toast.show();
			appClose();
		}
		// save the file for check if it is already processed
		readFromFile();
		appClose();
	}

	void readFromFile() {
		if (AlreadyRead && MainActivity.previous.equals(MainActivity.custFilePath))
			return;
		MainActivity.ClientTrajectory.clear();
		MainActivity.ploted = true;
		// it's the same file
		MainActivity.previous = MainActivity.custFilePath;
		// get the Latitude from file and store it to an array
		try {
			String line;
			String[] temp;
			myPoint temppoint;
			while ((line = reader.readLine()) != null) {
				temp = line.split("\t");
				temppoint = new myPoint(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
				MainActivity.ClientTrajectory.add(temppoint);
			}

		} catch (Exception e) {
			toast.setText("Exception while reading from file:\n" + e.getMessage());
			toast.show();
		}
		AlreadyRead = true;

		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			toast.setText("Exception while closing file:\n" + e.getMessage());
			toast.show();
		}

	}

	void appClose() {
		MainActivity.dialog.dismiss();
		finish();
	}
}
