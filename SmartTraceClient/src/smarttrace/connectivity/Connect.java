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
package smarttrace.connectivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import smarttrace.core.GpsUB;
import smarttrace.core.GpsLCSS;
import smarttrace.core.gps.MainActivity;
import smarttrace.core.gps.More;
import smarttrace.core.gps.myPoint;
import smarttrace.messages.Messages;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class Connect extends Thread {
	// Enumeration described in RFC
	enum state {
		CLOSED, ESTABLISH, CALCULATE, SEARCH, RETRIEVE;
	}

	private BufferedWriter writer = null;
	private DataOutputStream out;
	private BufferedReader in;
	private String serverAddress = new String();
	private String epsilon = new String();
	private String usr = new String();
	private Socket connection = null;
	private String ClientTRAJ;
	private double lcss;
	private String SSID;
	private String QUID;
	private String host = new String();
	private String buf = new String();
	private String[] traj = null;
	private InputStream inStream = null;
	private OutputStream outStream = null;
	private String psw = "";
	private String LCSS;
	private int port = 80;
	private String serverPort = "80";
	static state curState = state.CLOSED;
	public static Handler messageHandler;

	private String rmPREFIX(String msg) {
		String[] sArr = msg.split(" ");
		String temp = new String();
		for (int i = 1; i < sArr.length; i++)
			temp += sArr[i] + " ";
		return temp;
	}

	// Function For the MESSAGES
	static public void Send(String msg, OutputStream out) throws IOException {
		out.write((msg + Messages.CRLF).getBytes());
		out.flush();
	}

	public Connect(Context c, boolean flag, String username) {
		// set username
		usr = username;

		// initialized the message handler for the messages from the thread
		messageHandler = new Handler() {

			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Message msg1 = new Message();
				switch (msg.arg1) {
				// These means that the client want to use smartTrace search
				// We create new thread so the GUI doesn't block
				case Messages.SEARCH:

					// // TODO Auto-generated method stub
					if (!curState.equals(state.ESTABLISH)) {
						switch (curState) {
						case CALCULATE:
							msg1.arg1 = Messages.STATECAL;
							break;
						case SEARCH:
							msg1.arg1 = Messages.STATESER;
							break;
						case RETRIEVE:
							msg1.arg1 = Messages.STATERET;
							break;

						default:
							msg1.arg1 = Messages.SERVER_ERR;
							break;
						}

						MainActivity.messageHandler.sendMessage(msg1);
						return;
					}

					// change the trajectory to string
					ClientTRAJ = "";
					for (int i = 0; i < MainActivity.ClientTrajectory.size(); i++) {
						ClientTRAJ += MainActivity.ClientTrajectory.get(i).getX() + " " + MainActivity.ClientTrajectory.get(i).getY() + "\t";
					}

					String temp = MainActivity.preferences.getString("parK", "1");
					try {
						MainActivity.K = Integer.parseInt(temp);
					} catch (Exception e) {
						// TODO: handle exception
						MainActivity.K = 1;
					}
					try {
						Send(("SEARCH " + MainActivity.K + " " + SSID + " " + ClientTRAJ), out);
					} catch (IOException e) {
						// send a massage to main thread
						msg1.arg1 = Messages.OTHER;
						MainActivity.messageHandler.sendMessage(msg1);
					}
					// write everything to the transaction string
					synchronized (More.terminal) {
						More.terminal += "Client send : SEARCH " + SSID + " Client TRAJECTORY \n";

					}
					// Send message to the main thread that you are searching
					msg1.arg1 = Messages.SEARCH;
					MainActivity.messageHandler.sendMessage(msg1);
					// }
					// }).start();
					break;
				case Messages.QUIT:

					if (connection == null) {
						// appClose();
						return;
					}
					try {
						Send("QUIT", out);
						connection.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					synchronized (More.terminal) {
						More.terminal += "Client send : QUIT \n";

					}
					Message msg11 = new Message();
					msg11.arg1 = Messages.QUIT;
					MainActivity.messageHandler.sendMessage(msg11);
					close();
					return;

				default:

					break;
				}

			}
		};
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.arg1 = Messages.OTHER;
				MainActivity.messageHandler.sendMessage(msg);
				More.terminal += "Client(" + thread.getId() + ").Connection ERROR :\n" + ex.getLocalizedMessage();
				// toast.show();
			}
		});
	}

	public void run() {
		// Check for any change in preferences
		// Check if the user-name is set
		if (!MainActivity.isUsernameSet) {
			Message msg = new Message();
			msg.arg1 = Messages.SERVER_ERR;
			msg.arg2 = 0;
			MainActivity.messageHandler.sendMessage(msg);
			return;
		}
		serverAddress = MainActivity.preferences.getString("serverAdd", "n/a");
		if (serverAddress.equals("n/a") || serverAddress.equals("")) {
			Message msg = new Message();
			msg.arg1 = Messages.INVALID_ADDRESS;
			MainActivity.messageHandler.sendMessage(msg);
			return;
		}

		// serverPort
		serverPort = MainActivity.preferences.getString("serverPort", "65000");
		if (serverPort.equals("n/a") || serverPort.equals("")) {
			Message msg = new Message();
			msg.arg1 = Messages.INVALID_ADDRESS;
			MainActivity.messageHandler.sendMessage(msg);
			return;
		}
		// Check for any change in preferences
		epsilon = MainActivity.preferences.getString("epsilon", "n/a");
		if (epsilon.equals("n/a") || epsilon.equals("")) {
			Message msg = new Message();
			msg.arg1 = Messages.DEFAULT_EPSILON;
			MainActivity.messageHandler.sendMessage(msg);
			// toast.show();
			epsilon = "2";
		}
		// close the gps
		if (MainActivity.GpsStarted) {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e) {
			}
			MainActivity.locationManager.removeUpdates(MainActivity.locationListener);
			MainActivity.GpsStarted = false;
		}
		serverConnect();
	}

	// Function For the ESTABLISH STATE
	private boolean ESTABLISH() throws IOException {
		curState = state.ESTABLISH;
		// GET THE +OK READY FROM SERVER
		buf = in.readLine();
		More.terminal += "Server Response: " + buf + "\n";
		// Wait For Proceeding
		// Get The Session Id
		buf = "USER " + usr + " " + psw;
		// SEND A +OK REPLY and user-name TO SERVER
		Send(buf, out);
		More.terminal += "Client Send: " + buf + "\n";
		// GET THE +OK or -ERR FROM SERVER
		buf = in.readLine();
		More.terminal += "Server Response: " + buf + "\n";
		if (buf.startsWith("+OK")) {
			// Set SessionID
			SSID = rmPREFIX(buf);
			return true;
		}
		return false;

	}

	// Function For the SEARCH STATE
	private boolean SEARCH() throws IOException {
		curState = state.SEARCH;
		// Read The Message To Continue
		More.terminal += "Server Response: " + buf + "\n";
		// Set SessionID
		QUID = rmPREFIX(buf);
		// Read The Message To Continue
		buf = in.readLine();
		synchronized (More.terminal) {
			More.terminal += "Server Response: " + buf + "\n";
		}
		if (buf.contains("Goodbye"))
			return false;
		LCSS = rmPREFIX(buf);
		return true;
	}

	// Function For the CALCULATE STATE
	private boolean CALCULATE() throws IOException {
		if (!buf.startsWith("CALCULATE"))
			return true;
		curState = state.CALCULATE;
		synchronized (More.terminal) {
			More.terminal += "Server Response: " + "CALCULATE queryTrajectory" + "\n";
		}// toast.show();
		try {
			QUID = buf.split(" ")[1];
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		String query = rmPREFIX(buf);
		buf = "+OK CALCULATE";
		Send(buf, out);
		synchronized (More.terminal) {
			More.terminal += "Client send :" + buf + "\n";
		}
		// split the points
		traj = query.split("\t");
		if (traj == null) {
			connection.close();
			return true;
		}

		MainActivity.ServerTrajectory = new ArrayList<myPoint>();
		String[] tmppnt;
		for (int j = 0; j < traj.length; j++) {
			tmppnt = traj[j].split(" ");
			// synchronized (More.terminal) {
			// More.terminal += traj[j] + "\n";
			// }
			if (tmppnt != null)

				if (tmppnt.length > 1)
					try {
						MainActivity.ServerTrajectory.add(new myPoint(Double.parseDouble(tmppnt[0]), Double.parseDouble(tmppnt[1])));
					} catch (Exception e) {
						// TODO: handle exception
						continue;
					}
		}

		// CALCULATE THE UB
		Message msg = new Message();
		msg.arg1 = Messages.UPBOUND;
		MainActivity.messageHandler.sendMessage(msg);
		// SEND THE UB TO SERVER
		double eps = 0.1;
		try {
			eps = Double.parseDouble(epsilon);
		} catch (Exception e) {
			// TODO: handle exception
		}
		buf = "+OK " + QUID + " " + GpsUB.upperBound(MainActivity.ServerTrajectory, MainActivity.ClientTrajectory, eps);
		Send(buf, out);
		synchronized (More.terminal) {
			More.terminal += "Client send :" + buf + "\n";
		}
		// Wait for the Server to send "LCSS" OR "CLOSE"
		buf = in.readLine();
		synchronized (More.terminal) {
			More.terminal += "Server Response: " + buf + ".\n";
		}
		// IF LCSS SEND THE Coordinates[] ARRAY
		if (buf.startsWith("LCSS")) {
			synchronized (More.terminal) {
				More.terminal += "Calculate LCCS and send it to server...\n";
			}
			lcss = GpsLCSS.LCSS(MainActivity.ServerTrajectory, MainActivity.ClientTrajectory, eps);
			ClientTRAJ = "";
			for (int i = 0; i < MainActivity.ClientTrajectory.size(); i++) {
				ClientTRAJ += MainActivity.ClientTrajectory.get(i).getX() + " " + MainActivity.ClientTrajectory.get(i).getY() + "\t";
			}
			if (MainActivity.privacy)
				buf = "+OK " + QUID + " " + lcss + " " + "PRIVATE";
			else
				buf = "+OK " + QUID + " " + lcss + " " + ClientTRAJ;
			Send(buf, out);
			synchronized (More.terminal) {
				More.terminal += "Client send :" + buf + "\n";
			}
		}
		return true;

	}

	// Function For the CALCULATE STATE
	private boolean RETRIEVE() throws IOException {
		curState = state.RETRIEVE;
		// send RETRIEVE message to the server
		Send("RETRIEVE " + QUID, out);
		More.terminal += "Client send :" + "RETRIEVE" + "\n";
		buf = in.readLine();
		if (!buf.startsWith("+OK"))
			return true;
		buf = rmPREFIX(buf);
		if (buf.contains("PRIVATE"))
			MainActivity.QueryTrajectories = null;
		else {
			String[] temp = buf.split("&");
			MainActivity.QueryTrajectories = new String[temp.length][];
			int i = 0;
			for (String ss : temp) {
				MainActivity.QueryTrajectories[i] = ss.split("\t");
				i++;
			}

		}
		More.terminal += "Server response : [" + buf.split("\t") + "]\n";
		return true;

	}

	private void searchDone() {
		// TODO Auto-generated method stub
		// close the connection
		Message smsg = new Message();
		smsg.arg1 = Messages.ENDSRCH;
		smsg.arg2 = (int) (Double.parseDouble(LCSS) * 100);
		MainActivity.messageHandler.sendMessage(smsg);
	}

	// Connect to server to use the upper bound
	private void serverConnect() {
		// open the coordinates file
		if (MainActivity.ClientTrajectory.size() == 0) {
			Message msg = new Message();
			msg.arg1 = Messages.FIRST_PLOT;
			MainActivity.messageHandler.sendMessage(msg);
			return;
		}
		try {
			// ACCEPT FROM SERVER
			host = serverAddress;
			try {
				port = Integer.parseInt(serverPort);
			} catch (Exception e) {
				// TODO: handle exception
				port = 80;
			}

			connection = new Socket(host, port);
			synchronized (More.terminal) {
				More.terminal += "Socket created.\nConnecting to server on " + host + "...\n";
			}
			// INITIALIZE THE IN AND OUT STREAMS
			inStream = connection.getInputStream();
			in = new BufferedReader(new InputStreamReader(inStream));
			outStream = connection.getOutputStream();
			out = new DataOutputStream(outStream);
		} catch (IOException e) {
			return;
		}
		// -------------------------------ESTABLISH-STATE------------------------------------------
		try {
			// If the ESTABLISH-STATE is wrong
			if (!ESTABLISH()) {
				close();
				return;
			}
		} catch (IOException e) {
			synchronized (More.terminal) {
				More.terminal += "Error Connection: " + e.getMessage();
			}
			close();
			return;
		}
		// -------------------------------ESTABLISH-STATE------------------------------------------

		while (true) {

			// the thread is send a search query
			// and the server answer with an +OK or -ERR message
			curState = state.ESTABLISH;
			try {
				buf = in.readLine();
			} catch (IOException e) {
				synchronized (More.terminal) {
					More.terminal += "Error Connection: " + e.getMessage();
					// toast.show();
				}
				break;
			}

			// -------------------------------SEARCH-STATE------------------------------------------
			if (buf.startsWith("+OK")) {
				if (buf.contains("Goodbye")) {
					More.terminal += "\n+OK Goodbye\n";
					break;
				}
				try {
					if (!SEARCH())
						break;
				} catch (IOException e) {
					synchronized (More.terminal) {
						More.terminal += "Error Connection: " + e.getMessage();
						// toast.show();
					}
					break;
				} // end try
				try {
					if (!RETRIEVE())
						break;
				} catch (IOException e) {
					synchronized (More.terminal) {
						More.terminal += "Error Connection: " + e.getMessage();
						// toast.show();
					}
					break;
				} // end try
				searchDone();
				continue;

			} else if (buf.startsWith("-ERR")) {
				Message msg1 = new Message();
				msg1.arg1 = Messages.SERVER_ERR;
				try {
					msg1.arg2 = Integer.parseInt(buf.split(" ")[1]);
				} catch (Exception e) {
					// TODO: handle exception
					msg1.arg2 = 0;
				}
				MainActivity.messageHandler.sendMessage(msg1);
				More.terminal += "Server Busy\nPlease try again";
				continue;
			}
			// -
			// ------------------------------SEARCH-STATE------------------------------------------
			// -------------------------------CALCULATE-STATE------------------------------------------
			try {
				if (!CALCULATE())
					break;
			} catch (IOException e) {
				synchronized (More.terminal) {
					More.terminal += "Error Connection: " + e.getMessage();
					// toast.show();
				}
				break;
			} // end try
				// -------------------------------CALCULATE-STATE------------------------------------------

		}// end while

		// ELSE CLOSE THE PROGRAM
		// ending and closing the application
		try {
			connection.close();
			synchronized (More.terminal) {
				More.terminal += "Done.\nConnection closed.\n";
				// toast.show();
			}
		} catch (IOException e) {
			synchronized (More.terminal) {
				More.terminal += "Error Connection: " + e.getMessage();
				// toast.show();
			}
			close();
			return;
		} // end try
		Message mmsg = new Message();
		mmsg.arg1 = Messages.ENDCONN;
		MainActivity.messageHandler.sendMessage(mmsg);
		return;

	}

	/**
	 * 
	 */
	// Closing the connection and update the GUI
	public void close() {
		// ending and closing the application
		try {
			connection.close();
		} catch (IOException e) {
		}
		Message msg = new Message();
		msg.arg1 = Messages.OTHER;
		MainActivity.messageHandler.sendMessage(msg);
	}

}
