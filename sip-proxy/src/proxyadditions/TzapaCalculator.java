package proxyadditions;

public class TzapaCalculator implements ChargeCalculator {
	private final double initialCost = 10000.0;
	
	@Override
	public double calculate(CallProperties call) {
		return 0.0;
	}

	@Override
	public double getInitialCost() {
		return initialCost;
	}
}
