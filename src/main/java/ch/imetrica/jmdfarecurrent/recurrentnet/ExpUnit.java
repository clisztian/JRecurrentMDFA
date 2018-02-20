package ch.imetrica.jmdfarecurrent.recurrentnet;


public class ExpUnit implements Nonlinearity {

	private static final long serialVersionUID = 1L;

	@Override
	public double forward(double x) {
		return Math.exp(x);
	}

	@Override
	public double backward(double x) {
		return Math.exp(x);
	}
}
