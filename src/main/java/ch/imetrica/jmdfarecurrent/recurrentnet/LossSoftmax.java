package ch.imetrica.jmdfarecurrent.recurrentnet;

import java.util.ArrayList;
import java.util.List;

import ch.imetrica.jmdfarecurrent.observation.DataSequence;
import ch.imetrica.jmdfarecurrent.observation.DataStep;
import ch.imetrica.jmdfarecurrent.util.Util;

public class LossSoftmax implements Loss {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void backward(Matrix logprobs, Matrix targetOutput) throws Exception {
		
	  for(int j = 0; j < targetOutput.cols; j++) {
		
		int targetIndex = getTargetIndex(targetOutput.returnColumnW(j));
		Matrix probs = getSoftmaxProbs(logprobs.returnColumnW(j), 1.0);
		
		for (int i = 0; i < probs.w.length; i++) {
			logprobs.dw[logprobs.rows*j + i] = probs.w[i];
		}
		logprobs.dw[logprobs.rows*j + targetIndex] -= 1;
	  }
	}

	@Override
	public double measure(Matrix logprobs, Matrix targetOutput) throws Exception {
	  
	  double cost = 0;
	  	  
	  for(int j = 0; j < targetOutput.cols; j++) {
				  
		int targetIndex = getTargetIndex(targetOutput.returnColumnW(j));
		Matrix probs = getSoftmaxProbs(logprobs.returnColumnW(j), 1.0);
		
		cost += -Math.log(probs.w[targetIndex]);
		
	  }
	  return cost/targetOutput.cols;
	}

	public static double calculateMedianPerplexity(Model model, List<DataSequence> sequences) throws Exception {
		double temperature = 1.0;
		List<Double> ppls = new ArrayList<>();
		for (DataSequence seq : sequences) {
			double n = 0;
			double neglog2ppl = 0;
			
			Graph g = new Graph(false);
			model.resetState();
			for (DataStep step : seq.steps) {
				Matrix logprobs = model.forward(step.input, g);
				Matrix probs = getSoftmaxProbs(logprobs, temperature);
				int targetIndex = getTargetIndex(step.targetOutput);
				double probOfCorrect = probs.w[targetIndex];
				double log2prob = Math.log(probOfCorrect)/Math.log(2); //change-of-base
				neglog2ppl += -log2prob;
				n += 1;
			}
			
			n -= 1; //don't count first symbol of sentence
			double ppl = Math.pow(2, (neglog2ppl/(n-1)));
			ppls.add(ppl);
		}
		return Util.median(ppls);
	}
	
	public static Matrix getSoftmaxProbs(Matrix logprobs, double temperature) throws Exception {	
		Matrix probs = new Matrix(logprobs.w.length);
		if (temperature != 1.0) {
			for (int i = 0; i < logprobs.w.length; i++) {
				logprobs.w[i] /= temperature;
			}
		}
		double maxval = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < logprobs.w.length; i++) {
			if (logprobs.w[i] > maxval) {
				maxval = logprobs.w[i];
			}
		}
		double sum = 0;
		for (int i = 0; i < logprobs.w.length; i++) {
			probs.w[i] = Math.exp(logprobs.w[i] - maxval); //all inputs to exp() are non-positive
			sum += probs.w[i];
		}
		for (int i = 0; i < probs.w.length; i++) {
			probs.w[i] /= sum;
		}
		return probs;
	}

	
	public static Matrix getSoftmaxProbs(double[] logprobs, double temperature) throws Exception {	
		
		Matrix probs = new Matrix(logprobs.length);
		if (temperature != 1.0) {
			for (int i = 0; i < logprobs.length; i++) {
				logprobs[i] /= temperature;
			}
		}
		double maxval = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < logprobs.length; i++) {
			if (logprobs[i] > maxval) {
				maxval = logprobs[i];
			}
		}
		double sum = 0;
		for (int i = 0; i < logprobs.length; i++) {
			probs.w[i] = Math.exp(logprobs[i] - maxval); //all inputs to exp() are non-positive
			sum += probs.w[i];
		}
		for (int i = 0; i < probs.w.length; i++) {
			probs.w[i] /= sum;
		}
		return probs;
	}	
	
	
	
	private static int getTargetIndex(Matrix targetOutput) throws Exception {
		for (int i = 0; i < targetOutput.w.length; i++) {
			if (targetOutput.w[i] == 1.0) {
				return i;
			}
		}
		throw new Exception("no target index selected");
	}
	
	private static int getTargetIndex(double[] targetOutput) throws Exception {
		for (int i = 0; i < targetOutput.length; i++) {
			if (targetOutput[i] == 1.0) {
				return i;
			}
		}
		throw new Exception("no target index selected");
	}
	
}
