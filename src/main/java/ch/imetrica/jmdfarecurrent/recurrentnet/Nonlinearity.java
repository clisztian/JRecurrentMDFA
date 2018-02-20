package ch.imetrica.jmdfarecurrent.recurrentnet;

import java.io.Serializable;


public interface Nonlinearity extends Serializable {
	double forward(double x);
	double backward(double x);
}
