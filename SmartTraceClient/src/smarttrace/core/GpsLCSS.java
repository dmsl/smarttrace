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

import java.util.ArrayList;

import smarttrace.core.gps.myPoint;

//Optimize LCSS algorithm
public class GpsLCSS {
	public static double LCSS(ArrayList<myPoint> serverTraj, ArrayList<myPoint> clientTraj, double e) {
		// end here
		// PHASE 1
		// Initialize first column and row to assist the DP Table
		double[][] L = new double[clientTraj.size() + 1][serverTraj.size() + 1];

		for (int i = 1; i < clientTraj.size() + 1; i++) {
			for (int j = 1; j < serverTraj.size() + 1; j++) {
				if (containInFolder(clientTraj.get(i - 1).getX(), serverTraj.get(j - 1).getX(), e)
						&& containInFolder(clientTraj.get(i - 1).getY(), serverTraj.get(j - 1).getY(), e))
					L[i][j] = L[i - 1][j - 1] + 1;
				else
					L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
			}
		}
		// int k, l;
		// PHASE 2
		/*
		 * ////////////////////////////////////////////////////////////////////
		 * In this phase we can find the path on array L that shows the common
		 * values between the trajectory
		 */// /////////////////////////////////////////////////////////////////
			// k = clientTraj.size();
			// l = serverTraj.size();
			//
			// while (true) {
			// if ((k == 0) || (l == 0))
			// break;
			//
			// // Match
			// if (containInFolder(clientTraj.get(k - 1).getX(),serverTraj.get(l
			// -
			// 1).getX(),e)
			// &&containInFolder(clientTraj.get(k - 1).getY(),serverTraj.get(l -
			// 1).getY(),e))
			// {
			// // Move to L[i-1][j-1] in next round
			// k--;
			// l--;
			//
			// } else {
			// // Move to max { L[k][l-1],L[k-1][l] } in next round
			// if (L[k][l - 1] >= L[k - 1][l])
			// l--;
			// else
			// k--;
			//
			// }
			//
			// }
		double result = L[clientTraj.size()][serverTraj.size()];

		return ((result / (double) (Math.min(clientTraj.size(), serverTraj.size()))) * 100);
	}

	// Create an envelope around the trajectory
	private static boolean containInFolder(double valueC, double valueS, double e) {
		if ((valueC - e) < valueS && (valueC + e) > valueS)
			return true;
		return false;

	}

}
