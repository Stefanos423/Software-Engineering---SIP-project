package proxyadditions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimerServer {
	
	private static TimerServer instance = null;
	private DatabaseServer dbServer;

	private TimerServer() {
		dbServer = DatabaseServer.get();
	}

	public static TimerServer get() {
		if (instance == null)
			instance = new TimerServer(); 
		return instance;
	}

	public void start(int callerID, int calleeID) {
		dbServer.startCall(callerID, calleeID);
	}
	
	public void stop(int callId) {		
		Date date = new Date();
		Timestamp stop = new Timestamp(date.getTime());
		dbServer.endCall(callId, stop);
		BillingServer.get().calculateCost(callId);
	}
	
	public void stopAllUserCalls(String username) {
		int caller = dbServer.getUserId(username);
		ArrayList<Integer> list = dbServer.getAllCallId(caller);
		for (int call: list) {
			if (call < 0) call = -call;
			if (call != 0) stop(call);
		}
	}
	
	public List<CallProperties> getCallsList(int userId)
	{		
		return dbServer.getCallPropertiesList(userId);
	}
}