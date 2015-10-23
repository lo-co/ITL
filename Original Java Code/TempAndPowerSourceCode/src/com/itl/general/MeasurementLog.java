package com.itl.general;

import java.util.Vector;

/**
 * This keeps an expanding vector of Object arrays
 * as well as the methods used to compare extremes.
 * 
 * @author Kyle
 *
 */
public class MeasurementLog {
	private Vector<LogEntry> log;
	private int dataLength; // length of the data; does not include timeStamp
	
	public  MeasurementLog() {
		log = new Vector<LogEntry>();
		dataLength = 0;
	}
	
	public void addEntry(LogEntry newEntry) 
			throws IllegalArgumentException {
		if (dataLength != 0) {
			if (dataLength != newEntry.data.length) {
				throw new IllegalArgumentException(
						"newEntry data length does not match");
			} else {
				log.add(newEntry);
			}
		} else {
			dataLength = newEntry.data.length;
			log.add(newEntry);
		}
	}

	/**
	 * Finds the first entry with a timeStamp greater than or equal
	 * to the minimumTime given.  This assumes that the log entries
	 * are already sorted from earliest to latest.
	 * @param minimumTime
	 * @return the index of the first LogEntry
	 */
	public int findFirstLog(long minimumTime)
			throws IllegalArgumentException {
		int i = log.size() - 1;
		boolean isFirstEntryFound = false;
		while (i > -1 && !isFirstEntryFound) {
			if (log.get(i).timeStamp < minimumTime) {
				isFirstEntryFound = true;
			} else {
				i--;
			}
		}
		
		if (i == log.size() - 1) {
			throw new IllegalArgumentException(
					"All entries are earlier than minimumTime");
		} else {
			return (i + 1);
		}
	}
	
	/**
	 * Finds the last entry with a timeStamp (Object[0]) less than
	 * or equal to the maximumTime given.  This assumes that the
	 * log entries are already sorted from earliest to latest.
	 * @param maximumTime
	 * @return the index of the last LogEntry
	 */
	public int findLastLog(long maximumTime) 
			throws IllegalArgumentException {
		int i = 0;
		boolean isLastEntryFound = false;
		while (i < log.size() && !isLastEntryFound) {
			if (log.get(i).timeStamp > maximumTime) {
				isLastEntryFound = true;
			} else {
				i++;
			}
		}
		
		if (i == 0) {
			throw new IllegalArgumentException(
					"All entries are later than the maximumTime");
		} else {
			return (i - 1);
		}
	}
	
	/**
	 * Finds the minimum values between startTime and endTime,
	 * assuming that the Object[0] is a time and that the rest of
	 * Object[] are numbers.  Note that while
	 * the output is an instance of an entry (i.e., an Object
	 * array), each element may be from different entries within
	 * the complete log.
	 */
	public LogEntry minimumValues(long startTime, long endTime) 
			throws IllegalArgumentException {
		int startIndex = findFirstLog(startTime);
		int endIndex = findLastLog(endTime);
		
		double[] logData;
		double[] minData = new double[dataLength];
		for (int i = 0; i < dataLength; i++) {
			minData[i] = Double.MAX_VALUE;
		}
		
		for (int i = startIndex; i <= endIndex; i++) {
			logData = log.get(i).data;
			for (int j = 0; j < dataLength; j++) {
				if (!Double.isNaN(logData[j]) &&
						!Double.isInfinite(logData[j]) &&
						logData[j] < minData[j]) {
					minData[j] = logData[j];
				}
			}
		}
		
		return new LogEntry(log.get(startIndex).timeStamp, minData);
	}
	
	/** 
	 * Finds the maximum values between startTime and endTime.
	 * Note that while the output is an instance of a LogEntry,
	 * each element may be from a different entries within the
	 * complete log.
	 * @author Kyle
	 *
	 */
	public LogEntry maximumValues(long startTime, long endTime) 
			throws IllegalArgumentException {
		int startIndex = findFirstLog(startTime);
		int endIndex = findLastLog(endTime);
		
		double[] logData;
		double[] maxData = new double[dataLength];
		for (int i = 0; i < dataLength; i++) {
			maxData[i] = -1 * Double.MAX_VALUE;
		}
		
		for (int i = startIndex; i <= endIndex; i++) {
			logData = log.get(i).data;
			for (int j = 0; j < dataLength; j++) {
				if (!Double.isNaN(logData[j]) &&
						!Double.isInfinite(logData[j]) &&
						logData[j] > maxData[j]) {
					maxData[j] = logData[j];
				}
			}
		}
		
		return new LogEntry(log.get(endIndex).timeStamp, maxData);
	}
	
	/**
	 * Finds the difference between the highest readings and the lowest
	 * readings found between startTime and endTime and returns a ratio
	 * of those differences to the lowest readings.  Note that readings
	 * within a category should be either all positive or all negative.
	 * @param startTime
	 * @param endTime
	 * @return an array of ratios
	 * @throws IllegalArgumentException
	 */
	public double[] maximumDifferencesRatio(
			long startTime, long endTime) 
			throws IllegalArgumentException  {
		double[] output = new double[dataLength];
		
		double[] minData = minimumValues(startTime, endTime).data;
		double[] maxData = maximumValues(startTime, endTime).data;
		
		for (int i = 0; i < dataLength; i++) {
			output[i] = (maxData[i] - minData[i])/minData[i];
		}
		return output;
	}
	
	/**
	 * Finds the difference between the highest readings and the lowest
	 * readings found between startTime and endTime.
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double[] maximumDifferencesAbsolute(
			long startTime, long endTime) 
			throws IllegalArgumentException  {
		double[] output = new double[dataLength];
		
		double[] minData = minimumValues(startTime, endTime).data;
		double[] maxData = maximumValues(startTime, endTime).data;
		
		for (int i = 0; i < dataLength; i++) {
			output[i] = maxData[i] - minData[i];
		}
		return output;
	}
	
	/**
	 * Finds the mean readings between startTime and endTime.
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double[] calculateAverages(long startTime, long endTime)
			throws IllegalArgumentException {
		int startIndex = findFirstLog(startTime);
		int endIndex = findLastLog(endTime); 
		
		double[] totals = new double[dataLength];
		double[] numberOfValues = new double[dataLength]; // The number of values that are not NaN or Infinite
		for (int j = 0; j < dataLength; j++) {
			totals[j] = 0;
			numberOfValues[j] = 0;
		}
		double[] logData;
		double[] averages = new double[dataLength];
		
		for (int i = startIndex; i <= endIndex; i++) {
			logData = log.get(i).data;
			for (int j = 0; j < dataLength; j++) {
				if (!Double.isNaN(logData[j]) && 
						!Double.isInfinite(logData[j])) {
					totals[j] = totals[j] + logData[j];
					numberOfValues[j]++;
				}
			}
		}
		
		for (int j = 0; j < dataLength; j++) {
			averages[j] = totals[j]/numberOfValues[j];
		}
		
		return averages;
	}
	
	/**
	 * Calculates the mean readings between the startTime and the most
	 * recent entry.
	 * @param startTime
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double[] calculateAverages(long startTime) 
			throws IllegalArgumentException {
		int startIndex = findFirstLog(startTime);
		
		double[] totals = new double[dataLength];
		double[] numberOfValues = new double[dataLength];
		for (int j = 0; j < dataLength; j++) {
			totals[j] = 0;
			numberOfValues[j] = 0;
		}
		double[] logData;
		double[] averages = new double[dataLength];
		
		for (int i = startIndex; i < log.size(); i++) {
			logData = log.get(i).data;
			for (int j = 0; j < dataLength; j++) {
				if (!Double.isNaN(logData[j]) &&
						!Double.isInfinite(logData[j])) {
					totals[j] = totals[j] + logData[j];
					numberOfValues[j]++;
				}
			}
		}
		
		for (int j = 0; j < dataLength; j++) {
			averages[j] = totals[j]/numberOfValues[j];
		}
		
		return averages;
	}
	
	public LogEntry getEntry(int i) {
		return log.get(i);
	}
	
	public int getDataLength() {
		return dataLength;
	}
	
	public int getLogSize() {
		return log.size();
	}
}
