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

public class GpsUB {
	public static double upperBound(ArrayList<myPoint> serverTraj,
			ArrayList<myPoint> clientTraj, double eUb) {
		//double the size of the envelope that is used for lcss
		double e=eUb*2;
		double UB = 0.0;
		int size = Math.min(serverTraj.size(), clientTraj.size());
		for (int i = 0; i < size; i++) {
			// create envelope
			if (serverTraj.get(i).getX() < (clientTraj.get(i).getX() + e)
					&& serverTraj.get(i).getX() > (clientTraj.get(i).getX() - e))
				if (serverTraj.get(i).getY() < (clientTraj.get(i).getY() + e)
						&& serverTraj.get(i).getY() > (clientTraj.get(i).getY() - e))
					UB++;
		}
		if (size == 0)
			return 0;
		else
			return (UB / size )* 100;
	}
}