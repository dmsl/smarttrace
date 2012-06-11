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
import java.util.HashMap;
import java.util.HashSet;

public class WifiUB {
	public static double upperBound(ArrayList<HashMap<String, Integer>> client, ArrayList<HashMap<String, Integer>> server, double eUb) {

		int level = Math.max(server.size(), client.size());

		// Set that will have all the access point that we want to check
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
		// set what you want to use

		if (level <= 0)
			return 0;
		return ((double) intersection.size() / (double) union.size()) * 100;
	}

}