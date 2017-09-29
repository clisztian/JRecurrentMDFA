package ch.imetrica.jmdfarecurrent.transformdata;

public class FirstDifference implements TransformData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double forward(double t0, double t1) {
		return t0 - t1;
	}
	

}
