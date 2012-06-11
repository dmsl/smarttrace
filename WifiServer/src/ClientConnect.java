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
			String message = in.readLine();

			System.out.println(message);
			System.out.print("Enter response: ");
			String response = "USER " + arrName[r.nextInt(arrName.length)];// console.readLine()
																			// +
																			// "\n";
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
