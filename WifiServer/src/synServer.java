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
public class synServer {
	private volatile boolean threadSuspended;
	private volatile boolean threadAlready;
	public synchronized void pause(String string) {
		if(!threadAlready)
		threadSuspended = false;
		if(Server.DEBUG)
		System.err.println("PAUSE:"+string);
		while (!threadSuspended)
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("InterruptedException caught");
			}
			threadAlready=false;
	}

	public synchronized void unpause(String string) {
		if(Server.DEBUG)
		System.err.println("UNPAUSE:"+string);
		threadSuspended = true;
		notify();
		threadAlready=true;

	}
}