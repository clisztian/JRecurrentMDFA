package ch.imetrica.jmdfarecurrent.recurrentnet;

import java.io.Serializable;

public interface Loss extends Serializable {
	void backward(Matrix actualOutput, Matrix targetOutput) throws Exception;
	double measure(Matrix actualOutput, Matrix targetOutput) throws Exception;
}