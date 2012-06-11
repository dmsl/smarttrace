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
import java.awt.Color;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket; //import java.net.SocketException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.lang.Thread;

enum state {
	CLOSED, ESTABLISH, CALCULATE, SEARCH, RETRIEVE;
}

public class ServeClient extends Thread {
	// private static final int MINITES = 1200000;
	double UB = -1.0;
	InetAddress ip;
	boolean Oktraj = false;
	private Socket connection;
	String usr;
	String pass;
	String responseUsr;
	synServer syn;
	synServer synUB;
	synServer synSearch;
	// The server thread has a state in any time
	state curState = state.CLOSED;
	private OutputStream outStream;
	private DataOutputStream outDataStream;
	private InputStream inStream;
	private BufferedReader inDataStream;
	private String buf;
	private ColorPane logDisplay;
	private boolean quit = false;// VARIABLE THAT INDICATES IF THE STATES ARE

	// CORRECTLY

	private void updateGUI(final Color c, final String s) {
		if (!Server.graphics) {
			if (c.equals(Color.RED))
				System.err.print(s);
			else
				System.out.print(s);
			return;
		}
		EventQueue.invokeLater(new Runnable() {

			public void run() {

				logDisplay.append(c, s);

			}

		});
		yield();

	}

	private void init() {
		// INITIALIZED VARIABLES

		UB = -1.0;
		Oktraj = false;
		quit = false;// VARIABLE THAT INDICATES IF THE STATES ARE CORRECTLY
		curState = state.ESTABLISH;
		// In case the thread is the searcher
		if (Server.threadSearch == this) {
			Server.synSearch.unpause("Server.synSearch.unpause();");
		}
	}

	// FUNCTION FOR THE OK MESSAGES
	public boolean checkIfQuit(String msg) throws IOException {
		if (msg == null) {
			connection.close();
			quit = true;
			return true;
		}
		if (msg.equals("QUIT")) {
			Send(SendOK("Goodbye"), outDataStream);
			connection.close();
			quit = true;
			return true;
		}
		return false;
	}

	// FUNCTION FOR THE OK MESSAGES
	static public String SendOK(String msg) {
		return "+OK " + msg;
	}

	// FUNCTION FOR THE ERROR MESSAGES
	static public String SendERR(String msg) {
		return "-ERR " + msg;
	}

	// FUNCTION FOR THE OK k-Trajectories
	static public String SendTraj(String[] msg) {
		String conMsg = "";
		for (int i=0;i< msg.length;i++) {
			conMsg += msg[i] ;
			if(i<msg.length-1)
			conMsg +="&";
		}
		System.out.println("--------->Message length="+msg.length);
		return "+OK " + conMsg;

	}

	// FUNCTION FOR THE MESSAGES
	static public void Send(String msg, OutputStream out) throws IOException {
		out.write((msg + Server.CRLF).getBytes());
		out.flush();
	}

	// FUNCTION FOR THE CREATION OF SESSIONID
	static public String CreateSessionId() {
		return UUID.randomUUID().toString();
	}

	// DEFAULT CONSTRUCTOR
	public ServeClient(Socket connection, ColorPane aModel) {
		this.connection = connection;
		// try {
		// this.connection.setSoTimeout(MINITES);
		// } catch (SocketException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		this.logDisplay = aModel;
	}

	private String rmPREFIX(String msg) {
		String[] sArr = msg.split(" ");
		String temp = new String();
		for (int i = 1; i < sArr.length; i++)
			temp += sArr[i] + " ";
		return temp;
	}

	// FUNCTION FOR THE ESTABLISH STATE
	// ALL THE EXCEPTIONS IN THIS FUNCTION ARE HANDLE IN THE MAIN FUNCTION

	private boolean ESTABLISH() throws IOException {
		// SEND TO CLIENT +OK MESSAGE
		Send(SendOK("STP READY"), outDataStream);
		// CHECK IF THE PASSWORD AND USER-NAME IS CORRECT
		buf = inDataStream.readLine();
		if (Server.DEBUG)
			System.out.println("ESTABLISH:Server Send:" + SendOK("READY"));
		// CHECK THAT IS QUIT COMMAND
		if (checkIfQuit(buf))
			return false;
		usr = rmPREFIX(buf);
		if (Server.DEBUG)
			System.out.println("(" + getId() + ")ESTABLISH:Client Response: " + buf);
		// CHECK USER NAME
		if (checkPass(usr)) {

			Server.SID = CreateSessionId();
			Send(SendOK(Server.SID), outDataStream);
			if (Server.DEBUG)
				System.out.println("(" + getId() + ")ESTABLISH:Server Send: " + SendOK(Server.SID));
			// THE CLIENT IS ACCEPTABLE
			synchronized (Server.ClientList) {
				Server.ClientList.put(this, -1.0);
			}
			curState = state.ESTABLISH;
			return true;
		} else {
			Send(SendERR("401"), outDataStream);

			if (Server.DEBUG)
				System.out.println("(" + getId() + ")ESTABLISH:Server Send: " + SendERR("401"));
			return false;
		}

	}

	// FUNCTION FOR THE SEARCH STATE
	private boolean SEARCH() throws IOException {
		// CHECK THAT IS QUIT COMMAND
		if (checkIfQuit(buf))
			return false;

		if (Server.DEBUG)
			System.out.println("(" + getId() + ")SEARCH:Client Response: " + buf);
		if (!buf.startsWith("SEARCH")) {
			return true;
		}
		curState = state.SEARCH;
		// THE TRAJECTORY IS ALREADY SET
		if (Server.Trajset) {
			Send(SendERR("511"), outDataStream);
			// STOP THE WHOLE CONNECTION
			return true;
		} else {
			synchronized (Server.ClientList) {
				if (Server.ClientList.size() < Server.clients) {
					Send(SendERR("513"), outDataStream);
					// System.out.println("SEARCH:Server Send: " + SendERR(""));
					// STOP THE QUERY NOT ENOUGH CLIENTS
					return true;
				}
				synchronized (Server.Trajset) {
					Server.Trajset = true;
				}

			}
			synchronized (Server.QID) {
				Server.threadSearch = this;
				Server.QID = CreateSessionId();
			}
			synchronized (Server.QueryTrajectory) {
				try{
					//remove the search keyword
					buf=rmPREFIX(buf);
				Server.K = Integer.parseInt(buf.split(" ")[0]);
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					Server.K=1;
				}
				Server.QueryTrajectory = rmPREFIX(buf);

			}
			Send(SendOK(Server.QID), outDataStream);

			// --------------------- BROADCAST TO ALL TO MOVE IN CALCULATE STATE
			Set<ServeClient> set;
			synchronized (Server.ClientList) {
				set = Server.ClientList.keySet();
			}
			Iterator<ServeClient> iter = set.iterator();
			buf = "CALCULATE " + Server.QID + " " + Server.QueryTrajectory;
			// BROADCAST "+PROC" TO ALL CLIENT
			while (iter.hasNext()) {

				ServeClient temp;
				synchronized (Server.ClientList) {
					temp = iter.next();
				}
				// DON'T SEND TO THIS CONNECTION
				// IT IS THE CURRENT ONE
				if (temp.connection.equals(this.connection))
					continue;
				OutputStream toutStream;
				DataOutputStream toutDataStream;
				toutStream = temp.connection.getOutputStream();
				toutDataStream = new DataOutputStream(toutStream);
				try {
					if (temp.curState == state.RETRIEVE || temp.curState == state.SEARCH) {
						continue;
					}
					Send(buf, toutDataStream);
					synchronized (Server.countUBCalls) {
						Server.countUBCalls++;
					}
				} catch (IOException e) {

					continue;
				}
				// Very Important Don't Close The ToutDataStream
				// The handle is close the main object

				if (Server.DEBUG)
					System.out.println("(" + getId() + ")SEARCH:Server Send to " + temp.usr + " : "
							+ buf);
			}

			updateGUI(Color.BLACK, "The trajectory of ");
			updateGUI(Color.BLUE, usr);
			updateGUI(Color.BLACK, " : " + Server.QueryTrajectory + "\n");

			// LOCK AND UNLOCK OF SHARE VARIABLES

		}
		synSearch.pause("Client.synSearch.pause");
		synchronized (Server.LCSSList) {
			try {
				if (Server.LCSSList.size() > 0) {
					//Make response equals with the minimum of Clients number and Parameter K
					Server.ResponseTrajectory= new String[Math.min(Server.LCSSList.size(), Server.K)]; 
					//Fill the response K array
					for (int i = 0; i < Server.LCSSList.size() && i<Server.K; i++) {
						Server.ResponseTrajectory[i] = Server.LCSSList.get(i).thread.responseUsr;
					}
					Send(Server.QID + " " + Server.LCSSList.get(0).UBLCSS, outDataStream);
					if (Server.DEBUG)
						System.out.println("K="+Server.K+"...(" + getId() + ")SEARCH:Server Send: " + Server.QID
								+ " " + Server.LCSSList.get(0).UBLCSS);
				} else {
					Send(SendERR("512"), outDataStream);
					if (Server.DEBUG)
						System.out.println("(" + getId() + ")SEARCH:Server Send: " + SendERR(""));

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// return false;

			}
		}
		Server.synSearch.unpause("Server.synSearch.unpause();");
		return true;
	}

	// FUNCTION FOR THE CALCULATE STATE
	private boolean CALCULATE() throws InterruptedException, IOException {
		// CHECK THAT IS QUIT COMMAND
		if (checkIfQuit(buf))
			return false;
		// IF MESSAGE IS NOT DEFERMENT WITH "+OK CALCULATE" THEN STOP
		if (!buf.equals("+OK CALCULATE")) {

			return true;
		}
		curState = state.CALCULATE;
		// System.out.println(getId() + "Clients are " + Server.countClients);
		while (!Server.Trajset) {
			Thread.sleep(500);
		}

		// System.out.println("("+getId() +")CALCULATE:Server.trajectory" +
		// Server.QueryTrajectory + "\n");
		// WAIT FOR THE UPPER BOUND
		try {
			buf = inDataStream.readLine();
		} catch (IOException e) {
			// We should decrease the number of UB calls to wait less answers
			synchronized (Server.countUBCalls) {
				Server.countUBCalls--;
			}

			return false;
		}
		// CHECK THAT IS QUIT COMMAND
		if (checkIfQuit(buf)) {
			// We should decrease the number of UB calls to wait less answers
			synchronized (Server.countUBCalls) {
				Server.countUBCalls--;
				if (Server.DEBUG)
					System.err.println("Server.countUBCalls(" + Server.countUBCalls + ")");
			}
			return false;
		}
		// LOCK AND UNLOCK OF SHARE VARIABLES
		if (Server.DEBUG)
			System.out.println("(" + getId() + ")CALCULATE:Client Response: " + buf);
		synchronized (Server.ClientList) {
			Server.ClientList.put(this, UB);
		}
		try {
			String[] UBmessage = buf.split(" ");

			UB = Double.parseDouble(UBmessage[2]);
		} catch (Exception e) {
			synchronized (Server.countUBCalls) {
				Server.countUBCalls--;
			}
			Send(SendERR("514"), outDataStream);
			return false;
		}
		// UPDATE GRAPHICS
		updateGUI(Color.BLACK, "(");
		updateGUI(Color.BLUE, usr);
		updateGUI(Color.BLACK, connection.getInetAddress() + ") UB=" + UB + "\n");

		// ADD THE UB TO THE UB LIST
		synchronized (Server.UBList) {
			Server.UBList.add(new ThreadUB(UB, this));
		}
		// System.out.println("CALCULATE:Server.UBtable.size()=" +
		// Server.UBList.size());

		// WAIT FOR THE COMPUTATION OF THE MAIN PROCESSES

		this.synUB.pause("this.synUB.pause();");

		if (Oktraj) {
			// SEND TO CLIENT LCSS QID MESSAGE TO GET THE SCORE
			if (Server.DEBUG)
				System.out.println("(" + getId() + ")CALCULATE: Server send: " + Server.QID);
			try {
				Send("LCSS " + Server.QID, outDataStream);
			} catch (IOException e) {
				// We should decrease the number of LCSS calls to wait less
				// answers
				synchronized (Server.countLCSSCalls) {
					Server.countLCSSCalls--;
					if (Server.DEBUG)
						System.err.println("Server.countLCSSCalls(" + Server.countLCSSCalls + ")");
				}
				// We should decrease the number of UB calls to wait less
				// answers
				synchronized (Server.countUBCalls) {
					Server.countUBCalls--;
					if (Server.DEBUG)
						System.err.println("Server.countUBCalls(" + Server.countUBCalls + ")");
				}
				return false;
			}
		} else {
			buf = new String("CLOSE " + Server.QID);
			// SEND TO CLIENT +OK CLOSE MESSAGE FOR TRAJECTORY
			// System.out.println("CALCULATE:Server send: " + buf + " to " +
			// this.usr);
			try {
				Send(buf, outDataStream);
			} catch (IOException e) {
				// We should decrease the number of LCSS calls to wait less
				// answers
				synchronized (Server.countLCSSCalls) {
					Server.countLCSSCalls--;
					if (Server.DEBUG)
						System.err.println("Server.countLCSSCalls(" + Server.countLCSSCalls + ")");
				}
				// We should decrease the number of UB calls to wait less
				// answers
				synchronized (Server.countUBCalls) {
					Server.countUBCalls--;
					if (Server.DEBUG)
						System.err.println("Server.countUBCalls(" + Server.countUBCalls + ")");
				}
				return false;
			}
			if (Server.DEBUG)
				System.out.println("(" + getId() + ")CALCULATE: Server send: " + buf);
			return true;
		}
		// READ THE LCSS
		try {
			buf = inDataStream.readLine();
		} catch (IOException e) {

			// We should decrease the number of LCSS calls to wait less answers
			synchronized (Server.countLCSSCalls) {
				Server.countLCSSCalls--;
				if (Server.DEBUG)
					System.err.println("Server.countLCSSCalls(" + Server.countLCSSCalls + ")");

			}
			// We should decrease the number of UB calls to wait less answers
			synchronized (Server.countUBCalls) {
				Server.countUBCalls--;
				if (Server.DEBUG)
					System.err.println("Server.countUBCalls(" + Server.countUBCalls + ")");

			}
			return false;
		}
		// CHECK THAT IS QUIT COMMAND
		if (checkIfQuit(buf)) {
			// The LCSS list should be fill with the values of LCCSS call in
			// this stage because if the top clients leave the you should call
			// the others that are active
			synchronized (Server.LCSSList) {
				Server.LCSSList.add(new ThreadUB(-1.0, this));
			}
			// We should decrease the number of LCSS calls to wait less answers
			synchronized (Server.countLCSSCalls) {
				Server.countLCSSCalls--;
				if (Server.DEBUG)
					System.err.println("Server.countLCSSCalls(" + Server.countLCSSCalls + ")");

			}
			// We should decrease the number of UB calls to wait less answers
			synchronized (Server.countUBCalls) {
				Server.countUBCalls--;
				if (Server.DEBUG)
					System.err.println("Server.countUBCalls(" + Server.countUBCalls + ")");

			}
			return false;
		}
		if (Server.DEBUG)
			System.out.println("(" + getId() + ")CALCULATE: Client Response: " + buf);
		Double LCSS = new Double(0.0);
		try {
			buf = rmPREFIX(buf);
			buf = rmPREFIX(buf);

			LCSS = Double.parseDouble(buf.split(" ")[0]);
			responseUsr = rmPREFIX(buf);
		} catch (Exception e) {
			// TODO: handle exception
			synchronized (Server.LCSSList) {
				Server.LCSSList.add(new ThreadUB(LCSS, this));
			}
		}
		ip = this.connection.getInetAddress();
		// System.out.println("CALCULATE:Server.countLCSSClients=" +
		// Server.countLCSSClients);
		synchronized (Server.LCSSList) {
			Server.LCSSList.add(new ThreadUB(LCSS, this));
		}
		if (Server.DEBUG)
			System.out.println("CALCULATE:[*]LCSS=" + LCSS);
		// UPDATE GRAPHICS
		updateGUI(Color.BLACK, "(");
		updateGUI(Color.BLUE, usr);
		updateGUI(Color.BLACK, ip + ") LCSS=");
		updateGUI(Color.BLACK, LCSS + "\n");
		//

		return true;
	}

	// FUNCTION FOR THE RETRIEVE STATE
	private boolean RETRIEVE() throws IOException, InterruptedException {
		if (!buf.startsWith("RETRIEVE"))
			return true;
		curState = state.RETRIEVE;
		// SEND RETRIEVE MASSAGE TO THE CLIENT THAT HAVE THE BEST LCSS-SCORE
		Send(SendTraj(Server.ResponseTrajectory), outDataStream);
		responseUsr = "";
		if (Server.DEBUG)
			System.out.println("(" + getId() + ")RETRIEVE: Server Send: " + buf);
		return true;

	}

	//FUNCTION FOR THREADS
	public void run() {
		try {
			// INITIALIZE THE TRAJECTORIES;
			syn = new synServer();
			synUB = new synServer();
			synSearch = new synServer();
			try {
				outStream = connection.getOutputStream();
				inStream = connection.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			inDataStream = new BufferedReader(new InputStreamReader(inStream));
			outDataStream = new DataOutputStream(outStream);
			// -------------------------------ESTABLISH-STATE------------------------------------------
			try {
				// If the ESTABLISH-STATE is wrong

				if (!ESTABLISH())
					return;
			} catch (IOException e) {
				synchronized (Server.ClientList) {
					Server.ClientList.remove(this);
				}
				try {
					connection.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return;
			}
			init();
			while (!quit) {
				// -------------------------------ESTABLISH-STATE------------------------------------------
				// The main read is determine the next state
				// Read The Message To Continue
				try {
					buf = inDataStream.readLine();
					if (Server.DEBUG)
						System.out.println("CHOOSE TYPE OF CLIENT");
				} catch (IOException e) {
					if (Server.DEBUG)
						System.out.println("----------------4---------------");
					break;
				}

				// --------------------------------SEARCH-STATE--------------------------------------------
				try {
					if (!SEARCH()) {

						break;

					}
				} catch (IOException e) {
					if (Server.DEBUG)
						System.out.println("----------------3---------------");
					break;
				}
				// --------------------------------SEARCH-STATE--------------------------------------------
				// -------------------------------CALCULATE-STATE------------------------------------------

				try {
					if (!CALCULATE())
						break;
				} catch (IOException e) {
					if (Server.DEBUG)
						System.out.println("----------------2---------------");
					break;

				}
				// -------------------------------CALCULATE-STATE------------------------------------------
				try {
					// if (!search)
					if (!RETRIEVE())
						break;
				} catch (IOException e) {
					if (Server.DEBUG)
						System.out.println("----------------1---------------");
					break;
				}
				// if (!search)
				// syn.pause("syn.pause();");
				init();
			}

			// CLOSE THE CONNECTION AFTER THE QUIT COMMAND SEND OR THE
			// CONNECTION WAS CLOSED
			try {
				connection.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			removeClient();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// finally {
		// removeClient();
		//			
		// }
	}

	private void removeClient() {
		synchronized (Server.ClientList) {
			Server.ClientList.remove(this);
			if (Server.DEBUG)
				System.err.println("Server.ClientList.size() = " + Server.ClientList.size());

		}

		synchronized (Server.UBList) {
			if (Server.UBList.remove(this))
				// synchronized (Server.countUBCalls) {
				// Server.countUBCalls--;
				// System.err.println("Server.countUBCalls = " +
				// Server.countUBCalls);
				// }
				if (Server.DEBUG)
					System.err.println("Server.UBList.size() = " + Server.UBList.size());

		}
		if (Server.DEBUG)
			System.err.println("Before:Server.LCSSList.size() = " + Server.LCSSList.size());
		synchronized (Server.LCSSList) {
			if (Server.LCSSList.remove(this))
				// synchronized (Server.countLCSSCalls) {
				// Server.countLCSSCalls--;
				// System.err.println("Server.countLCSSCalls = " +
				// Server.countLCSSCalls);
				// }
				if (Server.DEBUG)
					System.err.println("After:Server.LCSSList.size() = " + Server.LCSSList.size());
		}

		updateGUI(Color.LIGHT_GRAY, "User " + usr + " has left.\n");

		// // IF THE CLIENT PASS THE LCSS CALCULATION
		// synchronized (Server.LCSSthread) {
		// Server.LCSSthread.remove(this);
		// }
		// CHECK IF THE THREADSEARCH ISN'T NULL AND CHECK IF IS THE CURRENT
		if (Server.threadSearch != null)
			if (Server.threadSearch.equals(this.connection)) {
				synchronized (Server.QueryTrajectory) {
					Server.Trajset = false;
				}
			}
		if (Server.DEBUG)
			System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++");
		// THREAD RETURN
	}

	private boolean checkPass(String userpass) {
		return true;
	}
}
