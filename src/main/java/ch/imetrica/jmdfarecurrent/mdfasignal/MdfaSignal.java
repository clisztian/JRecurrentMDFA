package ch.imetrica.jmdfarecurrent.mdfasignal;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

import com.opencsv.CSVReader;

import ch.imetrica.jmdfarecurrent.timeseries.TimeSeries;
import ch.imetrica.jmdfarecurrent.transformdata.BoxCox;
import ch.imetrica.jmdfarecurrent.transformdata.TransformData;
import ch.imetrica.jmdfarecurrent.unitsignal.UnitSignal;
import lombok.Getter;
import net.finmath.timeseries.models.parametric.GARCH;

public class MdfaSignal {

	
	
	private TimeSeries<Double> MyTimeSeries;	
	private TimeSeries<Double> MyPriceSeries;
	private TimeSeries<Double> targetSignal;
	
	private TimeSeries<Double> h_t;
	private TimeSeries<Double> x_t;
	
	TransformData transform; 
	
	@Getter
	int NumberOfUnits;
	int MasterSignalVal;
	int FilterLength; 
	UnitSignal[] UnitSignals;
	
	private double CurrentMid;

	
	private int L1;
	private double Frequency;
	private double[] bc;
	
	
	private double garch_omega;
	private double garch_alpha;
	private double garch_beta;
	private double volScaling;
	
	
	public MdfaSignal(int num_signals, int L) {
		
		MyTimeSeries = new TimeSeries<Double>();
		MyPriceSeries = new TimeSeries<Double>();
		targetSignal = new TimeSeries<Double>();
		
		UnitSignals = new UnitSignal[num_signals];
		NumberOfUnits = num_signals; 
		FilterLength = L;
		
		for(int i = 0; i < num_signals; i++) {
			UnitSignals[i] = new UnitSignal(L);
		}
	}
	
    public MdfaSignal(int L, double[] freqs) {
		
		MyTimeSeries = new TimeSeries<Double>();
		MyPriceSeries = new TimeSeries<Double>();
		targetSignal = new TimeSeries<Double>();
		
		FilterLength = L;
		NumberOfUnits = freqs.length;
		UnitSignals = new UnitSignal[NumberOfUnits];
		
		
		for(int i = 0; i < NumberOfUnits; i++) {
			UnitSignals[i] = new UnitSignal(L);
			UnitSignals[i].computeSignalCoeffs(freqs[i]);
		}
	}
	
    public MdfaSignal(int L, double[] freqs, TransformData transform) {
		
		MyTimeSeries = new TimeSeries<Double>();
		MyPriceSeries = new TimeSeries<Double>();
		targetSignal = new TimeSeries<Double>();
		
		
		FilterLength = L;
		NumberOfUnits = freqs.length;
		UnitSignals = new UnitSignal[NumberOfUnits];
		this.transform = transform;
		
		for(int i = 0; i < NumberOfUnits; i++) {
			UnitSignals[i] = new UnitSignal(L);
			UnitSignals[i].computeSignalCoeffs(freqs[i]);
		}
	}
	
	public void setFrequencies(double[] freqs) {
		
		for(int i = 0; i < NumberOfUnits; i++) {
			UnitSignals[i].computeSignalCoeffs(freqs[i]);
		}		
	}
	
	public void setTransformData(TransformData transform) {
		this.transform = transform;
	}
	
	
	
	public void setHistoricalObservation(String time, double val) {
		
		if(MyPriceSeries.size() > 1) {
		
			MyTimeSeries.add(time, transform.forward(val, MyPriceSeries.last().getValue().doubleValue()));
			MyPriceSeries.add(time, val);
		}
		else {
			
			MyPriceSeries.add(time, val);
			MyTimeSeries.add(time, 0.0);
		}
	}
	
	
	public void setHistoricalData(String[] dates, double[] data) {
		
		MyPriceSeries = new TimeSeries<Double>();
		MyTimeSeries = new TimeSeries<Double>();
		
		for(int i = 0; i < dates.length; i++) {
			
			MyPriceSeries.add(dates[i], data[i]);
		}
		
		MyTimeSeries.add(dates[0], 0.0);
        for(int i = 1; i < dates.length; i++) {
			
			MyTimeSeries.add(dates[i], transform.forward(data[i], data[i-1]));
		}
		
        computeHistoricalSignals();
	}
	
	
	
	public void computeHistoricalSignals() {
		
        int N = MyTimeSeries.size();
		double[] sums = new double[NumberOfUnits];
        
		for(int i = 0; i < NumberOfUnits; i++) {
			
          for(int t = UnitSignals[i].L - 1; t < N; t++) {
        			
        	sums[i] = 0;
			for (int l = 0; l < UnitSignals[i].L; l++) {
	          sums[i] = sums[i] + UnitSignals[i].getCoeff(l)*(MyTimeSeries.get(t - l).getValue());
	        }			
			UnitSignals[i].addEntry(MyTimeSeries.get(t).getDateTime(), sums[i]);		  
          }
	    }	
	}
	

	public void computeSymmetricFilter(int L1, double frequency) throws Exception {
		
		if(MyTimeSeries.size() < 2*L1 + 1) {
			throw new Exception("Need at least 2*L1 data points");
		}
		
		int i,l;
		this.L1 = L1; 
		this.Frequency = frequency; 
		
		double cutoff = frequency;
		double sum = 0; 
		bc = new double[L1+1];
		bc[0] = cutoff/Math.PI; 
		sum = bc[0];
		
		for(i=1;i<=L1;i++) {
			bc[i] = (1.0/Math.PI)*Math.sin(cutoff*i)/(double)i; 
			sum = sum + bc[i];
		} 
		sum = sum+(sum-bc[0]);
		for(i=0;i<=L1;i++) {
			bc[i] = bc[i]/sum;
	    }
	}

	public void computeSymmetricSignal() {
	
		int N = MyTimeSeries.size();
		double sum = 0; 
		
		for(int i = L1-1; i < N - L1; i++) {
			
			sum = 0.0;
			for(int l=0;l<L1;l++) {
		    	sum = sum + bc[l]*MyTimeSeries.get(i+l).getValue();
		    } 
		    for(int l=1;l<L1;l++) {
		    	sum = sum + bc[l]*MyTimeSeries.get(i-l).getValue();
		    }
		    targetSignal.add(MyTimeSeries.get(i).getDateTime(), sum);	
		}
		for(int i = N - L1; i < N; i++) {
			targetSignal.add(MyTimeSeries.get(i).getDateTime(), 0.0);
		}		
	}
		
	    
	  
	
	
	
	public void addObservation(String time, double bid, double ask) throws Exception {

		double Xt = 0;		       
		CurrentMid = (ask + bid)/2.0;
		
		Xt = transform.forward(CurrentMid, MyPriceSeries.last().getValue());		
		MyPriceSeries.add(time, CurrentMid);
		MyTimeSeries.add(time, Xt);
		
		int N = MyTimeSeries.size();
		double[] sums = new double[NumberOfUnits];
		
		for(int i = 0; i < NumberOfUnits; i++) {		
			for (int l = 0; l < UnitSignals[i].L; l++) {
	          sums[i] = sums[i] + UnitSignals[i].getCoeff(l)*(MyTimeSeries.get(N - l - 1).getValue());
	        }			
			UnitSignals[i].addEntry(time, sums[i]);
		}
		
		if(targetSignal != null) {
			
			targetSignal.add(MyTimeSeries.last().getDateTime(), 0.0);
			
			int i = targetSignal.size() - L1; 
			double sum = 0.0;
			for(int l=0;l<L1;l++) {
		    	sum = sum + bc[l]*MyTimeSeries.get(i+l).getValue();
		    } 
		    for(int l=1;l<L1;l++) {
		    	sum = sum + bc[l]*MyTimeSeries.get(i-l).getValue();
		    }
		    targetSignal.set(i, MyTimeSeries.get(i).getDateTime(), sum);	   
		}
		
		if(h_t != null) {
			
			double xt = MyPriceSeries.last().getValue(); 
		    double xt1 = MyPriceSeries.get(MyPriceSeries.size()-2).getValue();
			
		    String prevDate = MyPriceSeries.get(MyPriceSeries.size()-2).getDateTime();
		    String date = MyPriceSeries.last().getDateTime();
		    double eval	= volScaling * Math.log(xt/xt1);
				
		    if(prevDate.equals(h_t.last().getDateTime())) {
		    	
		    	double vol = h_t.last().getValue();
		    	
		    	x_t.add(date, Math.log(xt/xt1)/vol);
		    	
		    	double h = garch_omega + garch_alpha * eval * eval + garch_beta * vol*vol/volScaling;
		    	
		    	vol = Math.sqrt(h) * volScaling;
		        h_t.add(date, vol);
		    }
		    else {
		    	throw new Exception("Dates don't align in the Price and h_t estimates");
		    }	
		}		
	}
	

	public double[] getLatestStep() throws Exception {
		
		double[] values = new double[NumberOfUnits + 2];	
		String lastDate = MyPriceSeries.last().getDateTime();
		
		values[0] = MyPriceSeries.last().getValue();
		values[1] = MyTimeSeries.last().getValue();
		
		for(int i = 0; i < NumberOfUnits; i++) {
		  if(lastDate.equals(UnitSignals[i].getDateTime())) {
			  values[i+2] = UnitSignals[i].getValue();
		  }
		  else {
			  throw new Exception("Latest date in UnitSignal " + i + " don't match series");
		  }			
		}		
		return values;
	}
	
	
    public String getCurrentDateTime() {
    	return MyPriceSeries.last().getDateTime();
    }
	
	
    
    public static void main(String[] args) throws Exception {
    	
    	
    	CSVReader reader = new CSVReader(new FileReader("data/EEM.IB.dat"));
    	String [] nextLine;
    	   	
    	ArrayList<String> streamDates = new ArrayList<String>();
    	ArrayList<Double> streamPrice = new ArrayList<Double>();
    	
    	while ((nextLine = reader.readNext()) != null) {
       	
    	   streamDates.add(nextLine[0]);
    	   streamPrice.add(new Double(nextLine[1]));
    	}
    	reader.close();
    	
    	
    	int L = 60; 
    	int L1 = L;
    	double frequency = Math.PI/30.0;
    	double[] frequencies = new double[5];
    	
    	
    	frequencies[0] = Math.PI/30.0; 
    	frequencies[1] = Math.PI/22.0;
    	frequencies[2] = Math.PI/16.0;
    	frequencies[3] = Math.PI/10.0;
    	frequencies[4] = Math.PI/5.0;
    	
    	
    	MdfaSignal eurusd = new MdfaSignal(L, frequencies, new BoxCox());
        
    	for(int i = 0; i < 900; i++) {    		
    		eurusd.setHistoricalObservation(streamDates.get(i), streamPrice.get(i));   		
    	}
    	eurusd.computeHistoricalSignals();
    	eurusd.computeSymmetricFilter(L1, frequency);
    	eurusd.computeSymmetricSignal();
    	eurusd.estimateGARCHModel();
    	eurusd.estimateSzenarios(1.0);
    	
    	for(int i = 900; i < 1000; i++) {     	
    		eurusd.addObservation(streamDates.get(i), streamPrice.get(i), streamPrice.get(i));   		
    	}
    	    	
    	eurusd.printSignals(400); 
    	
    		
    }

	private void printSignals() {
		

		int N = UnitSignals[0].getLength();
	    int start = MyTimeSeries.size() - N;
		
		for(int i = 0; i < N; i++) {
	    	
		  System.out.print(MyTimeSeries.get(start + i).getDateTime() + ", " + 
		    MyTimeSeries.get(start + i).getValue() + ", ");
		  
		  for(int j = 0; j < 1; j++) {
			  System.out.print(UnitSignals[j].getValue(i) + ", ");			  
		  }
		  //System.out.println(UnitSignals[NumberOfUnits-1].getValue(i));		
		  System.out.println(targetSignal.get(i).getValue());	
	    }			
	}
    
	private void printSignals(int N) {
		

		for(int i = 0; i < N; i++) {
	    	
		  System.out.print(MyTimeSeries.get(MyTimeSeries.size() - 1 - i).getDateTime() + ", " + 
				  MyTimeSeries.get(MyTimeSeries.size() - 1 - i).getValue() + ", ");
		  
		  for(int j = 0; j < 1; j++) {
			  System.out.print(UnitSignals[j].getValue(UnitSignals[j].getSize() - 1 - i) + ", ");			  
		  }
		  //System.out.println(UnitSignals[NumberOfUnits-1].getValue(i));		
		  System.out.print(targetSignal.get(targetSignal.size() - 1 - i).getValue() + ", ");
		  System.out.println(h_t.get(h_t.size() - 1 - i).getValue());
	    }			
	}
	
	
	public void estimateGARCHModel() {
		
		double[] values = MyPriceSeries.getValues();
		GARCH garchEstimate = new GARCH(values);
		Map<String, Object> params = garchEstimate.getBestParameters();
	
		garch_omega = (double) params.get("Omega");
		garch_alpha = (double) params.get("Alpha");
		garch_beta = (double) params.get("Beta");
		
		
		System.out.println(params.toString());
 
		
	}
	
	
	public void estimateSzenarios(double volScaling) throws Exception {
		
		if(garch_alpha == 0) {
			throw new Exception("Need to estimage GARCH(1,1) model first");
		}
		
		x_t = new TimeSeries<Double>();
		h_t = new TimeSeries<Double>();
		
		this.volScaling = volScaling; 
		int windowIndexEnd = MyPriceSeries.size()-1;		

		double h = garch_omega / (1.0 - garch_alpha - garch_beta);
		double vol = Math.sqrt(h) * volScaling;
		
		for (int i = 1; i <= windowIndexEnd; i++) {
			
			double xt = MyPriceSeries.get(i).getValue(); 
			double xt1 = MyPriceSeries.get(i-1).getValue();
			String date = MyPriceSeries.get(i).getDateTime();
			double eval	= volScaling * Math.log((xt)/(xt1));
			
			x_t.add(date, Math.log(xt/xt1)/vol);						
	        h = garch_omega + garch_alpha * eval * eval + garch_beta * h;
	    	        
	        vol = Math.sqrt(h) * volScaling;
	        h_t.add(date, vol);
		}

	}
	
	
	

//	double[] step = eurusd.getLatestStep();
//	
//	for(int j = 0; j < step.length; j++) {
//		System.out.print(step[j] + " ");
//	}
//	System.out.println("");
    
    
    
	
}
