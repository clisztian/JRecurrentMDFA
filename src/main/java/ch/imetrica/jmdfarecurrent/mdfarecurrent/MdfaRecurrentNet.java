package ch.imetrica.jmdfarecurrent.mdfarecurrent;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import com.opencsv.CSVReader;

import ch.imetrica.jmdfarecurrent.mdfadata.MdfaData;
import ch.imetrica.jmdfarecurrent.mdfasignal.MdfaSignal;
import ch.imetrica.jmdfarecurrent.recurrentnet.Model;
import ch.imetrica.jmdfarecurrent.recurrentnet.RecurrentNet;
import ch.imetrica.jmdfarecurrent.transformdata.BoxCox;


public class MdfaRecurrentNet {

	
	private int historicalLength;
	private CSVReader reader;
	
	private int trainSize;
	private int testSize;
	private int valSize;
	
	private double omegaFrequency;
	private double[] frequencies;
	private MdfaSignal mdfaSignal;
	private MdfaData mdfaData;
			
	private int number_series;
	
	private int hidden_layers = 1;
	private int hidden_dimension = 60;
	private double initParamsStdDev = 0.08;
	private double learningRate = 0.001;
	
	
	/**
	* Sets the number of Trials used in the stochastic gradient solution. This number should 
	* usually be at least 1000 to get reasonable estimates of the source location. 
	* Each trial generates a solution be starting a random
	* coordinate inside the bounds of a region where the source is thought to be located. This 
	* region is a square box about 200 km outside each coordinate in the navigation list. 
	* The default value is 5000.
	* 
	* @param  historicalLength Number of trials used in the stochastic gradient solution
	* @throws Exception must be greater than 100
	*/
	
	public MdfaRecurrentNet(int historicalLength) {
		this.historicalLength = historicalLength;
	}
	
	
	public void setFrequencies(double[] fw) {
		
		frequencies = new double[fw.length];
		for(int i = 0; i < fw.length; i++) {
			frequencies[i] = fw[i];
		}
		number_series = frequencies.length + 1; 
	}
	
	public void setOmegaFrequency(double omega) {
		this.omegaFrequency = omega;
	}
	
	
	public void importMdfaDataFromCsv(String fileName, double trainPercent) 
			throws Exception {
		
    	ArrayList<String> streamDates = new ArrayList<String>();
    	ArrayList<Double> streamPrice = new ArrayList<Double>();
    	String [] nextLine;
    	
    	reader = new CSVReader(new FileReader(fileName));
    	while ((nextLine = reader.readNext()) != null) {
       	
    	   streamDates.add(nextLine[0]);
    	   streamPrice.add(new Double(nextLine[1]));
    	}
    	reader.close();
		
    	int numberObservations = streamDates.size();
    	
    	trainSize = (int) (((double)numberObservations)*(trainPercent*.01)); 
    	testSize = (int) ((numberObservations - trainSize)/2.0);
    	valSize = testSize;  
    	
    	System.out.println(trainSize + " " + testSize + " " + valSize);
    	
    	mdfaSignal = new MdfaSignal(historicalLength, frequencies, new BoxCox());
        
    	for(int i = 0; i < numberObservations; i++) {    		
    		mdfaSignal.setHistoricalObservation(streamDates.get(i), streamPrice.get(i));   		
    	}
    	
    	mdfaSignal.computeHistoricalSignals();
    	mdfaSignal.computeSymmetricFilter(historicalLength, omegaFrequency);
    	mdfaSignal.computeSymmetricSignal();
		mdfaSignal.setSignalType(0); 
		
			
		mdfaData = new MdfaData(mdfaSignal, number_series, historicalLength, 
				trainSize, testSize, valSize);
		
		
	}
	
	
	
	
	
	public static void main(String[] args) throws Exception { 
		
		int L = 60;
		double trainPercent = 60.0;
		int hiddenDimension = 60;
		int hiddenLayers = 1;
		double learningRate = .001;
		double stdDev = .15;
		
		double omegaFrequency = Math.PI/5;
		double[] frequencies = new double[5];
    	
    	frequencies[0] = Math.PI/38.0; 
    	frequencies[1] = Math.PI/26.0;
    	frequencies[2] = Math.PI/18.0;
    	frequencies[3] = Math.PI/10.0;
    	frequencies[4] = Math.PI/5.0;
		
		
		MdfaRecurrentNet net = new MdfaRecurrentNet(L);
		
		net.setFrequencies(frequencies);
		net.setOmegaFrequency(omegaFrequency);
		
		net.importMdfaDataFromCsv("data/EEM.IB.dat", trainPercent);
		net.setNetworkTrainingParameters(hiddenDimension, hiddenLayers, learningRate, stdDev);
		net.train();
		
		
	}

    private void setNetworkTrainingParameters(int hiddenDimension, int hiddenLayers, double learningRate, double stdDev) {
    	
    	this.learningRate = learningRate;
    	this.hidden_dimension = hiddenDimension;
    	this.hidden_layers = hiddenLayers;
    	this.initParamsStdDev = stdDev; 
    }
	
	private void train() throws Exception {
		
		Random rng = new Random();
		
		Model nn = RecurrentNet.makeRwa( 
				mdfaData.inputDimension,
				hidden_dimension, hidden_layers, 
				mdfaData.outputDimension, mdfaData.getModelOutputUnitToUse(), 
				initParamsStdDev, rng);
		
		
		int reportEveryNthEpoch = 10;
		int trainingEpochs = 1000;
		
		
		Trainer.train(trainingEpochs, learningRate , nn, mdfaData, reportEveryNthEpoch, rng);
		

		System.out.println("done.");
	}
	
	

	
	
}
