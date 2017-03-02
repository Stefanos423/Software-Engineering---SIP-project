package proxyadditions;

import java.util.HashSet;

public class ForwardingServer {

	private static ForwardingServer instance = null;
	private final DatabaseServer dbServer;

	private ForwardingServer() {
		dbServer = DatabaseServer.get();
	}

	public static ForwardingServer get() {
		if (instance == null)
			instance = new ForwardingServer(); 
		return instance;
	}
	
	public boolean addForwarding(int forwarderId, int forwardeeId) {
		return dbServer.addForwarding(forwarderId, forwardeeId);
	}
	
	public void removeForwarding(int forwarderId) {
		dbServer.removeForwarding(forwarderId);
	}
	
	public int getForwardeeId(int forwarderID) {
		return dbServer.getForwardeeId(forwarderID);
	}
	
	public int getLastCallRecipient(int callerId, int calleeId) {
		int currentId = calleeId, nextId;
		HashSet<Integer> accessed = new HashSet<>();
		accessed.add(callerId);
		if (!accessed.add(currentId))
			return -1;
		
		while ((nextId = getForwardeeId(currentId)) != 0) {
			currentId = nextId;
			if (!accessed.add(currentId))
				return -1;
		}
		return currentId;
	}
}