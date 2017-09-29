package ch.imetrica.jmdfarecurrent.unitsignal;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.imetrica.jmdfarecurrent.timeseries.TimeSeries;
import lombok.Getter;

public class UnitSignal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	TimeSeries<Double> MyUnitSignal;
	

	@Getter
	private double[] bcoeffs = null;
	@Getter
	public int L;

	DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	
	private double CurrentSignal = 0;
	private double PreviousSignal = 0;

	public UnitSignal(int L) {
		
		MyUnitSignal = new TimeSeries<Double>();
		this.L = L;
	}
	
	public int getSize() { 
		return MyUnitSignal.size();
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

	public void computeSignalCoeffs(double freq) {
		
	    int i; 
	    double sum;
	    double cutoff2 = freq;
	    bcoeffs = new double[L]; 
	    
	    bcoeffs[0] = cutoff2/Math.PI; sum= bcoeffs[0];
	    
	    for(i=1;i<L;i++) {
	    	bcoeffs[i] = (1/Math.PI)*Math.sin(cutoff2*i)/((double)i); sum = sum+bcoeffs[i];
	    }     	    
	}


	public double getCoeff(int l) {		
		return bcoeffs[l];
	}


	public void addEntry(String time, double d) {
		
		PreviousSignal = CurrentSignal;
		MyUnitSignal.add(time, d);
		CurrentSignal = d;
	}


	public double getValue() {
		return MyUnitSignal.last().getValue();
	}
	
	public double getValue(int i) {
		return MyUnitSignal.get(i).getValue();
	}
	
	public String getDateTime() {
		return MyUnitSignal.last().getDateTime();
	}
	
	public String getDateTime(int i) {
		return MyUnitSignal.get(i).getDateTime();
	}

	public int getLength() {		
		return MyUnitSignal.size();
	}	
	


	

}
