package com.itl.comm.chroma66200;

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


public class Chroma66200 implements PowerMeter {
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

	/** 66200-specific settings (set by configuration file) */
	String gpbiPort;

	/** the timer used to run the MeasureTask */
	Timer measuringTimer;

	// Apparently the Chroma can only handle a single measurement, according to Zack.
	static final int numChannels = 1;
	
	double voltage;
	double amperage;
	double wattage;
	double powerFactor;
	double voltageThd;
	double amperageThd;

	/** Whether or not we have a connection with an actual Chroma 66200. */
	private boolean isConnected;
	
	/** the last time valid data was taken */
	Long sampleTime;

	/** for passing messages back to the user */
	LogTextPane logPane;
		
	/**
	 * Create a Chroma 66200 object from an XML element.
	 * 
	 * @param powerMeter the XML element which contains the
	 * appropriate settings for the Chroma66200 (e.g., comPort
	 * and gpibPort)
	 * @param logPane the GUI element (if any) which displays important
	 * information to the user about the status of the connection
	 * @throws SerialPortException if SerialPortConnection creation 
	 * fails
	 * @throws IOException if something goes wrong when trying to
	 * query the device's ID
	 * @throws InterruptedException if something goes wrong when
	 * trying to query the device's ID
	 */
	public Chroma66200 (Element powerMeter, LogTextPane logPane) 
			throws SerialPortException, IOException, InterruptedException {
		this.logPane = logPane;

		portName = powerMeter.getElementsByTagName("comPort").
				item(0).getTextContent();
		gpbiPort = powerMeter.getElementsByTagName("gpibPort").
				item(0).getTextContent();

		serialPortConnection = new SerialPortConnection(
				portName, BAUDRATE, DATABITS,
				STOPBITS, PARITY);

		isConnected = isConnectedToChroma();
		startDataAcquisition();
	}
		
	/**
	 * Checks first to ensure we're connected to a Prologix
	 * box and if so, checks to ensure we're connected to
	 * a Chroma.
	 * 
	 * @return true if connected to a Prologix box on the given
	 * COM port and to a Chroma on the given GPIB port, false 
	 * otherwise.
	 * @throws IOException if the serialPortConnection has trouble
	 * either writing or reading
	 * @throws InterruptedException the thread sleeps between 
	 * sending messages to the Prologix box and reading the
	 * response; this is thrown if the thread is interrupted
	 */
	private boolean isConnectedToChroma() 
			throws IOException, InterruptedException {
		boolean isChroma;

		// check first for Prologix box: if that's not here,
		// we can't send commands to the Chroma!
		serialPortConnection.write("++ver\n");
		Thread.sleep(20);

		String response = serialPortConnection.read();
		if (response.toLowerCase().indexOf("prologix") != -1) {
			logPane.append("Controller found: " + response, Color.BLACK);

			// Now check for a Chroma
			serialPortConnection.write("++addr\n");
			Thread.sleep(20);
			response = serialPortConnection.read();

			serialPortConnection.write("SYST:VER?\n");
			Thread.sleep(20);

			if (serialPortConnection.read().toLowerCase().indexOf("chroma") != -1) {
				logPane.append("Chroma present", Color.BLACK);
				isChroma = true;
				// TODO: what if there's nothing returned?
			} else {
				logPane.append("Chroma instrument not found!", Color.RED);
				isChroma =  false;
			}

		} else {
			logPane.append("Prologix device not found!", Color.RED);
			isChroma =  false;
		}

		return isChroma;
	}
		
	private class MeasureTask extends TimerTask {
		/**
		 * This task basically consists of writing a query to
		 * the instrument, sleeping the Thread 100ms, and then 
		 * reading the returned data.
		 * <p>
		 * If the instrument doesn't return all of the values
		 * expected, they're simply thrown out. If the 
		 * serialPortConnection fails or the Thread's sleep is
		 * interrupted, it signifies a severe problem and the
		 * timer running this task is cancelled and the serial 
		 * port connection is closed. 
		 */
		public void run() {
			try {

				// For Chroma Power Meter devices there is a header
				serialPortConnection.write("SYST:HEAD OFF\n");

				// Set incoming data to be separated with commas
				serialPortConnection.write("SYST:TRAN:SEP 0\n");

				serialPortConnection.write("SYST:HEAD?\n");
				String response = serialPortConnection.read();
				logPane.append("Header Status: " + response, Color.BLACK);

				// TODO: make sure that this should be fetch instead of 
				// measure
				// TODO: eventually, this will include power factor (PF),
				// total harmonic distortion for voltage (THDV) and amperage
				// (THDI)
				serialPortConnection.write("FETC? V,I,W\n");
				
				// TODO: make sure this timing will work
				Thread.sleep(100); // how long for power meter write/response
				String[] measurementStrings = 
						serialPortConnection.read().split(",");

				// if the length is not three, something's gone wrong
				if (measurementStrings.length == 3) {
					try {
						voltage = Double.parseDouble(measurementStrings[0]);
						amperage = Double.parseDouble(measurementStrings[1]);
						wattage = Double.parseDouble(measurementStrings[2]);
					} catch (NumberFormatException e) {
						logPane.append(e.getMessage(), Color.RED);
					}
				} else {
					rLogger.info("Chroma 66200: Could not read all three data readings; " +
							"discarding buffer.");
					logPane.append("Chroma 66200: Could not read all three data readings; " +
							"discarding buffer.%n", 
							Color.RED);
				}
			} catch (IOException | InterruptedException e) {
				wLogger.error(e.getMessage(), e);
				logPane.append(String.format("%s%n%s%n%s", 
						e.getMessage(),
						"Cancelling measurement. Closing serial port.",
						"You must reconnect to the Chroma66200 before taking more measurements."));
				shutdown();
			}
		}
	}
		
	@Override
	public double getVoltage(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return voltage;
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getAmperage(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return amperage;
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getWattage(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return wattage;
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getPowerFactor(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return powerFactor;
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double getVoltageDistortion(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return voltageThd;
		} else {
			return Double.NaN;
		}
	}
	
	@Override
	public double getAmperageDistortion(int chan) {
		if (chan >= 0 || chan < numChannels) {
			return amperageThd;
		} else {
			return Double.NaN;
		}
	}

	@Override
	public Element[] toXml(Document doc) {
		Element[] elements = new Element[numChannels];
		elements[0] = doc.createElement("powerReading");
		elements[0].setAttribute("channel", "0");
		
		Element voltageElement = doc.createElement("voltage");
		voltageElement.setAttribute("units", "Volts");
		voltageElement.appendChild(doc.createTextNode(
				String.format("%f", voltage)));
		elements[0].appendChild(voltageElement);

		Element amperageElement = doc.createElement("amperage");
		amperageElement.setAttribute("units", "Amps");
		amperageElement.appendChild(doc.createTextNode(
				String.format("%f", amperage)));
		elements[0].appendChild(amperageElement);

		Element wattageElement = doc.createElement("wattage");
		wattageElement.setAttribute("units", "Watts");
		wattageElement.appendChild(doc.createTextNode(
				String.format("%f", wattage)));
		elements[0].appendChild(wattageElement);

		Element pfElement = doc.createElement("powerFactor");
		pfElement.appendChild(doc.createTextNode(
				String.format("%f", powerFactor)));
		elements[0].appendChild(pfElement);

		Element vdElement = doc.createElement("voltageDistortion");
		vdElement.appendChild(doc.createTextNode(
				String.format("%f", voltageThd)));
		elements[0].appendChild(vdElement);
		
		Element adElement = doc.createElement("amperageDistortion");
		adElement.appendChild(doc.createTextNode(
				String.format("%f", amperageThd)));
		elements[0].appendChild(adElement);

		return elements;
	}
	
	@Override
	public void startDataAcquisition() throws IOException, InterruptedException {
		if (isConnected) {
			measuringTimer = new Timer();
			measuringTimer.schedule(new MeasureTask(), 0, 1000);
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