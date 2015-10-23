package com.itl.comm.yokogawa.WT330;

import java.awt.Color;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import gnu.io.SerialPort;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itl.comm.PowerMeter;
import com.itl.comm.SerialPortConnection;
import com.itl.comm.SerialPortException;
import com.itl.comm.powerItl.Frontend;
import com.itl.swing.LogTextPane;

/**
 * This class will implement the PowerMeter interface for the Yokogawa 330-
 * series of power meters. Unfortunately, since the USB interface doesn't
 * seem to easily work with JNA, this means that we have to revert back to
 * using the Prologix GPIB-to-USB interface. As a result, this class is 
 * going to borrow heavily from the YokogawaWT210 class, which uses a 
 * similar hardware setup.
 * 
 * @author kgraba
 *
 */
public class WT330 implements PowerMeter {
	/** 
	 * rLogger (root logger) has a single appender to write to a file.
	 * This will be used to write debug and info messages.
	 * Since it's the root logger (from which every other
	 * logger is descended), it'll also get whatever we send
	 * to the warning logger.
	 */
	Logger rLogger = Logger.getLogger(Frontend.class);
	
	/**
	 * The warning logger has a single appender to write
	 * to the console. It's just for warnings and errors.
	 */
	Logger wLogger = Logger.getLogger("consoleLogger");
	
	// Serial port settings
	String portName; // set by configuration file 
	public static final int BAUDRATE = 9600;
	public static final int DATABITS = 8;
	public static final int STOPBITS = 1;
	public static final int PARITY = SerialPort.PARITY_EVEN;
	
	SerialPortConnection serialPortConnection;
	
	/** WT330-specific settings (set by configuration file) */
	String gpbiPort;
	
	/** the timer used to run the MeasureTask */
	Timer measuringTimer;
	
	/**
	 * There are two model numbers in the WT330 series: the WT332 (which
	 * can measure up to two channels) and the WT333 (which can measure up
	 * to three channels). When we get the identity of the power meter, we
	 * can set numChannels then the size of the measurements arrays.  
	 */
	int numChannels;
	
	/**
	 * The number of measurements per channel is currently 6 (voltage,
	 * current, active power, power factor lambda, voltage THD, and 
	 * amperage THD).
	 */
	static final int NUM_MEAS = 6;
	
	// measurements arrays
	double[] voltages;
	double[] amperages;
	double[] wattages;
	double[] powerFactors;
	double[] voltageThds;
	double[] amperageThds;
	
	/** Whether or not we have a connection with an actual WT330. */
	private boolean isConnected;
	
	/** the last time valid data was taken */
	Long sampleTime;
	
	/** for passing messages back to the user */
	LogTextPane logPane;
	
	/**
	 * Create a WT330 object from an XML element. Assuming that it connects 
	 * properly, the measuringTimer will be started.
	 *  
	 * @param powerMeter the XML element which contains the appropriate 
	 * settings for the WT330 (e.g., comPort and gpibPort)
	 * @param logPane the GUI element (may be null) that displays important
	 * information to the user about the status of the connection
	 * @throws IOException if something goes wrong when trying to
	 * query the device's ID
	 * @throws SerialPortException if SerialPortConnection creation 
	 * fails
	 * @throws InterruptedException if something goes wrong when
	 * trying to query the device's ID
	 */
	public WT330(Element powerMeter, LogTextPane logPane) 
			throws SerialPortException, IOException, InterruptedException {
		this.logPane = logPane;
		
		portName = powerMeter.getElementsByTagName("comPort").item(0).getTextContent();
		gpbiPort = powerMeter.getElementsByTagName("gpibPort").item(0).getTextContent();

		serialPortConnection = new SerialPortConnection(
				portName, BAUDRATE, DATABITS, STOPBITS, PARITY);
		
		isConnected = isConnectedToWT330();
		if (isConnected) {
			voltages = new double[numChannels];
			amperages = new double[numChannels];
			wattages = new double[numChannels];
			powerFactors = new double[numChannels];
			voltageThds = new double[numChannels];
			amperageThds = new double[numChannels];
			
			startDataAcquisition();
		}
	}

	/**
	 * Check first to ensure that we're Prologix box and if so, check
	 * to ensure that we're connected to a Yokogawa WT330-series power
	 * meter. If so, check which meter we're working with exactly and
	 * set the size of the voltages, amperages, etc. arrays.
	 * <p>
	 * This also sets the number of channels to 2 or 3 if we're connected
	 * to a WT332 or WT333, respectively, or 0 if we're not connected to
	 * a WT330 series of instrument.
	 * 
	 * @return
	 * @throws IOException if the serialPortConnection has trouble
	 * either writing or reading
	 * @throws InterruptedException the thread sleeps between 
	 * sending messages to the Prologix box and reading the
	 * response; this is thrown if the thread is interrupted
	 */
	private boolean isConnectedToWT330() 
			throws IOException, InterruptedException {
		boolean isWT330;
		
		// check first for Prologix box: if that's not here,
		// we can't send commands to the Yokogawa!
		serialPortConnection.write("++ver\n");
		
		Thread.sleep(20);

		String response = serialPortConnection.read();
		if (response.toLowerCase().indexOf("prologix") != -1) {
			logPane.append("Controller found: " + response, Color.BLACK);

			// Now check for a Yokogawa
			serialPortConnection.write("++addr\n");
			Thread.sleep(20);
			response = serialPortConnection.read();

			serialPortConnection.write("*IDN?\n");
			Thread.sleep(20);

			response = serialPortConnection.read().toLowerCase();
			if (response.indexOf("yokogawa") != -1) {
				if (response.indexOf("wt332") != -1) {
					logPane.append("Yokogawa WT332 present", Color.BLACK);
					numChannels = 2;
					isWT330 = true;
				} else if (response.indexOf("wt333") != -1) {
					logPane.append("Yokogawa WT333 present", Color.BLACK);
					numChannels = 3;
					isWT330 = true;	
				} else {
					logPane.append("Unknown Yokogawa detected", Color.RED);
					numChannels = 0;
					isWT330 = false;
				}
			} else {
				logPane.append("Yokogawa instrument not found!", Color.RED);
				numChannels = 0;
				isWT330 = false;
			}

		} else {
			logPane.append("Prologix device not found!", Color.RED);
			numChannels = 0;
			isWT330 = false;
		}

		return isWT330;
	}
	
	/**
	 * Sends commands to the WT330 to set its response format,
	 * the number of items to send data about, and define those
	 * items.
	 * 
	 * @throws IOException - if we can't write on the serial port connection.
	 * @throws InterruptedException - if the thread calls don't work properly.
	 */
	private void setMeasurements() throws IOException, InterruptedException {
		serialPortConnection.write(":NUMERIC:FORMAT ASCII\n");
		
		Thread.sleep(40);
//		serialPortConnection.write(":STATUS:ERROR?\n");
//		Thread.sleep(40);
//		String response = serialPortConnection.read();
//		System.out.println("Error0: " + response);
		
		int numItems = numChannels * NUM_MEAS;
		serialPortConnection.write(String.format(
				":NUMERIC:NORMAL:NUMBER %d\n", numItems));
		
		Thread.sleep(40);
//		serialPortConnection.write(":STATUS:ERROR?\n");
//		Thread.sleep(40);
//		response = serialPortConnection.read();
//		System.out.println("Error1: " + response);
		
		// if this is a 332 and we have two channels, they'll be known as channels 1 and 3
		// to the meter itself. If this is a 333, the available channels will be 1, 2, and 3
		int[] availChans = new int[numChannels];
		availChans[0] = 1;
		if (numChannels == 2) {
			availChans[1] = 3;
		} else if (numChannels == 3) {
			availChans[1] = 2;
			availChans[2] = 3;
		}
		
		StringBuffer sbCmd = new StringBuffer(":NUMERIC:NORMAL:");
		for (int i = 0; i < numChannels; i++) {
			sbCmd.append(String.format("ITEM%d U,%d;", i*NUM_MEAS+1, availChans[i]));
			sbCmd.append(String.format("ITEM%d I,%d;", i*NUM_MEAS+2, availChans[i]));
			sbCmd.append(String.format("ITEM%d P,%d;", i*NUM_MEAS+3, availChans[i]));
			sbCmd.append(String.format("ITEM%d LAMBDA,%d;", i*NUM_MEAS+4, availChans[i]));
			sbCmd.append(String.format("ITEM%d UTHD,%d;", i*NUM_MEAS+5, availChans[i]));
			sbCmd.append(String.format("ITEM%d ITHD,%d;", i*NUM_MEAS+6, availChans[i]));
		}
		serialPortConnection.write(sbCmd.toString() + "\n");
		
		Thread.sleep(40);
//		serialPortConnection.write(":STATUS:ERROR?\n");
//		Thread.sleep(40);
//		response = serialPortConnection.read();
//		System.out.println("Error2: " + response);
	}
	
	/**
	 * This task basically consists of writing a query to the instrument,
	 * sleeping the Thread 100ms, and then reading the returned data.
	 * <p>
	 * If the instrument doesn't return all of the values expected, they're
	 * simply thrown out. If the serialPortConnection fails or the Thread's
	 * sleep is interrupted, it signifies a severe problem and the timer
	 * running this task is cancelled and the serial port connection is
	 * closed.
	 */
	private class MeasureTask extends TimerTask {
		public void run() {
			try {
				serialPortConnection.write(":NUMERIC:NORMAL:VALUE?\n");
				Thread.sleep(100);
				String[] measurementStrings = 
						serialPortConnection.read().split(",");

				// if the length is not three, something's gone wrong
				if (measurementStrings.length == numChannels * NUM_MEAS) {
					double[][] newData = new double[numChannels][NUM_MEAS];

					for (int i = 0; i < numChannels; i++) {
						for (int j = 0; j < NUM_MEAS; j++) {
							try {
								newData[i][j] = Double.parseDouble(measurementStrings[i*NUM_MEAS + j]);
							} catch (NumberFormatException e) {
								newData[i][j] = Double.NaN;
							}
						}
						
						voltages[i] = newData[i][0];
						amperages[i] = newData[i][1];
						wattages[i] = newData[i][2];
						powerFactors[i] = newData[i][3];
						voltageThds[i] = newData[i][4];
						amperageThds[i] = newData[i][5];
					}
				} else {
					rLogger.info("Yokogawa WT330: Data readings failed; discarding buffer.");
					logPane.append(
							"Yokogawa WT330: Data readings failed; discarding buffer.",
							Color.RED);
				}

			} catch (IOException | InterruptedException e) {
				wLogger.error(e.getMessage(), e);
				logPane.append(String.format("%s%n%s%n%s", e.getMessage(),
						"Cancelling measurement. Closing serial port.",
						"You must reconnect to the WT330 before taking more measurements."));
				shutdown();
			}
		}
	}
	
	@Override
	public double getVoltage(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return voltages[chan];
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getAmperage(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return amperages[chan];
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getWattage(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return wattages[chan];
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getPowerFactor(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return powerFactors[chan];
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getAmperageDistortion(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return amperageThds[chan];
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getVoltageDistortion(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return voltageThds[chan];
		} else {
			return Double.NaN;
		}
	}

	@Override
	public Element[] toXml(Document doc) {
		Element[] elements = new Element[numChannels];
		for (int i = 0; i < numChannels; i++) {
			elements[i] = doc.createElement("powerReading");
			elements[i].setAttribute("channel", String.format("%d", i));
			
			Element voltageElement = doc.createElement("voltage");
			voltageElement.setAttribute("units", "Volts");
			voltageElement.appendChild(doc.createTextNode(
					String.format("%f", voltages[i])));
			elements[i].appendChild(voltageElement);

			Element amperageElement = doc.createElement("amperage");
			amperageElement.setAttribute("units", "Amps");
			amperageElement.appendChild(doc.createTextNode(
					String.format("%f", amperages[i])));
			elements[i].appendChild(amperageElement);

			Element wattageElement = doc.createElement("wattage");
			wattageElement.setAttribute("units", "Watts");
			wattageElement.appendChild(doc.createTextNode(
					String.format("%f", wattages[i])));
			elements[i].appendChild(wattageElement);

			Element pfElement = doc.createElement("powerFactor");
			pfElement.appendChild(doc.createTextNode(
					String.format("%f", powerFactors[i])));
			elements[i].appendChild(pfElement);

			Element vThdElement = doc.createElement("voltageDistortion");
			vThdElement.appendChild(doc.createTextNode(
					String.format("%f", voltageThds[i])));
			elements[i].appendChild(vThdElement);
			
			Element aThdElement = doc.createElement("amperageDistortion");
			aThdElement.appendChild(doc.createTextNode(
					String.format("%f", amperageThds[i])));
			elements[i].appendChild(aThdElement);
		}
		
		return elements;
	}

	@Override
	public void startDataAcquisition() throws IOException, InterruptedException {
		if (isConnected) {
			// The use-case for this is that a technician will pause the data 
			// acquisition so that she can alter the settings on the meter. For
			// now, I'll assume that they won't do anything to invalidate the 
			// connection to the meter, but I don't want to assume they didn't
			// alter the measurement settings.
			setMeasurements();
			measuringTimer = new Timer();
			measuringTimer.schedule(new MeasureTask(), 0, 2500);
		}
	}
	
	@Override
	public void stopDataAcquisition() {
		if (measuringTimer != null) {
			measuringTimer.cancel();
		}
	}
	
	@Override
	public void shutdown() {
		stopDataAcquisition();

		if (serialPortConnection != null) {
			serialPortConnection.close();
		}
	}

}
