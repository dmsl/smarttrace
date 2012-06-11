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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import smarttrace.connectivity.Connect;
import smarttrace.core.PopUp;
import smarttrace.core.R;
import smarttrace.messages.Messages;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity {

	private static final String SDCARD = "/sdcard/";
	// Server and Client Trajectory
	public volatile static ArrayList<myPoint> ClientTrajectory;
	public volatile static ArrayList<myPoint> ServerTrajectory;
	// the dialog that is use for drawing the path
	public volatile static ProgressDialog dialog;
	// flag that is used to now if the data comes from
	// a trace or the GPS
	public volatile static boolean useTrace;
	public volatile static boolean GpsStarted;
	public volatile static boolean privacy = false;
	// String that keep the previous file
	public volatile static boolean AlreadyRead = false;
	public volatile static String previous = "";

	public volatile static String custFilePath = "";
	public volatile static String outpFileloc = new String();
	// flags for the map
	public volatile static boolean pin = false;
	public volatile static boolean sat = false;
	public volatile static boolean traffic = false;
	public volatile static boolean strview = false;
	public volatile static int kindofdraw = R.drawable.flag;
	public volatile static String[][] QueryTrajectories = null;

	public volatile static SharedPreferences preferences;
	public volatile static LocationManager locationManager = null;
	public volatile static LocationListener locationListener = null;
	public volatile static boolean ploted;

	public volatile static Handler messageHandler;
	// start and end point for double clicking the home button
	public static ArrayList<GeoPoint> startpoints;
	public static ArrayList<GeoPoint> endpoints;

	public static boolean srchdn;
	public static int next;
	public static int startend;

	protected LinearLayout linearLayout;

	private int Density = 2;
	protected double platidute;
	protected double plongitude;
	// clicks from all the buttons
	public static int clicks;
	public static int clckcol;
	public static int clckpin;
	// variables that is using to flag something
	private boolean append;
	private boolean connected;

	// private variables that only are used here
	private MapView mapView;
	private BufferedWriter writer = null;
	private BufferedReader reader = null;
	private File outFile = null;
	private File inFile = null;
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private MapController myMC = null;
	private GeoPoint geoPoint = null;
	private MyItemizedOverlay itemizedOverlay;
	private Toast warn;
	private Toast info;
	private Connect con;
	/**
	 * All the buttons are private
	 */
	private ImageButton btnHome;
	private ImageButton btnClose;
	private ImageButton btnNext;
	// these button need to have a restore function
	private volatile static ImageButton btnCol;

	protected static void restoreCol() {
		btnCol.setBackgroundResource(R.drawable.colred);
	}

	// these button need to have a restore function
	private volatile static ImageButton btnPin;

	protected static void restorePin() {
		btnPin.setBackgroundResource(R.drawable.none);
	}

	// these buttons need to have a restore function
	private volatile static ImageButton btnWid;

	protected static void restoreWid() {
		btnWid.setBackgroundResource(R.drawable.width1);

	}

	protected String username = new String();
	public static boolean isUsernameSet = false;
	public static int K;

	int key = 0;

	private boolean record = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// start the main activity
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		final PopUp popup = (PopUp) findViewById(R.id.popupGpswindow);
		popup.setVisibility(View.GONE);
		popup.setTitle("Do you want to switch to wifi mode?");
		final Button btn = (Button) findViewById(R.id.show_popup_button);
		final Button btnCancel = (Button) findViewById(R.id.btnCancel);
		final Button btnOk = (Button) findViewById(R.id.btnOk);
		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				popup.setVisibility(View.GONE);
				btn.setBackgroundResource(R.drawable.leftarrow);
			}
		});

		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this, smarttrace.core.wifi.WifiGraph.class);
				startActivity(i);
				if (con != null) {
					con.close();
				}
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
				} else if (key == 1) {
					key = 0;

					popup.setVisibility(View.GONE);
					btn.setBackgroundResource(R.drawable.selectorleft);
				}
			}
		});
		// initialized Toast messages
		initMessage();
		// initialize variables
		initVariables();
		// initialize controls
		initControls(this);

		// initialized the message handler for the messages from the thread
		messageHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				// Connection mycon;
				switch (msg.arg1) {
				case Messages.FIRST_PLOT:
					warn.setText("Please plot your trajectory first.");
					warn.show();
					connected = false;
					// searched = false;
					break;
				case Messages.CONNECT:
					info.setText("Connecting.");
					info.show();
					break;
				case Messages.INVALID_ADDRESS:
					warn.setText("Invalid Address.");
					warn.show();
					connected = false;
					// searched = false;
					break;
				case Messages.SEARCH:
					warn.setText("You are Searching.");
					warn.show();
					break;
				case Messages.DEFAULT_EPSILON:
					info.setText("Using default epsilon(0.001).");
					info.show();
					break;
				case Messages.LCSS:
					info.setText("Calculating LCSS.!");
					info.show();
					// PlotSmrt(msg.arg2);
					break;
				case Messages.UPBOUND:
					info.setText("Calculating UB.!");
					info.show();
					break;
				case Messages.ENDSRCH:
					info.setText("Searching done!");
					info.show();
					if (QueryTrajectories == null)
						PlotSmrtPrivate(msg.arg2);
					else
						PlotSmrt(msg.arg2);
					// connected = false;
					// searched = false;
					break;
				case Messages.ENDCONN:
					info.setText("Connection done!");
					info.show();
					connected = false;
					break;
				case Messages.STATECAL:
					warn.setText("You are in calculate state.\nPlease try again!");
					warn.show();
					break;
				case Messages.STATERET:
					warn.setText("You are in retrieval state.\nPlease try again!");
					warn.show();
					break;
				case Messages.STATESER:
					warn.setText("You are already searching.\nPlease try again!");
					warn.show();
					break;
				case Messages.SERVER_ERR:
					warn.setText(Messages.errors.get(msg.arg2));
					warn.show();
					break;
				default:
					warn.setText("The Connection was closed.");
					warn.show();
					connected = false;
					// searched = false;
					break;
				}

			}
		};
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
					editor.putString("usrName", username);
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

	@SuppressLint("UseSparseArrays")
	private void initMessage() {
		// TODO Auto-generated method stub
		// initialized errors list
		Messages.errors = new HashMap<Integer, String>();
		fillHashMapError(Messages.errors);

		// initialize toast for warnings
		warn = Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG);
		View wifitextView = warn.getView();
		LinearLayout wifilay = new LinearLayout(MainActivity.this);
		wifilay.setOrientation(LinearLayout.HORIZONTAL);
		ImageView wifiview = new ImageView(MainActivity.this);
		wifiview.setImageResource(R.drawable.warning);
		wifilay.addView(wifiview);
		wifilay.addView(wifitextView);
		warn.setView(wifilay);
		// initialize toast for informations
		info = Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG);
		View textView = info.getView();
		LinearLayout lay = new LinearLayout(MainActivity.this);
		lay.setOrientation(LinearLayout.HORIZONTAL);
		ImageView view = new ImageView(MainActivity.this);
		view.setImageResource(R.drawable.info);
		lay.addView(view);
		lay.addView(textView);
		info.setView(lay);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		// The only time that a sample is draw
		if (MainActivity.custFilePath.equals(getString(R.string.sdcard))) {
			InputStream inStream = getResources().openRawResource(R.raw.trajectory_sample);
			readFromSampleFile(inStream);
			myMC = mapView.getController();
			myMC.setZoom(15);
			info.setText("This is a sample trajectory");
			info.show();
		}
		// We should redraw the map due the process dialog
		startMap();
		getPref();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// We should redraw the map due the process dialog
		startMap();
		getPref();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
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
			Connect.messageHandler.sendMessage(msg);

			break;
		case R.id.Cons:
			// Connect to Server with new thread
			final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
			final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (!connected) {
				if (setUsername()) {
					if (wifi.isAvailable())
						con = new Connect(getApplicationContext(), false, username);
					else {
						gpsWifiAlert("Wifi");
						break;
					}
				} else {
					warn.setText("The username is not set yet!!!");
					warn.show();
					break;
				}
				More.terminal = "";
				connected = true;

			} else {
				msg = new Message();
				msg.arg1 = Messages.QUIT;
				Connect.messageHandler.sendMessage(msg);
				connected = false;
			}

			break;
		case R.id.more:
			// Launch more activity
			i = new Intent(MainActivity.this, smarttrace.core.gps.More.class);
			startActivity(i);
			// con = null;
			break;
		case R.id.Share:
			// Launch Preferences activity
			i = new Intent(MainActivity.this, smarttrace.settings.Preferences.class);
			startActivity(i);
			info.setText("Here are the application's preferences.");
			info.show();
			// con = null;
			break;

		case R.id.preferences:
			// Launch Preferences activity
			i = new Intent(MainActivity.this, smarttrace.settings.OnOffPreferences.class);
			startActivity(i);
			info.setText("Here are the application's settings.");
			info.show();
			// con = null;
			break;
		case R.id.record:

			if (useTrace) {
				warn.setText("You are using a trace file.\nTo collect point go in online mode");
				warn.show();
				break;
			}
			// Launch ModeOfMap activity
			if (!record) {
				if (!GpsStarted) {
					Density = (int) preferences.getInt("Density", 1);
					Density = (int) Math.pow(10, Density);
					startGps();
				}
				record = true;
			} else {
				if (GpsStarted)
					stopGps();
				record = false;
			}
			break;

		}
		return true;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
		if (con != null) {
			con.start();
			con = null;
		}
	}

	void initVariables() {

		// initialize important variables
		connected = false;
		// searched = false;
		con = null;
		ClientTrajectory = new ArrayList<myPoint>();
		QueryTrajectories = new String[10][];
		startpoints = new ArrayList<GeoPoint>();
		endpoints = new ArrayList<GeoPoint>();
		startpoints.clear();
		endpoints.clear();
		ClientTrajectory.clear();
		clicks = 0;
		clckcol = 0;
		clckpin = 0;
		next = 0;
		ploted = false;
		srchdn = false;
		previous = new String("");
		DrawPathOverlay.widthOfPath = 2;

	}

	// initialize controls
	private void initControls(MainActivity googleMaps) {
		btnPin = (ImageButton) findViewById(R.id.btnPin);
		btnPin.setBackgroundResource(R.drawable.none);
		btnCol = (ImageButton) findViewById(R.id.btnCol);
		btnCol.setBackgroundResource(R.drawable.colred);
		btnClose = (ImageButton) findViewById(R.id.btnclose);
		btnClose.setBackgroundResource(R.drawable.close);
		btnClose.setAdjustViewBounds(true);
		btnWid = (ImageButton) findViewById(R.id.btnWid);
		btnWid.setBackgroundResource(R.drawable.width1);
		btnHome = (ImageButton) findViewById(R.id.btnHome);
		btnHome.setBackgroundResource(R.drawable.selector_home);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnNext.setBackgroundResource(R.drawable.selector_next);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// initialize the input file
		custFilePath = preferences.getString(EditTextBrowser.PATH, SDCARD);
		// initialized the map view
		mapView = (MapView) findViewById(R.id.mapview);

		myMC = mapView.getController();
		myMC.setZoom(8);
		// initialize the density and gps variable
		platidute = 0.0;
		plongitude = 0.0;
		GpsStarted = false;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener();
		// On click listener
		// Plot the trajectory on the
		btnHome.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// if we use a trace we should read the trace first
				if (useTrace) {
					if (!previous.equals(custFilePath) || !ploted) {

						dialog = ProgressDialog.show(MainActivity.this, "", "Loading. Please wait...", true);
						dialog.show();

						Intent i = new Intent(MainActivity.this, PlotPath.class);
						startActivity(i);
						++startend;
					}
				} else if (record || !ploted) {
					// start drawing on the map
					startMap();
					ploted = true;
				}
				// end-start of the trajectory
				startend = (++startend) % 2;
				// go to end or start point
				goToPoint();

			}
		});
		btnPin.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				clckpin = (++clckpin % 4);
				switch (clckpin) {
				case 0:
					MainActivity.pin = false;
					btnPin.setBackgroundResource(R.drawable.none);
					break;
				case 1:
					MainActivity.pin = true;
					MainActivity.kindofdraw = R.drawable.pin;
					btnPin.setBackgroundResource(R.drawable.thumbtack);

					break;
				case 2:
					MainActivity.pin = true;
					MainActivity.kindofdraw = R.drawable.arrow;
					btnPin.setBackgroundResource(R.drawable.btnarr);
					break;
				case 3:
					MainActivity.pin = true;
					MainActivity.kindofdraw = R.drawable.flag;
					btnPin.setBackgroundResource(R.drawable.btnflag);
					break;
				default:
					break;
				}
				startMap();
			}
		});
		btnCol.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				clckcol = (++clckcol % 6);
				switch (clckcol) {
				case 0:
					DrawPathOverlay.color = Color.RED;
					btnCol.setBackgroundResource(R.drawable.colred);
					break;
				case 1:
					DrawPathOverlay.color = Color.GREEN;
					btnCol.setBackgroundResource(R.drawable.colgreen);
					break;
				case 2:
					DrawPathOverlay.color = Color.BLUE;
					btnCol.setBackgroundResource(R.drawable.colblue);
					break;
				case 3:
					DrawPathOverlay.color = Color.YELLOW;
					btnCol.setBackgroundResource(R.drawable.colyel);
					break;
				case 4:
					DrawPathOverlay.color = Color.BLACK;
					btnCol.setBackgroundResource(R.drawable.colblk);
					break;
				case 5:
					DrawPathOverlay.color = Color.GREEN / 256;
					btnCol.setBackgroundResource(R.drawable.color);
					break;
				default:
					break;
				}
				startMap();
			}
		});
		btnWid.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				clicks = (++clicks % 3);
				switch (clicks) {
				case 0:
					btnWid.setBackgroundResource(R.drawable.width1);
					break;
				case 1:
					btnWid.setBackgroundResource(R.drawable.width2);
					break;
				case 2:
					btnWid.setBackgroundResource(R.drawable.width3);
					break;
				default:
					break;
				}
				DrawPathOverlay.widthOfPath = clicks * 2 + 2;
				startMap();
			}

		});
		btnClose.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				btnClose.setBackgroundResource(R.drawable.disclose);
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage("Are you sure you want to exit?").setCancelable(false).setPositiveButton("Yes", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						btnClose.setBackgroundResource(R.drawable.close);
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
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (startpoints.size() > 0)
					next = (++next % startpoints.size());
				if (startend == 0) {
					if (startpoints.size() > 0)
						myMC.setCenter(startpoints.get(next));
				} else {
					if (endpoints.size() > 0)
						myMC.setCenter(endpoints.get(next));
				}
			}

		});
	}

	protected void goToPoint() {
		// TODO Auto-generated method stub
		if (!ClientTrajectory.isEmpty() && startpoints.size() > 0 && endpoints.size() > 0) {
			if (startend == 0) {
				geoPoint = startpoints.get(0);
			} else {
				geoPoint = endpoints.get(0);
			}
		} else {
			geoPoint = new GeoPoint(34667805, 33000183);
			myMC.setZoom(15);
		}
		// the next button should be other trajectory but not mine
		next = 0;
		// put the center of the map the first coordinate
		myMC.setCenter(geoPoint);
	}

	private void appClose() {
		if (GpsStarted) {
			Editor editor = preferences.edit();
			editor.putString("custFile", outpFileloc);
			editor.commit();
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e) {
				warn.setText("writer Exception:" + e);
				warn.show();
			}

			locationManager.removeUpdates(locationListener);
		}
		finish();
		System.exit(0);
	}

	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {
			double latidute = 0.0;
			double longitude = 0.0;
			if (loc != null) {
				try {
					latidute = loc.getLatitude();
					longitude = loc.getLongitude();
					if (!((int) (latidute * Density) - (int) (platidute * Density) == 0 && (int) (longitude * Density) - (int) (plongitude * Density) == 0)) {
						platidute = latidute;

						plongitude = longitude;
						myPoint temppoint = new myPoint(latidute, longitude);
						ClientTrajectory.add(temppoint);
						writer.append(latidute + "\t" + longitude + "\n");
						warn.setText(

						"Latitude (X): " + latidute + ",Longitude(Y)=" + longitude);
						warn.show();
					}
				} catch (Exception e) {
					warn.setText("Exception in onLocationChanged(): " + e.getMessage());
					warn.show();
				}
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	}

	void readFromFile() {
		if (AlreadyRead && previous.equals(inFile.getName()))
			return;
		// it's the same file
		previous = inFile.getName();

		// get the Latitude from file and store it to an array
		try {
			String line;
			String[] temp;
			myPoint temppoint;
			while ((line = reader.readLine()) != null) {
				temp = line.split("\t");
				temppoint = new myPoint(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
				ClientTrajectory.add(temppoint);
			}

		} catch (Exception e) {
			warn.setText("Exception while reading from file:\n" + e.getMessage());
			warn.show();
		}
		AlreadyRead = true;
	}

	void startMap() {
		startpoints.clear();
		endpoints.clear();
		mapView.clearDisappearingChildren();
		mapView.removeAllViews();
		mapView.clearAnimation();
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(sat);
		mapView.setTraffic(traffic);
		mapView.setStreetView(strview);
		mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		drawable = this.getResources().getDrawable(kindofdraw);
		// for the map
		// STARTING POINT
		if (ClientTrajectory == null || ClientTrajectory.size() <= 0) {
			return;
		}
		GeoPoint startGP = new GeoPoint((int) (ClientTrajectory.get(0).getX() * 1E6), (int) (ClientTrajectory.get(0).getY() * 1E6));
		startpoints.add(startGP);
		geoPoint = startGP;
		// myMC.setCenter(geoPoint);
		// myMC.setZoom(15);
		mapView.getOverlays().add(new DrawPathOverlay(startGP, startGP, 0));
		// NAVIGATE THE PATH
		GeoPoint gp1;
		GeoPoint gp2 = startGP;
		for (int i = 0; i < ClientTrajectory.size(); i++) {
			gp1 = gp2;
			// watch out! For GeoPoint, first:latitude, second:longitude
			gp2 = new GeoPoint((int) (ClientTrajectory.get(i).getX() * 1E6), (int) (ClientTrajectory.get(i).getY() * 1E6));
			mapView.getOverlays().add(new DrawPathOverlay(gp1, gp2, 0));
		}
		endpoints.add(gp2);
		// END POINT
		mapView.getOverlays().add(new DrawPathOverlay(gp2, gp2, 0));
		itemizedOverlay = new MyItemizedOverlay(drawable, getApplicationContext());
		if (pin) {
			OverlayItem overlayStart = new OverlayItem(startGP, "", "");
			itemizedOverlay.addOverlay(overlayStart);
			OverlayItem overlayEnd = new OverlayItem(gp2, "", "");
			itemizedOverlay.addOverlay(overlayEnd);
			mapOverlays.add(itemizedOverlay);
		}
		if (srchdn)
			searchMap();
		mapView.getController().animateTo(startGP);
		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);

	}

	void getPref() {
		useTrace = preferences.getBoolean("OffGps", false);
		append = preferences.getBoolean("Append", false);
		privacy = preferences.getBoolean("PrivacyTraj", false);
		Density = (int) preferences.getInt("Density", 1);
		Density = (int) Math.pow(10, Density);
	}

	void stopGps() {

		GpsStarted = false;
		// Close gps
		custFilePath = outpFileloc;
		SharedPreferences.Editor ed = preferences.edit();
		ed.putString(EditTextBrowser.PATH, custFilePath);
		ed.commit();
		// EditTextBrowser.txb.setText(custFilePath);
		try {
			if (writer != null)
				writer.close();
		} catch (Exception e) {
			warn.setText("writer Exception:" + e);
			warn.show();
		}

		locationManager.removeUpdates(locationListener);
		GpsStarted = false;
		warn.setText(

		"GPS tracking stoped...\nClick Connect to server to upload data.");
		warn.show();

	}

	private void gpsWifiAlert(final String sett) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your " + sett + " seems to be disabled, do you want to enable it?").setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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

	void startGps() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			gpsWifiAlert("GPS");
		}

		if (useTrace) {
			warn.setText("Uncheck the use trace file check box.\n(menu preferences)");
			warn.show();

			return;
		}
		GpsStarted = true;
		ClientTrajectory.clear();
		outpFileloc = preferences.getString("outpFile", "n/a");

		if (outpFileloc.equals("n/a") || outpFileloc.equals("")) {
			warn.setText("Provide path/filename for output file.\n(menu preferences)");
			warn.show();
			return;
		}
		// initialize the coordinates file
		try {
			outFile = new File(outpFileloc);
			writer = new BufferedWriter(new FileWriter(outFile, append));
		} catch (Exception e) {
			warn.setText(

			"Error when opening output file:\n" + e.getMessage());
			warn.show();
		}

		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			GpsStarted = true;
		} catch (Exception e) {
			warn.setText("Unexpected Error on GPS service:\n" + e.getMessage());
			warn.show();
		}
		warn.setText("GPS tracking started...\nCollecting Position Information...");
		warn.show();

	}

	void searchMap() {

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		mapView.setSatellite(sat);
		mapView.setTraffic(traffic);
		mapView.setStreetView(strview);
		// for the map
		// STARTING POINT
		String[] tmppnt;

		int length = QueryTrajectories.length;
		info.setText("K=" + QueryTrajectories.length);
		info.show();
		for (int k = 0; k < length; k++) {
			mapOverlays = mapView.getOverlays();
			tmppnt = QueryTrajectories[k][0].split(" ");
			GeoPoint startGP;
			try {
				startGP = new GeoPoint((int) (Double.parseDouble(tmppnt[0]) * 1E6), (int) (Double.parseDouble(tmppnt[1]) * 1E6));
			} catch (Exception e) {
				// TODO: handle exception
				startGP = new GeoPoint(0, 0);

			}
			startpoints.add(startGP);
			myMC = mapView.getController();
			geoPoint = startGP;
			// myMC.setCenter(geoPoint);
			// myMC.setZoom(15);
			mapView.getOverlays().add(new DrawPathOverlay(startGP, startGP, DrawPathOverlay.color ^ (256 << 5) * (k + 1) * 75));
			// NAVIGATE THE PATH
			GeoPoint gp1;
			GeoPoint gp2 = startGP;
			for (int i = 0; i < QueryTrajectories[k].length; i++) {
				gp1 = gp2;
				try {
					tmppnt = QueryTrajectories[k][i].split(" ");
					// watch out! For GeoPoint, first:latitude, second:longitude
					gp2 = new GeoPoint((int) (Double.parseDouble(tmppnt[0]) * 1E6), (int) (Double.parseDouble(tmppnt[1]) * 1E6));
				} catch (Exception e) {
					continue;
				}
				mapView.getOverlays().add(new DrawPathOverlay(gp1, gp2, DrawPathOverlay.color ^ (256 << 5) * (k + 1) * 75));
			}
			endpoints.add(gp2);
			// END POINT
			mapView.getOverlays().add(new DrawPathOverlay(gp2, gp2, DrawPathOverlay.color ^ (256 << 5) * (k + 1) * 75));
			// mapView.getController().animateTo(startGP);

		}

		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);

	}

	boolean setUsername() {
		// Check for any change in preferences
		username = MainActivity.preferences.getString("usrName", "");
		if (username.length() <= 0 || username.contains(" \t\n\b\"\')({}[];")) {

			isUsernameSet = false;
			return false;
		} else {
			isUsernameSet = true;
			return true;
		}

	}

	void PlotSmrt(int lcss) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage("LCSS = " + lcss / 100.0 + "!\nDo you want to plot the trajectory?").setCancelable(false)
				.setPositiveButton("Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						srchdn = true;
						startMap();

					}
				}).setNegativeButton("No", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						btnClose.setBackgroundResource(R.drawable.close);
						srchdn = false;
						return;

					}
				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	void PlotSmrtPrivate(int lcss) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage("LCSS = " + lcss / 100.0 + "!\nBut The trajectory is private").setNeutralButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				btnClose.setBackgroundResource(R.drawable.close);
				srchdn = false;
				return;

			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	void readFromSampleFile(InputStream in) {
		if (AlreadyRead && MainActivity.previous.equals(MainActivity.custFilePath))
			return;
		MainActivity.ClientTrajectory.clear();
		MainActivity.ploted = true;
		// it's the same file
		MainActivity.previous = MainActivity.custFilePath;

		InputStreamReader isr = new InputStreamReader(in);

		BufferedReader br = new BufferedReader(isr);

		// get the Latitude from file and store it to an array
		try {
			String line;
			String[] temp;
			myPoint temppoint;
			while ((line = br.readLine()) != null) {
				temp = line.split("\t");
				temppoint = new myPoint(Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
				MainActivity.ClientTrajectory.add(temppoint);
			}

		} catch (Exception e) {
			warn.setText("Exception while reading from file:\n" + e.getMessage());
			warn.show();
		}
		try {
			br.close();
			isr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void fillHashMapError(HashMap<Integer, String> errors) {
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(getResources().openRawResource(R.raw.errors));

			// normalize text representation
			doc.getDocumentElement().normalize();

			NodeList listOfErrors = doc.getElementsByTagName("error");

			for (int s = 0; s < listOfErrors.getLength(); s++) {

				Node firstErrorNode = listOfErrors.item(s);
				if (firstErrorNode.getNodeType() == Node.ELEMENT_NODE) {

					Element firstErrorElement = (Element) firstErrorNode;

					// -------
					NodeList numberList = firstErrorElement.getElementsByTagName("number");
					Element numberElement = (Element) numberList.item(0);

					NodeList textFNList = numberElement.getChildNodes();
					String number = ((Node) textFNList.item(0)).getNodeValue().trim();

					// -------
					NodeList descriptionList = firstErrorElement.getElementsByTagName("description");
					Element descriptionElement = (Element) descriptionList.item(0);

					NodeList textLNList = descriptionElement.getChildNodes();
					String description = ((Node) textLNList.item(0)).getNodeValue().trim();

					errors.put(Integer.parseInt(number), description);

				}// end of if clause

			}// end of for loop with s var

		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}