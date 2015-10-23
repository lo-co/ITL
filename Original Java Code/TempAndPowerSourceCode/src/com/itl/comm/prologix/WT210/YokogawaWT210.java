package com.itl.comm.prologix.WT210;

import java.awt.Color;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import gnu.io.SerialPort;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itl.comm.PowerMeter;
import com.itl.comm.SerialPortConnection;
import com.itl.comm.SerialPortException;
import com.itl.comm.powerItl.Frontend;
import com.itl.swing.LogTextPane;

@SuppressWarnings("unused")
public class YokogawaWT210 implements PowerMeter {
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
	
	/** WT210-specific settings (set by configuration file) */
	String gpbiPort;
	
	/** the timer used to run the MeasureTask */
	Timer measuringTimer;
	
	// Note that the WT210 can only handle a single measurement. However, I think that
	// other meters in the WT200 series can handle multiple measurements. If we ever get 
	// one of those, I'll have to update this to make it more like YokogawaWT300.
	static final int numChannels = 1;
	
	double voltage;
	double amperage;
	double wattage;
	double powerFactor;
	double voltageThd;
	double amperageThd;
	
	/** Whether or not we have a connection with an actual WT210. */
	private boolean isConnected;
	
	/** the last time valid data was taken */
	Long sampleTime;
	
	/** for passing messages back to the user */
	LogTextPane logPane;
	
	/**
	 * Create a YokogawaWT210 object from an XML element.
	 * 
	 * @param powerMeter the XML element which contains the
	 * appropriate settings for the YokogawaTW210 (e.g., comPort
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
	public YokogawaWT210(Element powerMeter, LogTextPane logPane)
			throws SerialPortException, IOException, InterruptedException {
		this.logPane = logPane;

		portName = powerMeter.getElementsByTagName("comPort").item(0).getTextContent();
		gpbiPort = powerMeter.getElementsByTagName("gpibPort").item(0).getTextContent();

		serialPortConnection = new SerialPortConnection(
				portName, BAUDRATE, DATABITS, STOPBITS, PARITY);

		isConnected = isConnectedToYokogawa();
		startDataAcquisition();
	}
	
	/**
	 * Checks first to ensure we're connected to a Prologix
	 * box and if so, checks to ensure we're connected to
	 * a Yokogawa.
	 * 
	 * @return true if connected to a Prologix box on the given
	 * COM port and to a Yokogawa on the given GPIB port, false 
	 * otherwise.
	 * @throws IOException if the serialPortConnection has trouble
	 * either writing or reading
	 * @throws InterruptedException the thread sleeps between 
	 * sending messages to the Prologix box and reading the
	 * response; this is thrown if the thread is interrupted
	 */
	private boolean isConnectedToYokogawa() throws IOException,
			InterruptedException {
		boolean isYokogawa;

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

			serialPortConnection.write("*idn?\n");
			Thread.sleep(20);

			if (serialPortConnection.read().toLowerCase().indexOf("yokogawa") != -1) {
				logPane.append("Yokogawa present", Color.BLACK);
				isYokogawa = true;
				// TODO: what if there's nothing returned?
			} else {
				logPane.append("Yokogawa instrument not found!", Color.RED);
				isYokogawa = false;
			}

		} else {
			logPane.append("Prologix device not found!", Color.RED);
			isYokogawa = false;
		}

		return isYokogawa;
	}
	
	/**
	 * Sets the WT210 to only output voltage, amperage,
	 * and wattage.
	 * 
	 * @author kgraba
	 */
	private void setMeasurements() throws IOException {
		serialPortConnection.write(
				":meas:norm:item:v:all on; " + 
				":meas:norm:item:a:all on; " + 
				":meas:norm:item:w:all on; " + 
				":meas:norm:item:va:all off; " + 
				":meas:norm:item:var:all off; " + 
				":meas:norm:item:pf:all on; " + 
				":meas:norm:item:deg:all off; " + 
				":meas:norm:item:vhz:all off; " + 
				":meas:norm:item:ahz:all off; " + 
				":meas:norm:item:wh:all off; " + 
				":meas:norm:item:whp:all off; " + 
				":meas:norm:item:whm:all off; " + 
				":meas:norm:item:ah:all off; " + 
				":meas:norm:item:ahp:all off; " + 
				":meas:norm:item:ahm:all off; " + 
				":meas:norm:item:vpk:all off; " + 
				":meas:norm:item:apk:all off; " + 
				":meas:norm:item:time off; " + 
				":meas:norm:item:math off;" + 
				"\n");
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
				serialPortConnection.write("meas:val?\n");
				Thread.sleep(100);
				String[] measurementStrings = 
						serialPortConnection.read().split(",");

				// if the length is not three, something's gone wrong
				if (measurementStrings.length == 4) {
					double[] newData = new double[measurementStrings.length];
					double parsedValue;

					try {
						for (int i = 0; i < measurementStrings.length; i++) {
							parsedValue = Double.parseDouble(measurementStrings[i]);
							if (parsedValue == Double.parseDouble("9.9E+37")) {
								// Yokogawa specifc number: 9.9E+37 
								newData[i] = Double.POSITIVE_INFINITY;
							} else if (parsedValue == Double.parseDouble("9.91E+37")) {
								// Yokogawa specifc number: 9.91E+37
								newData[i] = Double.NaN;
							} else {
								newData[i] = parsedValue;
							}
						}

						voltage = newData[0];
						amperage = newData[1];
						wattage = newData[2];
						powerFactor = newData[3];
					} catch (NumberFormatException e) {
						logPane.append(e.getMessage(), Color.RED);
					}

				} else {
					rLogger.info("Yokogawa WT210: Power data readings failed; discarding buffer.");
					logPane.append(
							"Yokogawa WT210: Power data readings failed; discarding buffer.",
							Color.RED);
				}

			} catch (IOException | InterruptedException e) {
				wLogger.error(e.getMessage(), e);
				logPane.append(String.format("%s%n%s%n%s", e.getMessage(),
						"Cancelling measurement. Closing serial port.",
						"You must reconnect to the YokogawaTW210 before taking more measurements."));
				shutdown();
			}
		}
	}
	
	private void measureHarmonicsTask() {
		try {
			int elementCount = 0;
			double athd = 0.0;
			double vthd = 0.0;

			String temp;
			String[] dataStream = new String[30];

			serialPortConnection.write(
					":meas:norm:item:v:all off; " + 
					":meas:norm:item:a:all off; " + 
					":meas:norm:item:w:all off; " + 
					":meas:norm:item:pf:all off; " + 
					"\n");
			Thread.sleep(250);

			serialPortConnection.write(":meas:val?\n");
			Thread.sleep(100);

			// Turn harmonics on
			serialPortConnection.write(":harm:stat on\n"); // Error 420
			Thread.sleep(2100); // WT210 User's Manual (pg240)
			logPane.append("Harmonics enabled.", Color.BLACK);

			// Enable voltage harmonics
			serialPortConnection.write("meas:harm:item:pres vpattern\n");
			Thread.sleep(100);

			// Query voltage harmonics
			serialPortConnection.write("meas:harm:val?\n");
			Thread.sleep(500);
			dataStream = serialPortConnection.read().split(",");

			// Parse distortion value from voltage harmonics reading
			vthd = Double.parseDouble(dataStream[1]);
			if (vthd != Double.POSITIVE_INFINITY || vthd != Double.NaN) {
				elementCount++;
			}

			// Enable current harmonics
			serialPortConnection.write("meas:harm:item:pres apattern\n");
			Thread.sleep(100);

			// Query current harmonics
			serialPortConnection.write("meas:harm:val?\n");
			Thread.sleep(500);
			dataStream = serialPortConnection.read().split(",");

			// Parse distortion value from current harmonics reading
			athd = Double.parseDouble(dataStream[1]);
			if (athd != Double.POSITIVE_INFINITY || athd != Double.NaN) {
				elementCount++;
			}

			// Turn harmonics off
			serialPortConnection.write(":harm:stat off\n"); // Error 420
			Thread.sleep(2000); // WT210 User's Manual (pg240)
			logPane.append("Harmonics disabled.", Color.BLACK);

			// Reset standard measurements
			setMeasurements();
			Thread.sleep(1000);

			if (elementCount == 2) {
				amperageThd = athd;
				voltageThd = vthd;
			} else {
				rLogger.info("Yokogawa WT210: Harmonics data readings failed; discarding buffer.");
				logPane.append(
						"Yokogawa WT210: Harmonics data readings failed; discarding buffer.",
						Color.RED);
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
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

		Element vThdElement = doc.createElement("voltageDistortion");
		vThdElement.appendChild(doc.createTextNode(
				String.format("%f", voltageThd)));
		elements[0].appendChild(vThdElement);
		
		Element aThdElement = doc.createElement("amperageDistortion");
		aThdElement.appendChild(doc.createTextNode(
				String.format("%f", amperageThd)));
		elements[0].appendChild(aThdElement);

		return elements;
	}
	
	@Override
	public void startDataAcquisition() throws IOException, InterruptedException {
		if (isConnected) {
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
