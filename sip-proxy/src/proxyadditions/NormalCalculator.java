package proxyadditions;

public class NormalCalculator implements ChargeCalculator {
	private final double ratio = 4.0;
	private final double initialCost = 100.0;

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
