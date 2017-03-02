package proxyadditions;

public class BillingServer {

	private static BillingServer instance = null;
	private static DatabaseServer dbServer;

	private BillingServer() {
		BillingServer.dbServer = DatabaseServer.get();
	}

	public static BillingServer get() {
		if (instance == null)
			instance = new BillingServer();
		return instance;
	}

	private void charge(int callerId, double charge) {
		dbServer.updateUserBalance(callerId, charge);
	}
	
	private ChargeCalculator getChargeCalculator(int callerId) {
		int rate = dbServer.getUserRate(callerId);
		return ChargeCalculatorFactory.create(rate);
	}

	public void calculateCost(int callId) {
		int callerId = dbServer.getCallerId(callId);
		CallProperties callProp = dbServer.getCallProperties(callId);
		
		double cost = getChargeCalculator(callerId).calculate(callProp);
		dbServer.updateCallCharge(callId, cost);
		charge(callerId, cost);
	}
}