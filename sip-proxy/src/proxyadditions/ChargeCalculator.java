package proxyadditions;

public interface ChargeCalculator {
	
	public double getInitialCost();
	public double calculate(CallProperties call);
}
