package ch.imetrica.jmdfarecurrent.transformdata;

public class BoxCox implements TransformData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double forward(double t0, double t1) {
		return Math.log(t0) - Math.log(t1);
	}
	

}
