package com.itl.general;

public class LogEntry {
	public long timeStamp;
	public double[] data;
	
	public LogEntry(long timeStamp, double[] entry) {
		this.timeStamp = timeStamp;
		this.data = entry;
	}
}
