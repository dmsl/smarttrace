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
class Consumer implements Runnable {

	public void run() {
		synchronized (concurrency.NAME) {
		while (concurrency.NAME < 5000) {
		

				concurrency.NAME++;
				
			}

		}
	}
}

class Producer implements Runnable {

	public void run() {
		synchronized (concurrency.NAME) {

		while (concurrency.NAME < 5000) {
		
			
				concurrency.NAME++;
				if(concurrency.NAME>1000){
					System.out.println("i will return");
				return;
				 }
				
				
				
			}
		}
	}
}

public class concurrency {

	public static Integer NAME = 0;

	
	public static void main(String[] args) {
		Thread one = new Thread(new Producer());
		Thread two = new Thread(new Consumer());
		one.start();
		two.start();
		try {
			one.join();
			two.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Final="+concurrency.NAME);
		
	}

}

