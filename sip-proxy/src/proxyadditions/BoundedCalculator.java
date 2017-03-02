package proxyadditions;

public class BoundedCalculator implements ChargeCalculator {
	private final double ratio = 4.0; 
	private final double cap = 240.0;
	private final double initialCost = 500.0;

	@Override
	public double calculate(CallProperties call) {
		long millis = call.endTime.getTime() - call.startTime.getTime();
		return Math.min(cap, (millis / 1000.0) * ratio);
	}

	@Override
	public double getInitialCost() {
		return initialCost;
	}
}
