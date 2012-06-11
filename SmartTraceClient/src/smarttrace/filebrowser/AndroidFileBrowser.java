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
package smarttrace.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import smarttrace.core.Permision;
import smarttrace.core.R;
import smarttrace.core.gps.EditTextBrowser;
import smarttrace.core.gps.MainActivity;
import smarttrace.core.wifi.WifiEditTextBrowser;
import smarttrace.core.wifi.WifiGraph;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidFileBrowser extends ListActivity {
	// Enum For The Display Mode You Want
	private enum DISPLAYMODE {
		ABSOLUTE, RELATIVE;
	}

	private final DISPLAYMODE displayMode = DISPLAYMODE.ABSOLUTE;
	// All The Files In The Trajectory
	private List<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory;
	private File homeDirectory;
	private File file;
	private Toast toast;
	public static boolean flagMainOrwifi = true;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// THE SAME TOAST AS THE PREVIOUS
		toast = Toast.makeText(AndroidFileBrowser.this, "", Toast.LENGTH_LONG);
		View textView = toast.getView();
		LinearLayout lay = new LinearLayout(AndroidFileBrowser.this);
		lay.setOrientation(LinearLayout.HORIZONTAL);
		ImageView view = new ImageView(AndroidFileBrowser.this);
		view.setImageResource(R.drawable.info);
		lay.addView(view);
		lay.addView(textView);
		toast.setView(lay);
		// BROWSE TO HOME DIRECTORY THAT WAS SAVE BEFORE
		browseToHome();

	}

	// This Function Browses To The Root-Directory Of The File-System.
	private void browseToHome() {
		if (flagMainOrwifi)
			currentDirectory = new File(makePath(MainActivity.custFilePath));
		else
			currentDirectory = new File(makePath(WifiGraph.custFilePath));

		if (currentDirectory == null || isMount())
			currentDirectory = new File("/");

		homeDirectory = currentDirectory.getAbsoluteFile();
		toast.setText("You will browse from directory :\n" + homeDirectory.getAbsoluteFile());
		toast.show();
		browseTo(currentDirectory);

	}

	// check that the sdcard is mounted
	static public boolean isMount() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;

		}
		return false;
	}

	private String makePath(String path) {
		if (path.length() == 0) {
			return "/";
		}
		File check = new File(path);
		if (!check.exists()) {
			return "/";
		}
		if (check.isDirectory())
			return path;
		else {
			return check.getParent();
		}
	}

	// This Function Browses Up One Level According To The Field:
	// Current directory

	private void browseTo(final File aDirectory) {
		if (aDirectory.isDirectory()) {
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
		} else {
			toast.setText("Browse...");
			toast.show();
		}
	}

	private void fill(File[] files) {
		this.directoryEntries.clear();
		// Add the "~" == "home directory"
		// And the ".." == 'Up one level'
		this.directoryEntries.add(getString(R.string.homeDir));
		if (this.currentDirectory.getParent() != null)
			this.directoryEntries.add(getString(R.string.parentDir));
		switch (this.displayMode) {
		case ABSOLUTE:
			for (File file : files) {
				this.directoryEntries.add(file.getAbsolutePath());
			}
			break;
		case RELATIVE: // On relative Mode, we have to add the current-path to
			// the beginning
			int currentPathStringLenght = this.currentDirectory.getAbsolutePath().length();
			for (File file : files) {
				this.directoryEntries.add(file.getAbsolutePath().substring(currentPathStringLenght));
			}
			break;
		}

		MyCustomAdapter directoryList = new MyCustomAdapter(this, R.layout.file_row, this.directoryEntries);

		this.setListAdapter(directoryList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		file = new File(directoryEntries.get(position));
		if (file.isDirectory())
			if (file.canRead()) {
				// Two Cases : If The Item Is The ".." Or "."
				if (file.getName().equals(getString(R.string.parentDir))) {
					browseTo(currentDirectory.getParentFile());
				} else if (file.getName().equals(getString(R.string.homeDir))) {
					browseTo(homeDirectory);
				} else
					browseTo(file);
			} else {
				Intent i = new Intent(AndroidFileBrowser.this, Permision.class);
				startActivity(i);
			}
		else
			Stop();

		super.onListItemClick(l, v, position, id);

	}

	void Stop() {
		if (flagMainOrwifi)
			EditTextBrowser.txb.setText(file.getAbsolutePath());
		else
			WifiEditTextBrowser.txb.setText(file.getAbsolutePath());
		flagMainOrwifi = true;
		finish();
	}

	// Public Inner Class Which Help Me To Put Png Image For Each File That Is
	// Different Type
	public class MyCustomAdapter extends ArrayAdapter<String> {
		List<String> myList;

		public MyCustomAdapter(Context context, int textViewResourceId, List<String> objects) {

			super(context, textViewResourceId, objects);
			myList = objects;
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			// super.getView(position, convertView, parent);
			View row = convertView;
			// Check If Row Is Null
			if (row == null) {
				// Make New Layoutinflater
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = vi.inflate(R.layout.file_row, parent, false);
			}
			TextView label = (TextView) row.findViewById(R.id.file);
			String stringFile = myList.get(position).toString();
			// Change The Symbol Of The Home Directory
			if (stringFile.equals(getString(R.string.homeDir)))
				label.setText("~");

			else
				label.setText(stringFile);

			File file = new File(stringFile);
			ImageView icon = (ImageView) row.findViewById(R.id.icon);
			if (file.isDirectory())
				icon.setImageResource(R.drawable.directory);
			else
				icon.setImageResource(FindDrawable(stringFile));
			return row;
		}

		// Find The Right Icon For Each File Type
		private int FindDrawable(String file) {
			if (file.endsWith(".txt")) {
				return R.drawable.txt;
			} else if (file.endsWith(".pdf")) {
				return R.drawable.pdf;
			} else if (file.endsWith(".apk")) {
				return R.drawable.apk;
			} else if (file.endsWith(".png") || file.endsWith(".gif")) {
				return R.drawable.png;
			} else if (file.endsWith(".gif")) {
				return R.drawable.gif;
			} else if (file.endsWith(".jpg")) {
				return R.drawable.jpg;
			} else if (file.endsWith(".rar")) {
				return R.drawable.rar;
			} else if (file.endsWith(".zip")) {
				return R.drawable.zip;
			} else if (file.endsWith(".gz")) {
				return R.drawable.gz;
			} else if (file.endsWith(".mp3") || file.endsWith(".wav") || file.endsWith(".amp")) {
				return R.drawable.sound;
			} else if (file.endsWith(".mp4") || file.endsWith(".avi") || file.endsWith(".flv")) {
				return R.drawable.video;
			} else
				return R.drawable.txt;
		}

	}
}
