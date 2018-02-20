package ch.imetrica.jmdfarecurrent.observation;

import java.io.Serializable;

import ch.imetrica.jmdfarecurrent.recurrentnet.Matrix;

public class DataStep implements Serializable {

	private static final long serialVersionUID = 1L;
	public Matrix input = null;
	public Matrix targetOutput = null;
	
	
	public DataStep(double[] input, double[] targetOutput) {
		this.input = new Matrix(input);
		if (targetOutput != null) {
			this.targetOutput = new Matrix(targetOutput);
		}
	}

	
	public DataStep(double[] input, double[] targetOutput, int n, int batchsize) throws Exception {
		this.input = new Matrix(input, n, batchsize);
		if (targetOutput != null) {
			this.targetOutput = new Matrix(targetOutput, n, batchsize);
		}
	}
		
	@Override
	public String toString() {
		String result = "";
		for (int i = 0; i < input.w.length; i++) {
			result += String.format("%.5f", input.w[i]) + "\t";
		}
		result += "\t->\t";
		if (targetOutput != null) {
			for (int i = 0; i < targetOutput.w.length; i++) {
				result += String.format("%.5f", targetOutput.w[i]) + "\t";
			}
		}
		else {
			result += "___\t";
		}
		return result;
	}
}

