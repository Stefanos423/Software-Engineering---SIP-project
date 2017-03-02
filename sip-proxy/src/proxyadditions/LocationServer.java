package proxyadditions;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocationServer {
	public final static long PING_TIMEOUT = 5000;
	private static LocationServer instance = null;
	private static DatabaseServer dbServer;
	private HashMap<String, PingWaiter> waiters = new HashMap<>();

	private LocationServer() {
		LocationServer.dbServer = DatabaseServer.get();
	}

	public static LocationServer get() {
		if (instance == null)
			instance = new LocationServer();
		return instance;
	}
	
	public boolean isUserOnline(String username) {
		return dbServer.isUserOnline(username);
	}
	
	public void setUserOnline(String username, boolean status) {
		if (!dbServer.isConnected()) return;
		if (status) {
			if (waiters.get(username) == null) {
				PingWaiter waiter = new PingWaiter(username);
				waiters.put(username, waiter);
				waiter.start();
			}
		}
		else {
			waiters.remove(username);
			dbServer.setUserOnline(username, false);
			TimerServer.get().stopAllUserCalls(username);
		}
	}
	
	public void userPinged(String username) {
		PingWaiter waiter = waiters.get(username);
		if (waiter == null)  // user has probably timed out
			setUserOnline(username, true);
		else
			waiter.pinged();
	}
	
	private class PingWaiter extends Thread {
		private final String username;
		private AtomicBoolean pinged = new AtomicBoolean();
		
		public PingWaiter(String username) {
			this.username = username;
		}
		
		public void pinged() {
			pinged.set(true);
		}
		
		@Override
	    public void run() {
			/* Sets user's status to online
			 * and it will wait PING_TIMEOUT ms for a ping
			 * if it doesn't get it, user is set to offline and thread exits
			 */
			
			dbServer.setUserOnline(username, true);
			pinged.set(true);
			
			while (pinged.get()) {
				pinged.set(false);
				try {
					Thread.sleep(PING_TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace(System.err);
					break;
				}
			}
			setUserOnline(username, false);
		}

		@Override
		public boolean equals(Object arg0) {
			if (this == arg0)
				return true;
			if (arg0 == null)
				return false;
			if (getClass() != arg0.getClass())
				return false;
			
			PingWaiter other = (PingWaiter) arg0;
			return username.equals(other.username);
		}

		@Override
		public int hashCode() {
			return username.hashCode();
		}
	}
}