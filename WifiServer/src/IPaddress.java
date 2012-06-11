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
import java.net.*;
import java.util.*;

public class IPaddress {
	public InetAddress getInterfaces() {
		InetAddress ip = null;
		try {
			Enumeration<?> e = NetworkInterface.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) e.nextElement();
				Enumeration<?> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					ip = (InetAddress) e2.nextElement();
				}
				if ((ni.getName().startsWith("e")||ni.getName().startsWith("w")) && ni.isUp() && !ni.isVirtual())
					return ip;
				if(Server.DEBUG)
				System.out.println(""+ni.getName()+ip);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ip;
	}

	public static void main(String[] args) {
		IPaddress ip = new IPaddress();
		
	System.out.println("IPaddress.main()"+ip.getInterfaces());
	
	}
}