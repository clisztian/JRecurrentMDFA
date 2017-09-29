package ch.imetrica.jmdfarecurrent.timeseries;


import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@ToString(includeFieldNames=false)

public class TimeSeriesEntry<V> {
	
	public TimeSeriesEntry(String timeStamp2, V value2) {
		this.timeStamp = timeStamp2; 
		this.value = value2;
	}
	
	@Getter
	private String timeStamp;
	
	@Getter
	private V value;

	public V getValue() {
		return value;
	}
	
	public String getDateTime() {
		return timeStamp; 
	}

}