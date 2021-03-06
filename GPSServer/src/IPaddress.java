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