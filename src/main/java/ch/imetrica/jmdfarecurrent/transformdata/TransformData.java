package ch.imetrica.jmdfarecurrent.transformdata;

import java.io.Serializable;


public interface TransformData extends Serializable {
	double forward(double t0, double t1);
}
