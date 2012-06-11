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

