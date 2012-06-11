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
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Server {

	// Volatile variables for session ID and query ID
	volatile static String SID = "";
	volatile static String QID = "";

	// static final Lock lock = new ReentrantLock();
	// static final Condition write = lock.newCondition();
	static final boolean DEBUG = false;

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	// Default port
	private static int port = 65000;
	// interval time for checking when the client are left
	private static int interval = 2000;
	// Synchronization variable for searching
	volatile static synServer synSearch;
	public static final String CRLF = "\r\n";

	// TODO: Reset the clients to 3
	volatile static int clients = 2;
	volatile static ServeClient threadSearch = null;
	// private synServer lock;
	volatile static Integer countUBCalls = 0;
	volatile static String QueryTrajectory = "";
	volatile static String[] ResponseTrajectory = null;
	volatile static Boolean Trajset = false;
	volatile static Hashtable<ServeClient, Double> ClientList;
	volatile ColorPane serverDisplay;
	volatile static JScrollPane scrollPane;
	volatile static ArrayList<ThreadUB> LCSSList;
	volatile static ArrayList<ThreadUB> UBList;
	ServerSocket listenSocket;
	Socket connection;
	OutputStream outStream;
	DataOutputStream outDataStream;
	TextField filetext;
	String message;
	volatile private synServer synClient;
	// boolean flag = true;
	volatile static Integer countLCSSCalls = 0;
	static boolean graphics;
	volatile static Integer K = 1;

	public Server() {

		// initialize variables
		synSearch = new synServer();
		synClient = new synServer();
		LCSSList = new ArrayList<ThreadUB>();
		LCSSList.clear();
		UBList = new ArrayList<ThreadUB>();
		UBList.clear();
		ClientList = new Hashtable<ServeClient, Double>();
		ClientList.clear();

		// initialize all the parameters
		initPrams();

		// choose if you want to continue with grahics

		if (!graphics)
			return;

		class graphicDisplay extends Frame implements WindowListener, KeyListener {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public graphicDisplay() {
				// TODO Auto-generated constructor stub
				super("Server");

				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				serverDisplay = new ColorPane();
				scrollPane = new JScrollPane();

				serverDisplay.setBorder(new javax.swing.border.TitledBorder(null, "Server is running", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 16), new java.awt.Color(0, 102, 102)));
				// scrollPane.add(logDisplay);
				add(BorderLayout.CENTER, scrollPane);
				scrollPane.getViewport().setBackground(Color.white);
				scrollPane.getViewport().add(serverDisplay);
				filetext = new TextField();
				filetext.addKeyListener(this);
				add(BorderLayout.PAGE_END, filetext);
				addWindowListener(this);
				setSize(650, 400);
				setVisible(true);

			}

			public void windowActivated(WindowEvent e) {
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				setVisible(false);
				this.dispose();
				System.exit(0);
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			/** Handle the key typed event from the text field. */
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n' && filetext.isEditable()) {

					if (!isIntNumber(filetext.getText())) {
						updateGUI(Color.RED, "You gave wrong clients's number.\nThe default number (3) will be used\n");
					} else {
						clients = Integer.parseInt(filetext.getText());
						if (clients < 3) {
							updateGUI(Color.RED, "You gave invalid clients's number.\nThe default number (3) will be used\n");
							clients = 3;
						} else

							updateGUI(Color.BLACK, "You gave " + clients + " clients\n");
					}
					filetext.setText("");
					filetext.setText("The server has started!");
					filetext.setEditable(false);
					synClient.unpause("synClient.unpause();");

				}
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
			}

			private boolean isIntNumber(String num) {
				try {
					Integer.parseInt(num);
				} catch (NumberFormatException nfe) {
					return false;
				}
				return true;
			}
		}

		@SuppressWarnings("unused")
		graphicDisplay serverDisplay = new graphicDisplay();
	} // end Server_Basic constructor

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

				serverDisplay.append(c, s);

			}

		});

	}

	public void runServer() {

		try {

			updateGUI(Color.BLACK, "Server started ");
			try {
				InetAddress here = InetAddress.getLocalHost();
				String host = here.getHostName();
				// get Server's ip address
				IPaddress ip = new IPaddress();
				updateGUI(Color.BLACK, "on ");
				updateGUI(Color.BLUE, host);
				updateGUI(Color.BLACK, " with ip:" + ip.getInterfaces() + "\nport :" + port + "\n");
			} catch (UnknownHostException e) {
				updateGUI(Color.RED, "Problem with local host\n");
			}
			// TODO SKIP THE AKSING PART
			/*
			 * updateGUI(Color.BLACK,
			 * "Please give the minimum require number of clients at bottom of the window\n"
			 * ); if (!graphics) { BufferedReader in = new BufferedReader(new
			 * InputStreamReader(System.in)); try { String sclients =
			 * in.readLine(); clients = Integer.parseInt(sclients); } catch
			 * (Exception e) { // TODO: handle exception updateGUI(Color.RED,
			 * "You gave wrong clients's number.\nThe default number (3) will be used\n"
			 * );
			 * 
			 * } } else { // Synchronized the server with the key event below
			 * synClient.pause("synClient.pause();"); }
			 */
			// Create the TCP Server socket
			listenSocket = new ServerSocket(port);

			// new runnable to RESPONSIBLE FOR accept OR NOT ACCEPT CLIENTS
			Runnable r = new Runnable() {
				public void run() {
					try {
						while (true) {
							try {

								connection = listenSocket.accept();
								//
							} catch (Exception e) {
								// TODO: handle exception
								break;
							}
							updateGUI(new java.awt.Color(0, 202, 202), "Connection request received\n");
							ServeClient thread = new ServeClient(connection, serverDisplay);

							thread.start();
						}
					} catch (Exception x) {
						// in case ANY exception slips through
						x.printStackTrace();
					}
				}
			};
			// new thread to RESPONSIBLE FOR accept OR NOT ACCEPT CLIENTS
			Thread internalThread = new Thread(r);
			internalThread.start();
			updateGUI(Color.BLACK, "\n-------------------------------------------------------------------------\n");
			Set<ServeClient> set;
			// Barrier for the server.The clients should be more than the
			// minimum clients
			while (true) {

				while (ClientList.size() < clients && !Server.Trajset) {
					try {
						if (DEBUG)
							System.out.println("(countClients < clients)Barrier:" + ClientList.size() + "<" + clients);
						Thread.sleep(interval);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				synchronized (ClientList) {
					set = ClientList.keySet();
				}

				// wait until all threads are started
				// This is a barrier if client searcher is the first one
				while (countUBCalls < ClientList.size() - 1) {
					try {
						if (DEBUG)
							System.out.println("(countUBClients < ClientList.size()-1)Barrier:" + countUBCalls + "<" + (ClientList.size() - 1));
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				// Barrier that waits the other threads to calculate upper bound
				while (UBList.size() < countUBCalls) {
					try {
						if (DEBUG)
							System.out.println("(UBtable.size() < countUBClients)Barrier:" + UBList.size() + "<" + countUBCalls);
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				if (DEBUG)
					System.err.println("THE UPPERBOUNT IS PASS!!!!!!!");

				// sort the array based on my comparator
				synchronized (UBList) {
					Collections.sort(UBList, new myComparator());
				}
				// the first and the second of elements is the element with the
				// biggest UB
				for (int i = 0; i < K; i++) {
					synchronized (UBList) {
						if (UBList.size() > i) {
							UBList.get(i).thread.Oktraj = true;
							UBList.get(i).thread.synUB.unpause("UBList.get(0).thread.synUB.unpause();");
							synchronized (Server.countLCSSCalls) {
								Server.countLCSSCalls++;
							}
						} else
							break;
					}
				}
				// wait for all threads
				while (LCSSList.size() < clients - 1 && ClientList.size() > clients - 1) {
					try {
						if (DEBUG)
							System.out.println("(LCSSList.size() < 2 && ClientList.size() > 2)Barrier:" + LCSSList.size() + "<2 && " + ClientList.size()
									+ "> 2");

						Thread.sleep(interval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// the second element
				synchronized (LCSSList) {
					Collections.sort(LCSSList, new myComparator());
				}

				synchronized (LCSSList) {
					for (int i = countLCSSCalls; i < UBList.size(); i++) {
						synchronized (UBList) {
							try {
								if (LCSSList.get(0).UBLCSS < UBList.get(i).UBLCSS) {
									UBList.get(i).thread.Oktraj = true;
									synchronized (Server.countLCSSCalls) {
										Server.countLCSSCalls++;
									}
								}
							} catch (Exception e) {
								// TODO: handle exception
								synchronized (Server.countLCSSCalls) {
									Server.countLCSSCalls--;
								}
								break;
							}
						}
						UBList.get(i).thread.synUB.unpause("UBList.get(i).thread.synUB.unpause();");
					}
				}
				// Unpause the other threads through a monitor
				for (int j = countLCSSCalls; j < UBList.size(); j++) {
					UBList.get(j).thread.synUB.unpause("");
				}

				while (LCSSList.size() < countLCSSCalls && UBList.size() > 2) {
					try {
						if (DEBUG)
							System.out.println("(LCSSthread.size() < countLCSSCalls) Barrier:" + LCSSList.size() + "<" + countLCSSCalls);
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				synchronized (LCSSList) {

					Collections.sort(LCSSList, new myComparator());
				}
				// send the trajectory of the most common
				// by calling the responsible thread
				if (threadSearch != null) {
					synchronized (threadSearch) {
						threadSearch.synSearch.unpause("threadSearch.synSearch.unpause();");

					}
				}
				// Print the winner
				if (LCSSList.size() > 0) {
					updateGUI(Color.BLUE, LCSSList.get(0).thread.usr);
					updateGUI(Color.BLACK, " has the most similar trajectory with you with ip " + LCSSList.get(0).thread.ip + "\n");
					// wait until it's job is finished
					synSearch.pause("synSearch.pause();");
				} else
					updateGUI(Color.RED, "The minimum client left!!!");
				updateGUI(Color.BLACK, "\n-------------------------------------------------------------------------\n");

				// Initialize all the variables
				// END
				if (threadSearch != null)
					synchronized (threadSearch) {
						threadSearch = null;
					}
				Server.Trajset = false;

				synchronized (QueryTrajectory) {
					QueryTrajectory = "";
				}
				synchronized (countUBCalls) {
					countUBCalls = 0;
				}
				synchronized (countLCSSCalls) {
					countLCSSCalls = 0;
				}
				synchronized (ClientList) {
					for (ServeClient thread : set) {
						thread.syn.unpause("thread.syn.unpause();");
					}
				}
				// initialize the counter for the clients and the tables
				synchronized (LCSSList) {
					LCSSList.clear();
				}
				synchronized (UBList) {
					UBList.clear();
				}
			}// end of while(true)
		} // end try
		catch (IOException ex) {
			updateGUI(Color.RED, "\n" + ex.getMessage() + "\n");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(-1);
		} // end catch
	}// end runServer

	public static void initPrams() {
		try {
			FileInputStream fileInputStream = new FileInputStream("./GPS.cfg");
			// Get the object of InputStream.
			DataInputStream inputStream = new DataInputStream(fileInputStream);
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			// Reads a line of text. A line is considered to be terminated by
			// any one of a line feed ('\n'), a carriage return ('\r'), or a
			// carriage return followed immediately by a linefeed.
			while ((line = buffReader.readLine()) != null) {

				// The StringTokenizer methods do not distinguish among
				// identifiers, numbers, and quoted strings, nor do they
				// recognize and skip comments.
				StringTokenizer st = new StringTokenizer(line);
				String strCur = st.nextToken(" ");

				// Get the appropriate value and initialize the variables
				if (strCur.equals("clients"))
					clients = Integer.parseInt(st.nextToken(" "));
				else if (strCur.equals("interval"))
					interval = Integer.parseInt(st.nextToken(" "));
				else if (strCur.equals("port"))
					port = Integer.parseInt(st.nextToken(" "));

			}
			// Close the input stream
			inputStream.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	public static void main(String[] args) {

		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(System.in));

		// System.out.print("Do you want graphics [y/n] : ");

		String message = "n";
		// TODO skip the read line option
		// try {
		// message = in.readLine();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// graphics = false;
		// }
		if (message.equals("y")) {
			graphics = true;
		} else
			graphics = false;

		Server server = new Server();
		server.runServer();

	} // end main

} // end Server_Basic
// create comparator for sorting the array

class myComparator implements Comparator<ThreadUB> {
	public int compare(ThreadUB o1, ThreadUB o2) {
		// TODO Auto-generated method stub
		return (o1.UBLCSS > o2.UBLCSS ? -1 : (o1.UBLCSS == o2.UBLCSS ? 0 : 1));

	}
}

// class to store UB and the thread of that UB
class ThreadUB {
	double UBLCSS = 0.0;
	ServeClient thread = null;

	ThreadUB(double d, ServeClient s) {
		this.UBLCSS = d;
		this.thread = s;
	}

}
