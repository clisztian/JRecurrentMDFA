package ch.imetrica.jmdfarecurrent.mdfasignal;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import ch.algotrader.entity.Position;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.simulation.Simulator;
import ch.algotrader.simulation.SimulatorImpl;
import ch.imetrica.jmdfarecurrent.transformdata.BoxCox;

public class MdfaSignalStrategySimulator {

	
	
	double currentMasterSignal = 0; 
	double previousMasterSignal = 0; 
	
	MdfaSignal eurusd;
	private long lotSize;
	private static final Logger LOGGER = LoggerFactory.getLogger("ch.imetrica.jmdfarecurrent.mdfasignal.MdfaSignalStrategySimulator");
	
	
	public void setSignalStrategy() throws NumberFormatException, IOException {
		
		
		

	
    	
    	
    	
    	
	}
	
	
	
	public void simuateStrategy() throws Exception {
		
		
		long ONELOT = 100; 
		CSVReader reader = new CSVReader(new FileReader("data/QQQ.IB.dat"));
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
    	double frequency = Math.PI/5.0;
    	double[] frequencies = new double[5];
    	
    	
    	frequencies[0] = Math.PI/30.0; 
    	frequencies[1] = Math.PI/22.0;
    	frequencies[2] = Math.PI/16.0;
    	frequencies[3] = Math.PI/10.0;
    	frequencies[4] = Math.PI/5.0;
    	
    	MdfaSignal signal = new MdfaSignal(L, frequencies, new BoxCox());
        
    	for(int i = 0; i < 900; i++) {    		
    		signal.setHistoricalObservation(streamDates.get(i), streamPrice.get(i));   		
    	}
    	
    	signal.computeHistoricalSignals();
    	signal.computeSymmetricFilter(L1, frequency);
    	signal.computeSymmetricSignal();
		signal.setSignalType(0);
		
    	for(int i = 900; i < 1000; i++) {     	
    		signal.addObservation(streamDates.get(i), streamPrice.get(i), streamPrice.get(i));   		
    	}
    	
		
		
		/* Set strategy settings and define order and simulator*/
		Security eurusd = new Security("EUR",Currency.USD);
		Strategy bollinger = new Strategy("BollingerBand");
		LimitOrder order;
		
		Simulator simulator = new SimulatorImpl();
		simulator.createCashBalance("STRATEGY", Currency.USD, new BigDecimal(1000000.0));
		
		
		
		int observation = 0; 
		int maxObservations = signal.getNumberObservations();
		
		while (observation < maxObservations) {
			 
			double price = signal.getPrice(observation+L1-1);
			String date_stamp = signal.getPriceDate(observation+L1-1);
		    
			/* Compute new signal */
		    previousMasterSignal = currentMasterSignal; 
		    currentMasterSignal = signal.getTradeSignal(observation);
			
			System.out.println(date_stamp + ", " + price + ", " +  signal.getTradeSignalDate(observation) + ", " + currentMasterSignal);
			
	      	/* Handle signal entry/exit logic */
	      	if(currentMasterSignal != previousMasterSignal) {
	      	    	 
    	    	  if(previousMasterSignal == 0) { lotSize = ONELOT;} 
    	    	  else lotSize = 2*ONELOT;
    	    	  
	      	      if(currentMasterSignal == 0) {    	    	    
	      	    	    
	      	    	    lotSize = ONELOT;
	      	    	    if(previousMasterSignal > 0) {	      	    	      	
	      	    	      order = new LimitOrder(Side.SELL, lotSize, eurusd, bollinger, new BigDecimal(price));
	      	    	      simulator.sendOrder(order);
	      	    	    }
	      	    	    else {
	      	    	      order = new LimitOrder(Side.BUY, lotSize, eurusd, bollinger, new BigDecimal(price));
		      	    	  simulator.sendOrder(order);
	      	    	    }
	      	    	    
	  	      	        Position position = simulator.findPositionByStrategyAndSecurity(bollinger.getName(), eurusd);
	  	   	    	    LOGGER.info(position.toString());	
	      	    		
	      	    		break; 
	      	      } 
 
	      		
	      	      if(currentMasterSignal > previousMasterSignal) { //Buy order
	      	    	  	      	    	  
	      	    	  order = new LimitOrder(Side.BUY, lotSize, eurusd, bollinger, new BigDecimal(price));
	      	    	  simulator.sendOrder(order);
	      	    	  
	      	      }
	      	      else if(currentMasterSignal < previousMasterSignal) { //Sell order
	      	    	  
	      	    	  order = new LimitOrder(Side.SELL, lotSize, eurusd, bollinger, new BigDecimal(price));
	      	    	  simulator.sendOrder(order);
	      	    	  
	      	      }

	      	      /*Log current position*/
	      	      Position position = simulator.findPositionByStrategyAndSecurity(bollinger.getName(), eurusd);
	   	    	  LOGGER.info(position.toString());	
	      	}		      
			observation++;
	   }
	
	}
	
	
	
	public static void main(String[] args) throws Exception { 
		
		
		MdfaSignalStrategySimulator simulate = new MdfaSignalStrategySimulator();
		
		simulate.simuateStrategy();
		
	}
	
	
	
	
}
