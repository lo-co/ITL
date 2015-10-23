package com.itl.comm;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This interface is meant to cover the Yokogawa WT210, Yokogawa WT300 series, 
 * and Chroma 66200 power meters. With the inclusion of the WT300 series, we
 * now face the possibility of having to deal with multiple channels.
 * <p>
 * Basically, implementing classes should cover setup (using an XML Element), 
 * the creation of a TimerTask or something to regularly read data in, a means 
 * to get that data out of the object, and a way of shutting down the TimerTask 
 * and, if necessary, closing the connection to the instrument if something 
 * goes wrong or if the user stops the program.
 * 
 * @author kgraba
 *
 */
public interface PowerMeter 
{
	/** 
	 * @param chan
	 * @return the latest voltage reading from the meter for the 
	 * index of the channel given. If the index is out of range, 
	 * returns Double.NaN.
	 */
	public double getVoltage(int chan);
	
	/** 
	 * @param chan
	 * @return the latest amperage reading from the meter for the 
	 * index of the channel given. If the index is out of range, 
	 * returns Double.NaN.
	 */
	public double getAmperage(int chan);
	
	/** 
	 * @param chan
	 * @return the latest wattage reading from the meter for the 
	 * index of the channel given. If the index is out of range, 
	 * returns Double.NaN.
	 */
	public double getWattage(int chan);
	
	/** 
	 * @param chan
	 * @return the latest power factor reading from the meter for the 
	 * index of the channel given. If the index is out of range, 
	 * returns Double.NaN.
	 */
	public double getPowerFactor(int chan);
	
	/** 
	 * @param chan
	 * @return the latest amperage THD reading from the meter for the 
	 * index of the channel given. If the index is out of range, 
	 * returns Double.NaN.
	 */
	public double getAmperageDistortion(int chan);
	
	/** 
	 * @param chan
	 * @return the latest voltage THD reading from the meter for the 
	 * index of the channel given. If the index is out of range, 
	 * returns Double.NaN.
	 */
	public double getVoltageDistortion(int chan);
	
	/** 
	 * Since we basically used the getters above only to build XML elements,
	 * I decided to just go ahead and move that responsibility here. This has
	 * the advantage of letting the PowerMeter object itself deal with the
	 * varying number of channels. 
	 * @param doc
	 * @return an array of XML elements representing the voltage, amperage,
	 * wattage, power factor, voltage THD and amperage THD.
	 */
	public Element[] toXml(Document doc);
	
	/**
	 * 2015-03-24: Since we want to temporarily stop the data acquisition at 
	 * some point, we'll need a method to re-start it!
	 * @throws InterruptedException - if the Timer started in this method 
	 * doesn't start properly.
	 * @throws IOException 
	 */
	public void startDataAcquisition() throws IOException, InterruptedException;
	
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
