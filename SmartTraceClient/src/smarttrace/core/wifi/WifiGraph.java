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
package smarttrace.core.wifi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import smarttrace.core.PopUp;
import smarttrace.core.R;
import smarttrace.core.gps.MainActivity;
import smarttrace.messages.Messages;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WifiGraph extends Activity {
	private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
	protected static int K;
	protected volatile static SharedPreferences preferences;
	protected volatile static Handler messageHandler;
	public volatile static String[] QueryTrajectories = null;
	protected volatile static boolean privacy = false;
	private WifiManager mainWifi;
	private WifiReceiver receiverWifi;
	private List<ScanResult> wifiList;
	static ArrayList<HashMap<String, Integer>> ClientTrajectory = new ArrayList<HashMap<String, Integer>>();
	static ArrayList<HashMap<String, Integer>> ServerTrajectory = new ArrayList<HashMap<String, Integer>>();
	private Toast warn;
	private Toast info;
	private boolean connected = false;
	private int key = 0;
	private WifiConnect con;
	private String username = new String();
	private LinearLayout graphLayout;
	public static String custFilePath = Environment.getExternalStorageDirectory().getPath() +"/wifi.txt";
	public static boolean isUsernameSet = false;
	private boolean useTrace = false;
	private static String outpFileloc;
	private GraphView graphView;
	private BufferedWriter writer = null;
	// private BufferedReader reader = null;
	private File outFile = null;
	private File inFile = null;
	private boolean append;
	private double Density;
	private boolean record = false;
	private Timer timer;

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
		if (con != null) {
			con.start();
			con = null;
		}
	}

	private void initializeComponents() {
		// initialize toast for warnings
		warn = Toast.makeText(WifiGraph.this, "", Toast.LENGTH_LONG);
		View wifitextView = warn.getView();
		LinearLayout wifilay = new LinearLayout(WifiGraph.this);
		wifilay.setOrientation(LinearLayout.HORIZONTAL);
		ImageView wifiview = new ImageView(WifiGraph.this);
		wifiview.setImageResource(R.drawable.warning);
		wifilay.addView(wifiview);
		wifilay.addView(wifitextView);
		warn.setView(wifilay);
		// initialize toast for informations
		info = Toast.makeText(WifiGraph.this, "", Toast.LENGTH_LONG);
		View textView = info.getView();
		LinearLayout lay = new LinearLayout(WifiGraph.this);
		lay.setOrientation(LinearLayout.HORIZONTAL);
		ImageView view = new ImageView(WifiGraph.this);
		view.setImageResource(R.drawable.info);
		lay.addView(view);
		lay.addView(textView);
		info.setView(lay);
		graphLayout = (LinearLayout) findViewById(R.id.wifiLayout);
		messageHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				// Connection mycon;
				switch (msg.arg1) {

				case Messages.CONNECT:
					info.setText("Connecting...");
					info.show();
					break;
				case Messages.INVALID_ADDRESS:
					warn.setText("Invalid Address...");
					warn.show();
					connected = false;
					// searched = false;
					break;
				case Messages.SEARCH:
					info.setText("You are Searching...");
					info.show();
					break;
				case Messages.DEFAULT_EPSILON:
					info.setText("Using default epsilon(5)...");
					info.show();
					break;
				case Messages.LCSS:
					info.setText("Calculating LCSS...!");
					info.show();
					break;
				case Messages.UPBOUND:
					info.setText("Calculating UB...!");
					info.show();
					break;
				case Messages.ENDSRCH:
					info.setText("Searching done succesfully!");
					info.show();
					wifiSmrt(msg.arg2);
					break;
				case Messages.QUIT:
				case Messages.ENDCONN:
					info.setText("Connecting done!");
					info.show();
					connected = false;
					break;
				case Messages.SERVER_ERR:
					warn.setText(Messages.errors.get(msg.arg2));
					warn.show();
					break;
				case Messages.STATECAL:
					warn.setText("You are in calculate state\nPlease try again!");
					warn.show();
					break;
				case Messages.STATERET:
					warn.setText("You are in retrieval state\nPlease try again!");
					warn.show();
					break;
				case Messages.STATESER:
					warn.setText("You are already searching!\nPlease try again!");
					warn.show();
					break;
				default:
					warn.setText("Connection closed.");
					warn.show();

					connected = false;
					// searched = false;
					break;
				}

			}
		};

		final PopUp popup = (PopUp) findViewById(R.id.popupWifiwindow);
		final PopUp popuptxt = (PopUp) findViewById(R.id.popupWifiText);
		popup.setVisibility(View.GONE);
		popup.setTitle("Do you want to switch to GPS mode?");
		final Button btn = (Button) findViewById(R.id.ppWifibutton);
		final Button btnCancel = (Button) findViewById(R.id.btnWCancel);
		final Button btnOk = (Button) findViewById(R.id.btnWOk);
		final Button btnClose = (Button) findViewById(R.id.btnWifiClose);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// initialize the input file
		custFilePath = preferences.getString(WifiEditTextBrowser.wPATH, SDCARD);

		btnClose.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				btnClose.setBackgroundResource(R.drawable.disclose);
				AlertDialog.Builder builder = new AlertDialog.Builder(WifiGraph.this);
				builder.setMessage("Are you sure you want to exit?").setCancelable(false).setPositiveButton("Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						appClose();

					}
				}).setNegativeButton("No", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						btnClose.setBackgroundResource(R.drawable.close);
						return;

					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}

		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				popup.setVisibility(View.GONE);
				btn.setBackgroundResource(R.drawable.leftarrow);
				popuptxt.setVisibility(View.VISIBLE);
				btnClose.setVisibility(View.VISIBLE);
			}
		});

		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(WifiGraph.this, MainActivity.class);
				startActivity(i);
				appClose();
			}
		});

		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (key == 0) {
					key = 1;
					popup.setVisibility(View.VISIBLE);
					btn.setBackgroundResource(R.drawable.selectorrigth);
					popuptxt.setVisibility(View.GONE);
					btnClose.setVisibility(View.GONE);
				} else if (key == 1) {
					key = 0;
					popup.setVisibility(View.GONE);
					btn.setBackgroundResource(R.drawable.selectorleft);
					popuptxt.setVisibility(View.VISIBLE);
					btnClose.setVisibility(View.VISIBLE);
				}
			}
		});

		// show that scanning started
		info.setText("Scanning...");
		info.show();

		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mainWifi.startScan();
		timer = new Timer();
		// change the mode of the comparison
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mainWifi.startScan();

			}
		}, 2000, 2000);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi);
		initializeComponents();
		// set The UserName
		if (!setUsername()) {

			final AlertDialog.Builder alert = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setMessage("Please write your Screen Name");
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					username = input.getText().toString().trim();
					Editor editor = preferences.edit();
					editor.putString("wusrName", username);
					editor.commit();
					warn.setText(username);
					warn.show();
				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});
			alert.show();

		}
	}

	protected void appClose() {
		// TODO Auto-generated method stub
		finish();
		System.exit(0);
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, R.id.record, 0, "Record").setIcon(R.drawable.recorded);
		menu.add(0, R.id.Cons, 1, "Connect").setIcon(R.drawable.conser);
		menu.add(0, R.id.SmrtTrc, 2, "Smart Trace").setIcon(R.drawable.smart);
		menu.add(0, R.id.Share, 3, "Share").setIcon(R.drawable.share);
		menu.add(0, R.id.preferences, 4, "Settings").setIcon(R.drawable.prefer);
		menu.add(0, R.id.more, 5, "More ...").setIcon(R.drawable.more);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		MenuItem item = menu.getItem(1);
		MenuItem itemrecord = menu.getItem(0);
		if (!connected) {
			item.setIcon(R.drawable.conser);
			item.setTitle("Connect");
		} else {
			item.setIcon(R.drawable.disser);
			item.setTitle("Disconnect");
		}
		if (!record) {
			itemrecord.setIcon(R.drawable.recorded);
			itemrecord.setTitle("Record");
		} else {
			itemrecord.setIcon(R.drawable.record);
			itemrecord.setTitle("Recording...");
		}
		return super.onMenuOpened(featureId, menu);
	}

	// This method is called once the menu is selected
	@SuppressLint("SdCardPath")
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		Message msg = null;
		switch (item.getItemId()) {
		// We have only 5 menu option
		// Smart trace request to Server with new thread
		case R.id.SmrtTrc:

			if (!connected) {
				warn.setText("You should connect first!");
				warn.show();
				break;
			}
			msg = new Message();
			msg.arg1 = Messages.SEARCH;
			WifiConnect.messageHandler.sendMessage(msg);

			break;
		case R.id.Cons:
			// Connect to Server with new thread
			final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
			final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (!connected) {
				if (setUsername()) {
					if (wifi.isAvailable()) {
						// set the trace flag and the trace file' s name
						custFilePath = preferences.getString(WifiEditTextBrowser.wPATH, SDCARD);
						// useTrace = preferences.getBoolean("OffWifi", false);
						if (record) {
							warn.setText("Stop recording wifi points");
							warn.show();
							record = false;
						}
						// create a new thread for the connection
						con = new WifiConnect(getApplicationContext(), useTrace, username, this);
						WifiMore.terminal = "";
						connected = true;
					} else {
						gpsWifiAlert("Wifi");
						break;
					}
				} else {
					warn.setText("The username is not set yet!!!");
					warn.show();
					break;
				}

			} else {
				msg = new Message();
				msg.arg1 = Messages.QUIT;
				WifiConnect.messageHandler.sendMessage(msg);
				connected = false;
			}

			break;

		case R.id.record:
			if (useTrace) {
				warn.setText("You are using a trace file.\nTo collect point go in online mode");
				warn.show();
				break;
			}
			// Launch ModeOfMap activity
			if (!record) {
				WifiConnect.ClientTRAJ = "";
				outpFileloc = preferences.getString("wOutpFile", "/sdcard/wifi.txt");
				append = preferences.getBoolean("wAppend", false);
				try {
					outFile = new File(outpFileloc);
					writer = new BufferedWriter(new FileWriter(outFile, append));
				} catch (Exception e) {
					warn.setText(

					"Error when opening output file:\n" + e.getMessage());
					warn.show();
					break;
				}
				record = true;
			} else {
				record = false;
				if (writer != null)
					try {
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			}
			break;
		case R.id.more:
			// Launch more activity
			i = new Intent(WifiGraph.this, smarttrace.core.wifi.WifiMore.class);
			startActivity(i);

			break;
		case R.id.Share:
			// Launch Preferences activity
			i = new Intent(WifiGraph.this, smarttrace.settings.WifiPreferences.class);
			startActivity(i);
			info.setText("Here are the application's preferences.");
			info.show();
			// con = null;
			break;
		case R.id.preferences:
			// Launch Preferences activity
			i = new Intent(WifiGraph.this, smarttrace.settings.WifiOnOffPreferences.class);
			startActivity(i);
			info.setText("Here are the application's settings.");
			info.show();
			break;
		}
		return true;
	}

	class WifiReceiver extends BroadcastReceiver {

		public void onReceive(Context c, Intent intent) {
			wifiList = mainWifi.getScanResults();
			HashMap<String, Integer> WifiAP = new HashMap<String, Integer>();
			WifiAP.clear();

			for (int i = 0; i < wifiList.size(); i++) {
				WifiAP.put(wifiList.get(i).BSSID/* +"\n"+wifiList.get(i).SSID */, wifiList.get(i).level);
				if (record) {
					try {
						WifiConnect.ClientTRAJ += wifiList.get(i).BSSID + "," + wifiList.get(i).level + "&";
						writer.append(wifiList.get(i).BSSID + "," + wifiList.get(i).level + "\n");

					} catch (IOException e) {
						// TODO Auto-generated catch block
						warn.setText("Exception in onReceive(): " + e.getMessage());
						warn.show();
					}
				}
				// else
				// break;
			}
			if (record) {
				try {

					// add the new line
					WifiConnect.ClientTRAJ += "#";
					writer.append("#\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					warn.setText("Exception in onReceive(): " + e.getMessage());
					warn.show();
				}
				ClientTrajectory.add(WifiAP);
			}
			// toast.setText("Size=" + ClientTrajectory.size());
			// toast.show();
			int i = 0;
			float[] values = new float[WifiAP.size()];
			String[] verlabels = new String[] { "-100db", "-90db", "-80db", "-70db", "-60db", "-50db", "-40db", "-30db", "-20db", "-10db", "0db" };
			String[] horlabels = new String[WifiAP.size()];
			for (String string : WifiAP.keySet()) {
				horlabels[i] = string;
				values[i] = (float) WifiAP.get(string);
				i++;
			}

			graphView = new GraphView(graphLayout.getContext(), values, "wifi", horlabels, verlabels, GraphView.BAR);
			graphLayout.removeAllViews();
			graphLayout.addView(graphView);
			graphView.invalidate();
		}
	}

	protected void onPause() {
		// unregisterReceiver(receiverWifi);
		getPref();
		super.onPause();
	}

	protected void onResume() {
		// registerReceiver(receiverWifi, new
		// IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		getPref();
		super.onResume();
	}

	void wifiSmrt(double lcss) {

		String folowedUsers = "";

		for (int i = 1; i < QueryTrajectories.length; i++) {
			folowedUsers += QueryTrajectories[i];
		}

		String msg;
		if (folowedUsers.length() > 0)
			msg = "folowed by " + folowedUsers;
		else
			msg = "";
		AlertDialog.Builder builder = new AlertDialog.Builder(WifiGraph.this);

		DecimalFormat df = new DecimalFormat("##.##");
		builder.setMessage("LCSS = " + df.format(lcss / 100) + "% with " + QueryTrajectories[0] + msg + ".\n").setCancelable(false).setPositiveButton("OK",
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						return;

					}
				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void readFromFile() {

		String line = "";

		inFile = new File(custFilePath);
		BufferedReader reader = null;
		if (!inFile.exists()) {
			warn.setText("Provide path/filename for input file that exists.\n(menu preferences)\n");
			warn.show();
			return;
		}
		try {
			reader = new BufferedReader(new FileReader(inFile));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			warn.setText("Error:" + e1.getMessage());
			warn.show();
		}

		try {
			WifiConnect.ClientTRAJ = "";
			HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
			while ((line = reader.readLine()) != null) {
				if (line.equals("#")) {
					WifiConnect.ClientTRAJ += "#";
					ClientTrajectory.add(tempMap);
					tempMap = new HashMap<String, Integer>();
				} else {
					tempMap.put(line.split(",")[0], Integer.parseInt(line.split(",")[1]));
					WifiConnect.ClientTRAJ += line + "&";
				}
			}
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			warn.setText("Error:" + e.getMessage());
			warn.show();
		}
	}

	boolean setUsername() {
		// Check for any change in preferences
		username = WifiGraph.preferences.getString("wusrName", "");
		if (username.length() <= 0 || username.contains(" \t\n\b\"\')({}[];")) {
			isUsernameSet = false;
			return false;
		} else {
			isUsernameSet = true;
			return true;
		}

	}

	private void gpsWifiAlert(final String sett) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your " + sett + " seems to be disabled, do you want to enable it?").setCancelable(false).setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (sett.equals("GPS"))
							startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
						else
							startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public void checkTrace() {
		WifiGraph.ClientTrajectory.clear();
		if (!useTrace) {
			if (WifiGraph.ClientTrajectory.size() != 0) {
				appClose();
			} else {
				warn.setText("Press Wifi record button to collect coordinates");
				warn.show();
				// appClose();
			}
			return;
		}
		try {
			inFile = new File(WifiGraph.custFilePath);
			if (!inFile.exists()) {
				warn.setText("Provide path/filename for input file that exists.\n(menu preferences)\n");
				warn.show();
				// appClose();
				return;
			}
			if (WifiGraph.custFilePath.equals("n/a") || WifiGraph.custFilePath.equals("")) {
				info.setText("Using default trajectory file (Wifi) data).");
				info.show();
				inFile = new File(WifiGraph.outpFileloc);
			} else {
				info.setText("Using trajectory file: " + WifiGraph.custFilePath + ".");
				info.show();
				// appClose();
			}

			// reader = new BufferedReader(new FileReader(inFile));

		} catch (Exception e) {
			warn.setText("Error when opening file:" + e.getMessage());
			warn.show();
			// appClose();
		}
		// save the file for check if it is already processed
		readFromFile();
	}

	void getPref() {
		useTrace = preferences.getBoolean("OffWifi", false);
		append = preferences.getBoolean("wAppend", false);
		outpFileloc = preferences.getString("wOutpFile", SDCARD+"/wifi.txt");
		custFilePath = preferences.getString(WifiEditTextBrowser.wPATH, SDCARD+"/wifi.txt");
		privacy = preferences.getBoolean("wPrivacyTraj", false);
		Density = preferences.getInt("wDensity", 1) == 0 ? 0.5 : preferences.getInt("wDensity", 1);
		timer.cancel();
		timer.purge();
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mainWifi.startScan();
			}
		}, (int) (Density * 1000), (int) (Density * 1000));

	}

}