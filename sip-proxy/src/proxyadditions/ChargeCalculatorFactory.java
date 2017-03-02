package proxyadditions;

public class ChargeCalculatorFactory {
	public static final int NORMAL_RATE = 0;
	public static final int PREMIUM_RATE = 1;
	public static final int BOUNDED_RATE = 2;
	public static final int TZAPA_RATE = 3;
	
	private ChargeCalculatorFactory() {
		/* Class is Static! */
	}

	public static ChargeCalculator create(int rate) {
		switch (rate) {
		case PREMIUM_RATE:
			return new PremiumCalculator();
		case BOUNDED_RATE:
			return new BoundedCalculator();
		case TZAPA_RATE:
			return new TzapaCalculator();
		default: // if the rate somehow becomes an invalid value
			// you should be charged normally!!
			return new NormalCalculator();
		}
	}
	
	
}
