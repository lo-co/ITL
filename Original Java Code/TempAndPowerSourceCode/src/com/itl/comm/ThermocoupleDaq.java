package com.itl.comm;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This interface will cover both the Omega HH802U and the Omega OM-USB-TC 
 * thermocouple DAQs.
 * <p>
 * Basically, implementing classes should cover setup (from an XML Element), 
 * the creation of a TimerTask to regularly read data in, a means to get that 
 * data out of the object, and a way of shutting down the TimerTask and, if 
 * necessary, closing the connection to the instrument if something goes wrong 
 * or if the user stops the program.
 * 
 * @author kgraba
 *
 */
public interface ThermocoupleDaq {
	public int getNumChannels();
	
	/**
	 * @param sensor the index of the thermocouple sensor
	 * being queried
	 * @return true if the given sensor has valid data
	 */
	public boolean isValid(int sensor);
	
	/**
	 * @param sensor the index of the thermocouple sensor
	 * being queried
	 * @return the units for the given sensor. Assumes that 
	 * the sensor index is in-range and that the data is valid.
	 */
	public String getUnits(int sensor);
	
	/**
	 * @param sensor sensor the index of the thermocouple sensor
	 * being queried
	 * @return the measurement for the given sensor. Assumes
	 * that the sensor index is in-range and that the data is valid. 
	 */
	public double getData(int sensor);
	
	/**
	 * If the given sensor does not have valid data (or
	 * if its out of range), gives a human-readable error
	 * message
	 */
	public String getError(int sensor);
	
	/**
	 * If the instrument is still connected properly but 
	 * hasn't responded properly to a query for data, this 
	 * method will return when valid data was last returned.
	 * 
	 * @return the time, in nanoseconds, from when valid data was 
	 * most recently taken.
	 */
	public long getSampleTime();
	
	/** 
	 * Since we basically used the getters above only to build XML elements,
	 * I decided to just go ahead and move that responsibility here. This has
	 * the advantage of letting the ThermocoupleDaq object itself deal with the
	 * varying number of channels (if we ever want to do that).
	 * @param doc
	 * @return an array of XML elements representing the temperature(s).
	 */
	public Element[] toXml(Document doc);
	
	/**
	 * 2015-03-24: Since we want to temporarily stop the data acquisition at 
	 * some point, we'll need a method to re-start it!
	 */
	public void startDataAcquisition();
	
	/**
	 * 2015-03-24: We want the ability to temporarily stop the data acquisition
	 * without necessarily deleting the object.
	 */
	public void stopDataAcquisition();
	
	/**
	 * Ensure that the timer task responsible for taking regular
	 * measurements stops and any resources (such as the serial 
	 * port connection to the instrument) are released. 
	 */
	public void shutdown();
}
