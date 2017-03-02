package proxyadditions;

import java.sql.Timestamp;

public class CallProperties {
	public final int callId;
	public final int callerId;
	public final int calleeId;
	public final String callerName;
	public final String calleeName;
	public final Timestamp startTime;
	public final Timestamp endTime;
	public final double charge;

	public CallProperties(int callId, int callerId, int calleeId,
			Timestamp startTime, Timestamp endTime, double charge) {
		this(callId, callerId, calleeId,
				null, null, startTime, endTime, charge);
	}
	
	public CallProperties(int callId, int callerId, int calleeId,
			String callerName, String calleeName,
			Timestamp startTime, Timestamp endTime, double charge) {
		this.callId = callId;
		this.callerId = callerId;
		this.calleeId = calleeId;
		this.callerName = callerName;
		this.calleeName = calleeName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.charge = charge;
	}

	@Override
	public String toString() {
		return String.format("%d %d %d %s %s %d %d %d",
				callId, callerId, calleeId,
				(callerName == null ? "null" : callerName),
				(calleeName == null ? "null" : calleeName),
				startTime.getTime(), endTime.getTime(), (int)charge);
	}
}
