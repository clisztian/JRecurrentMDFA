package ch.imetrica.jmdfarecurrent.recurrentnet;

import java.util.ArrayList;
import java.util.List;




public class Graph {
	boolean applyBackprop;
	
	List<Runnable> backprop = new ArrayList<>();
	
	public Graph() {
		this.applyBackprop = true;
	}
	
	public Graph(boolean applyBackprop) {
		this.applyBackprop = applyBackprop;
	}
	
	public void backward() {
		for (int i = backprop.size()-1; i >= 0; i--) {
			backprop.get(i).run();
		}
	}
	
	public Matrix concatVectors(final Matrix m1, final Matrix m2) throws Exception {
		if (m1.cols > 1 || m2.cols > 1) {
			throw new Exception("Expected column vectors");
		}
		final Matrix out = new Matrix(m1.rows + m2.rows);
		int loc = 0;
		for (int i = 0; i < m1.w.length; i++) {
			out.w[loc] = m1.w[i];
			out.dw[loc] = m1.dw[i];
			out.stepCache[loc] = m1.stepCache[i];
			loc++;
		}
		for (int i = 0; i < m2.w.length; i++) {
			out.w[loc] = m2.w[i];
			out.dw[loc] = m2.dw[i];
			out.stepCache[loc] = m2.stepCache[i];
			loc++;
		}
		if (this.applyBackprop) {
			Runnable bp = new Runnable() {
				public void run() {
					int loc = 0;
					for (int i = 0; i < m1.w.length; i++) {
						m1.w[i] = out.w[loc];
						m1.dw[i] = out.dw[loc];
						m1.stepCache[i] = out.stepCache[loc];
						loc++;
					}
					for (int i = 0; i < m2.w.length; i++) {
						m2.w[i] = out.w[loc];
						m2.dw[i] = out.dw[loc];
						m2.stepCache[i] = out.stepCache[loc];
						loc++;
					}
				}
			};
			backprop.add(bp);
		}
		return out;
	}
	
	public Matrix nonlin(final Nonlinearity neuron, final Matrix m) throws Exception {
		final Matrix out = new Matrix(m.rows, m.cols);
		final int n = m.w.length;
		for (int i = 0; i < n; i++) {
			out.w[i] = neuron.forward(m.w[i]);
		}
		if (this.applyBackprop) {
			Runnable bp = new Runnable() {
				public void run() {
					for (int i = 0; i < n; i++) {
						m.dw[i] += neuron.backward(m.w[i]) * out.dw[i];
					}
				}
			};
			backprop.add(bp);
		}
		return out;
	}
	
	public Matrix mul(final Matrix m1, final Matrix m2) throws Exception {
		if (m1.cols != m2.rows) {
			throw new Exception("matrix dimension mismatch");
		}
		
		final int m1rows = m1.rows;
		final int m1cols = m1.cols;
		final int m2cols = m2.cols;
		final Matrix out = new Matrix(m1rows, m2cols);
		final int outcols = m2cols;
		for (int i = 0; i < m1rows; i++) {
			int m1col = m1cols*i;
			for (int j = 0; j < m2cols; j++) {
				double dot = 0;
				for (int k = 0; k < m1cols; k++) {
					dot +=  m1.w[m1col + k] * m2.w[m2cols*k + j];
				}
				out.w[outcols*i + j] = dot;
			}
		}
		if (this.applyBackprop) {
			Runnable bp = new Runnable() {
				public void run() {
					for (int i = 0; i < m1.rows; i++) {
						int outcol = outcols*i;
						for (int j = 0; j < m2.cols; j++) {
							double b = out.dw[outcol + j];
							for (int k = 0; k < m1.cols; k++) {
								m1.dw[m1cols*i + k] += m2.w[m2cols*k + j] * b;
								m2.dw[m2cols*k + j] += m1.w[m1cols*i + k] * b;
							}
						}
					}
//					printArray(m1.w);
//					printArray(m2.w);
//					printArray(out.dw);
//					printArray(m1.dw);
//					printArray(m2.dw);
//					System.out.println("");
					
					
				}
			};
			backprop.add(bp);
		}
		return out;
	}
	
	public Matrix add(final Matrix m1, final Matrix m2) throws Exception {
		if (m1.rows != m2.rows || m1.cols != m2.cols) {
			throw new Exception("matrix dimension mismatch");
		}
		final Matrix out = new Matrix(m1.rows, m1.cols);
		for (int i = 0; i < m1.w.length; i++) {
			out.w[i] = m1.w[i] + m2.w[i];
		}
		if (this.applyBackprop) {
			Runnable bp = new Runnable() {
				public void run() {
					for (int i = 0; i < m1.w.length; i++) {
						m1.dw[i] += out.dw[i];
						m2.dw[i] += out.dw[i];
					}
				}
			};
			backprop.add(bp);
		}
		return out;
	}
	
	public Matrix addbatch(final Matrix m1, final Matrix m2) throws Exception {
		if (m1.rows != m2.rows) {
			throw new Exception("matrix dimension mismatch");
		}

		final Matrix out = new Matrix(m1.rows, m1.cols);
		for (int i = 0; i < m1.cols; i++) {
			for(int j = 0; j < m1.rows; j++) {
				out.w[j*m1.cols + i] = m1.w[j*m1.cols + i] + m2.w[j];
			}
		}
		if (this.applyBackprop) {
			Runnable bp = new Runnable() {
				public void run() {
					
					for (int i = 0; i < out.cols; i++) {
						for(int j = 0; j < out.rows; j++) {
							m1.dw[j*m1.cols + i] += out.dw[j*m1.cols + i];
							m2.dw[j] += out.dw[j*m1.cols + i]/out.cols;
						}
					}
				}
			};
			backprop.add(bp);
		}
		return out;
	}
	
	
	public void printArray(double[] me) 
	{
		for(int i = 0; i < me.length; i++) System.out.print(me[i] + ", ");
		System.out.println();
	}
	
	public Matrix oneMinus(final Matrix m) throws Exception {
		Matrix ones = Matrix.ones(m.rows, m.cols);
		Matrix out = sub(ones, m);
		return out;
	}
	
	public Matrix sub(final Matrix m1, final Matrix m2) throws Exception {
		Matrix out = add(m1, neg(m2));
		return out;
	}
	
	public Matrix smul(final Matrix m, final double s) throws Exception {
		Matrix m2 = Matrix.uniform(m.rows, m.cols, s);
		Matrix out = elmul(m, m2);
		return out;
	}
	
	public Matrix smul(final double s, final Matrix m) throws Exception {
		Matrix out = smul(m, s);
		return out;
	}
	
	public Matrix neg(final Matrix m) throws Exception {
		Matrix negones = Matrix.negones(m.rows, m.cols);
		Matrix out = elmul(negones, m);
		return out;
	}
	
	public Matrix elmul(final Matrix m1, final Matrix m2) throws Exception {
		if (m1.rows != m2.rows || m1.cols != m2.cols) {
			throw new Exception("matrix dimension mismatch");
		}
		final Matrix out = new Matrix(m1.rows, m1.cols);
		for (int i = 0; i < m1.w.length; i++) {
			out.w[i] = m1.w[i] * m2.w[i];
		}
		if (this.applyBackprop) {
			Runnable bp = new Runnable() {
				public void run() {
					for (int i = 0; i < m1.w.length; i++) {
						m1.dw[i] += m2.w[i] * out.dw[i];
						m2.dw[i] += m1.w[i] * out.dw[i];
					}
				}
			};
			backprop.add(bp);
		}
		return out;
	}

	
	public Matrix maximum(final Matrix m1, final Matrix m2) throws Exception {
		
		if (m1.rows != m2.rows || m1.cols != m2.cols) {
			throw new Exception("matrix dimension mismatch");
		}
		
		Matrix diff = sub(m1,m2);
		Matrix mask = greaterThan(diff, 0);
		Matrix oneM = oneMinus(mask);
		
		final Matrix out = add(elmul(m1, mask), elmul(m2, oneM));
		return out;

	}

	
	public Matrix greaterThan(final Matrix m, double v)
	{
		Matrix out = new Matrix(m.rows, m.cols);
		
		for(int i = 0; i < m.w.length; i++)
		{
			if(m.w[i] > v) {out.w[i] = 1;}
			else out.w[i] = 0;
		}
		
		return out;
		
	}

	public Matrix eldiv(final Matrix m1, final Matrix m2) throws Exception {
		if (m1.rows != m2.rows || m1.cols != m2.cols) {
			throw new Exception("matrix dimension mismatch");
		}
		final Matrix out = new Matrix(m1.rows, m1.cols);
		for (int i = 0; i < m1.w.length; i++) {
			out.w[i] = m1.w[i] / m2.w[i];
		}
		if (this.applyBackprop) {
			Runnable bp = new Runnable() {
				public void run() {
					for (int i = 0; i < m1.w.length; i++) {
						m1.dw[i] += out.dw[i]/m2.w[i]; 
						m2.dw[i] += m1.w[i] * out.dw[i]/(m2.w[i] * m2.w[i]); 
					}
				}
			};
			backprop.add(bp);
		}
		return out;
	}
	
	
}
