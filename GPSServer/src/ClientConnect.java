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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class ClientConnect {

	private static final String CRLF = "\r\n";
	static String arrName[] = { "Michael", "Christopher", "Matthew", "Joshua", "Andrew", "David",
			"Justin", "Daniel", "James", "Robert", "John", "Joseph", "Ryan", "Nicholas",
			"Jonathan", "William", "Brandon", "Anthony", "Kevin", "Eric", "Jessica", "Ashley",
			"Amanda", "Sarah", "Jennifer", "Brittany", "Stephanie", "Samantha", "Nicole",
			"Elizabeth", "Lauren", "Megan", "Tiffany", "Heather", "Amber", "Melissa", "Danielle",
			"Emily", "Rachel", "Kayla" };
	static BufferedReader in;
	static BufferedReader console;
	static DataOutputStream out;
	Socket socket;

	static public void Send(String msg, OutputStream out) throws IOException {
		out.write((msg + CRLF).getBytes());
		out.flush();
	}

	ClientConnect(String ip) throws UnknownHostException, IOException {
		socket = new Socket(ip, 8080);

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		console = new BufferedReader(new InputStreamReader(System.in));
		out = new DataOutputStream(socket.getOutputStream());
	}

	public static void main(String[] args) {
		try {

			Random r = new Random();
			if (args.length > 0)
				new ClientConnect(args[0]);
			else
				new ClientConnect("127.0.0.1");
			System.out.print("Receive : ");
			//Receive message from client
			String message = in.readLine();
			System.out.println(message);
			System.out.print("Enter response: ");
			String response = "USER " + arrName[r.nextInt(arrName.length)];
			System.out.println(response);
			Send(response, out);
			if (response.equals("QUIT"))
				return;
			message = in.readLine() ;
			System.out.print("Receive : ");
			System.out.println(message);
			while (true) {
				message = in.readLine() ;
				System.out.print("Receive for SEARCH: ");
				System.out.println(message);
				// if(response.startsWith("RETRIEVE")){
				// response = "1 2\t3 4";// Upper Bound
				// System.out.println(response);
				// out.println(response);
				//			
				// }

				System.out.print("Enter response: ");
				response = "+OK CALCULATE";//
				System.out.println(response);
				Send(response, out);
				System.out.print("Enter response: ");
				response = console.readLine();
				if (!response.equals("QUIT"))
					response = "+OK quid " + response;// Upper Bound
				System.out.println(response);
				Send(response, out);
				if (response.equals("QUIT"))
					return;
				message = in.readLine();
				System.out.print("Receive : ");
				System.out.println(message);
				if (message.startsWith("CLOSE"))
					continue;

				System.out.print("Enter response: ");
				response = console.readLine() ;
				if (!response.equals("QUIT"))
					response = "+OK quid " + response + " 1 2\t3 4";// lcss
				System.out.println(response);
				Send(response, out);
				if (response.equals("QUIT"))
					return;

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
