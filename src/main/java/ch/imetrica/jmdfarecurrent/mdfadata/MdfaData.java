package ch.imetrica.jmdfarecurrent.mdfadata;

import java.util.ArrayList;
import java.util.Random;

import ch.imetrica.jmdfarecurrent.mdfasignal.MdfaSignal;
import ch.imetrica.jmdfarecurrent.observation.DataSequence;
import ch.imetrica.jmdfarecurrent.observation.DataSet;
import ch.imetrica.jmdfarecurrent.observation.DataStep;
import ch.imetrica.jmdfarecurrent.recurrentnet.LossSoftmax;
import ch.imetrica.jmdfarecurrent.recurrentnet.Model;
import ch.imetrica.jmdfarecurrent.recurrentnet.Nonlinearity;
import ch.imetrica.jmdfarecurrent.recurrentnet.SigmoidUnit;



public class MdfaData extends DataSet {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MdfaData(MdfaSignal mdfaSignal, int number_series, int historicalLength, 
			int trainSize, int testSize, int valSize) throws Exception {
		
		int numberObservations = mdfaSignal.getNumberObservations();
		
		inputDimension = number_series;
		outputDimension = 2;
		
		lossTraining = new LossSoftmax();
		lossReporting = new LossSoftmax();
		
		
		
		training = new ArrayList<>();
		for(int i = 0; i < trainSize-(historicalLength-1); i++) {
    	
		  DataSequence sequence = new DataSequence();
          
		  for(int t = 0; t < historicalLength-1; t++) {			
			DataStep observation = mdfaSignal.getStep(i + t, false);
			sequence.steps.add(observation);
          }
		  DataStep observation = mdfaSignal.getStep(i + (historicalLength-1), true);
	      sequence.steps.add(observation);  	
		
	      training.add(sequence);
		}
		
		testing = new ArrayList<>();
		for(int i = trainSize; i < trainSize+testSize-(historicalLength-1); i++) {
	    	
			  DataSequence sequence = new DataSequence();
	          
			  for(int t = 0; t < historicalLength-1; t++) {			
				DataStep observation = mdfaSignal.getStep(i + t, false);
				sequence.steps.add(observation);
	          }
			  DataStep observation = mdfaSignal.getStep(i + (historicalLength-1), true);
		      sequence.steps.add(observation);  	
			
		      testing.add(sequence);
		}
	
		boolean validationSetComplete = false;
		int validationStart = trainSize+testSize;
		
		validation = new ArrayList<>();
		for(int i = validationStart; i < numberObservations-(historicalLength-1); i++) {
	    				
			  DataSequence sequence = new DataSequence();
	          
			  for(int t = 0; t < historicalLength-1; t++) {
				
				if(mdfaSignal.getTargetSignal(i+t) == 0) {
					validationSetComplete = true;
					break;
				}
				DataStep observation = mdfaSignal.getStep(i + t, false);
				sequence.steps.add(observation);
	          }
			  
			  if(validationSetComplete) break;
			  
			  DataStep observation = mdfaSignal.getStep(i + (historicalLength-1), true);
		      sequence.steps.add(observation);  	
			
		      validation.add(sequence);
		}
		
		System.out.println(training.size() + ", " + testing.size() + ", " + validation.size());
	}
	


	@Override
	public void DisplayReport(Model model, Random rng) throws Exception {
		
		
	}

	@Override
	public Nonlinearity getModelOutputUnitToUse() {		
		return new SigmoidUnit();
	}
	
	

}
