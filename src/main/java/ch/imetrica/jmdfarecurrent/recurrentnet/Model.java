package ch.imetrica.jmdfarecurrent.recurrentnet;

import java.io.Serializable;
import java.util.List;


public interface Model extends Serializable {
	Matrix forward(Matrix input, Graph g) throws Exception;
	void resetState();
	List<Matrix> getParameters();
}

