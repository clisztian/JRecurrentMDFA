package ch.imetrica.jmdfarecurrent.recurrentnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RwaLayer implements Model {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Matrix s;
	Matrix Wgx, Wu, Wax;
	Matrix bgx, bu;
	
	Matrix Wgh, Wah;	
	Matrix hiddenContent;
	Matrix numerator;
	Matrix denominator;
	Matrix a_max;
	
	int nsteps = 0;
	int inputDimension;
	int outputDimension;
	
	Nonlinearity fActivation = new TanhUnit();
	Nonlinearity fExp = new ExpUnit();

	
	public RwaLayer(int inputDimension, int outputDimension, double initParamsStdDev, Random rng) {
		
		this.inputDimension = inputDimension;
		this.outputDimension = outputDimension;
		
		Wgx = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		Wax = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);
		Wu = Matrix.rand(outputDimension, inputDimension, initParamsStdDev, rng);	
		
		bgx = new Matrix(outputDimension);
		bu = new Matrix(outputDimension);		
		Wgh = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);
		Wah = Matrix.rand(outputDimension, outputDimension, initParamsStdDev, rng);	
		s = Matrix.rand(outputDimension, 1, 1.0, rng);
				
	}
	
	
	@Override
	public Matrix forward(Matrix input, Graph g) throws Exception {
		
		if(nsteps  == 0) {
			hiddenContent = g.nonlin(fActivation, s);
		}

		
		Matrix sum0 = g.mul(Wu, input);
		Matrix ux = g.add(sum0, bu);
	
		Matrix sum2 = g.mul(Wgx, input);
		Matrix sum3 = g.mul(Wgh, hiddenContent);
		Matrix gxh = g.nonlin(fActivation, g.add(g.add(sum2, sum3), bgx));
		
		Matrix sum4 = g.mul(Wax, input);
		Matrix sum5 = g.mul(Wah, hiddenContent);
		Matrix axh = g.add(sum4, sum5);
		
		Matrix z = g.elmul(ux, gxh);
		Matrix a_newmax = g.maximum(a_max, axh);
		
		Matrix exp_diff = g.nonlin(fExp, g.sub(a_max, a_newmax));
		Matrix exp_scaled = g.nonlin(fExp, g.sub(axh, a_newmax));
		
		Matrix outnum = g.add(g.elmul(numerator, exp_diff), g.elmul(z, exp_scaled));
		Matrix outdenom = g.add(g.elmul(denominator, exp_diff), exp_scaled);
		
		Matrix hnew = g.nonlin(fActivation, g.eldiv(outnum, outdenom));
		
		numerator = outnum; 
		denominator = outdenom;
		hiddenContent = hnew;
		a_max = a_newmax;
		
		nsteps++;
		
		return hnew;
	}

	@Override
	public void resetState() {
		
		nsteps = 0;
		hiddenContent = new Matrix(outputDimension);
		numerator = new Matrix(outputDimension);
		denominator = new Matrix(outputDimension);
		a_max = new Matrix(outputDimension);
	}

	@Override
	public List<Matrix> getParameters() {
		
		List<Matrix> result = new ArrayList<>();
		result.add(Wgx);
		result.add(Wu);
		result.add(Wax);
		result.add(Wgh);
		result.add(Wah);
		result.add(bgx);
		result.add(bu);
		result.add(s);

		return result;
	}

}
