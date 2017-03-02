package proxyadditions;

public class PremiumCalculator implements ChargeCalculator {
	private final double ratio = 2.0; 
	private final double initialCost = 500.0;

	@Override
	public double calculate(CallProperties call) {
		long millis = call.endTime.getTime() - call.startTime.getTime();
		return (millis / 1000.0) * ratio;
	}

	@Override
	public double getInitialCost() {
		return initialCost;
	}
}
