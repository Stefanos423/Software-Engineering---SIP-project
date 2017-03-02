package proxyadditions;

import java.util.List;

public class BlockingServer {

	private static BlockingServer instance = null;
	private final DatabaseServer dbServer;

	private BlockingServer() {
		dbServer = DatabaseServer.get();
	}

	public static BlockingServer get() {
		if (instance == null)
			instance = new BlockingServer();
		return instance;
	}

	public boolean isBlocked(int callerId, int calleeId) {
		return dbServer.isBlocked(callerId, calleeId);
	}

	public boolean block(int blockerId, int blockedId) {
		return dbServer.addBlocking(blockerId, blockedId);
	}

	public void unblock(int blockerId, int blockedId) {
		dbServer.removeBlocking(blockerId, blockedId);
	}

	public List<String> getBlockedUsers(int userID) {
		return dbServer.getBlockedUsers(userID);
	}

	public void unblockAll(int blockerId) {
		dbServer.removeBlocking(blockerId);
	}
}