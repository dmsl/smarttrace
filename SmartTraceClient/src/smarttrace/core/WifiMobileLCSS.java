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
package smarttrace.core;

import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WifiMobileLCSS {
	//Optimize LCSS algorithm
	@SuppressLint("UseValueOf")
	public static double LCSS(ArrayList<HashMap<String, Integer>> client,
			ArrayList<HashMap<String, Integer>> server, double eLCSS) {

		if (server.size() <= 0) {
			return -1;
		}
		int level = Math.max(client.size(), server.size());

		double LccsSum = 0.0;
		// set that will have all the access point that we want to check
		HashSet<String> set;
		HashSet<String> sets = new HashSet<String>();
		HashSet<String> setc = new HashSet<String>();
		HashSet<String> union = new HashSet<String>();
	
		// fill the access point that we want to check
		int j = 0;
		for (j = 0; j < level; j++) {
			if (j < server.size()) {
				union.addAll(server.get(j).keySet());
				sets.addAll(server.get(j).keySet());
			}
			if (j < client.size()) {
				union.addAll(client.get(j).keySet());
				setc.addAll(client.get(j).keySet());
			}
		}
		HashSet<String> intersection = new HashSet<String>(setc);
		intersection.retainAll(sets);
		//set what you want to use
		set=union;

		
		Integer num = 0;
		ArrayList<Integer> serverTemp = new ArrayList<Integer>();
		ArrayList<Integer> clientTemp = new ArrayList<Integer>();
		for (String str : set) {
			serverTemp.clear();
			clientTemp.clear();
			for (int i = 0; i < level; i++) {
				if (i < client.size()) {
					num = client.get(i).get(str);
					num = num == null ? new Integer(-110) : num;
					clientTemp.add(num);
				}
				if (i < server.size()) {
					num = server.get(i).get(str);
					num = num == null ? new Integer(-110) : num;
					serverTemp.add(num);
				}
			}
			LccsSum += LCSS1D(serverTemp, clientTemp, eLCSS);

			
		}

		return LccsSum / set.size()*((double)intersection.size()/(double)union.size());
	}

	// LCCSS for one dimension
	static double LCSS1D(ArrayList<Integer> serverTraj, ArrayList<Integer> clientTraj, double eps) {
		// PHASE 1
		// Initialize first column and row to assist the DP Table
		double[][] L = new double[clientTraj.size() + 1][serverTraj.size() + 1];

		for (int i = 1; i < clientTraj.size() + 1; i++) {
			for (int j = 1; j < serverTraj.size() + 1; j++) {
				if (containInFolder(clientTraj.get(i - 1), serverTraj.get(j - 1), eps))
					L[i][j] = L[i - 1][j - 1] + 1;
				else
					L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
			}
		}

		double result = L[clientTraj.size()][serverTraj.size()];

		return ((result / (double) (Math.min(clientTraj.size(), serverTraj.size()))) * 100);
	}

	// Create an envelope around the trajectory
	public static boolean containInFolder(int valueC, int valueS, double eps) {
		if ((valueC - eps) <= valueS && (valueC + eps) >= valueS)
			return true;
		return false;

	}

	public  static void readFromFile(String custFilePath,ArrayList<HashMap<String, Integer>> Trajectory ) {

		String line = "";

		File inFile = new File(custFilePath);
		BufferedReader reader = null;
		if (!inFile.exists()) {
			System.out.print("Provide path/filename for input file that exists.\n(menu preferences)\n");
		
			return;
		}
		try {
			reader = new BufferedReader(new FileReader(inFile));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
		e1.printStackTrace();
		}

		try {
			HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
			while ((line = reader.readLine()) != null) {
				if (line.equals("#")) {
					
					Trajectory.add(tempMap);
					tempMap = new HashMap<String, Integer>();
				} else {
					tempMap.put(line.split(",")[0], Integer.parseInt(line.split(",")[1]));
				
				}
			}
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
