package com.itl.comm.omega.hh802u;

import java.awt.Color;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itl.comm.SerialPortConnection;
import com.itl.comm.SerialPortException;
import com.itl.comm.ThermocoupleDaq;
import com.itl.comm.omega.Datagram;
import com.itl.comm.omega.DatagramException;
import com.itl.comm.powerItl.Frontend;
import com.itl.comm.powerItl.PowerItlException;
import com.itl.swing.LogTextPane;

public class OmegaHH802U implements ThermocoupleDaq {
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
	
	/** 
	 * The maximum number of times we'll try to verify 
	 * the model number before giving up. 
	 */
	private static final int MAXIMUM_VERIFICATION_ATTEMPTS = 3;
	
	public static final int NUM_CHANNELS = 2;
	
	// Serial port settings
	String portName; // set by config file 
	int baudRate = 19200;
	int dataBits = 8;
	int stopBits = 1;
	int parity = 2;
	
	// HH802U-specific settings (set by config file)
	int id;
	int channel;
		
	SerialPortConnection serialPortConnection;
	
	/** The Timer which will run our TimerTask */
	Timer measuringTimer;
	
	/** the last valid datagrams we could get */
	Datagram[] datagrams;
	
	boolean isConnected;
	
	/** 
	 * time, in nanoseconds, from when valid data was 
	 * most recently taken 
	 */
	Long sampleTime;
	
	/** the GUI element that'll display important notes to the user */
	LogTextPane logPane;
	
	public OmegaHH802U(Element thermocoupleDaq, LogTextPane logPane) {
		this.logPane = logPane;
		
		portName = thermocoupleDaq.getElementsByTagName("comPort").
				item(0).getTextContent();
		id = Integer.parseInt(
				thermocoupleDaq.getElementsByTagName("id").
				item(0).getTextContent());
		channel = Integer.parseInt(
				thermocoupleDaq.getElementsByTagName("channel").
				item(0).getTextContent());
		
		// since the serial port connection process can take
		// a while, put it in its own thread
		Thread t = new Thread(new Runnable() {
			public void run() {
				rLogger.info("Attempting to connect to the HH802U");
				OmegaHH802U.this.logPane.append("Attempting to connect to the HH802U");
				try {
					connect();
					isConnected = true;
					
					rLogger.info("Now connected to the HH802U");
					OmegaHH802U.this.logPane.append("Now connected to the HH802U");
				} catch (SerialPortException | IOException | 
						InterruptedException | PowerItlException e) {
					isConnected = false;
					
					wLogger.error(e.getMessage(), e);
					OmegaHH802U.this.logPane.append(String.format("%s%n%s%n",
							e.getMessage(),
							"Could not connect to the Omega HH802U"));
					shutdown();
				}
			}
		});
		t.start();
	}
	
	/**
	 * Establishes a serial port connection and
	 * verifies that the serial port is actually 
	 * connected to an Omega HH802U
	 * 
	 * @throws SerialPortException if the port doesn't exist
	 * or is in use or some other problem with the serial port
	 * that may or may not be so serious that the program 
	 * should halt entirely.
	 * @throws IOException if there's a problem with 
	 * serialPortConnection's InputStream or OutputStream
	 * @throws InterruptedException if the thread can't sleep
	 * properly
	 * @throws PowerItlException if the serial port is valid
	 * but not actually connected to an Omega HH802U
	 */
	public void connect()
			throws SerialPortException, IOException, 
			InterruptedException, PowerItlException {
		
		serialPortConnection = new SerialPortConnection(portName, baudRate,
				dataBits, stopBits, parity);
		
		// Ensure that the serial port is in fact 
		// connected to an HH802U
		int attempt = 0;
		boolean isHH802U = false;
		while (attempt < MAXIMUM_VERIFICATION_ATTEMPTS
				&& !isHH802U) {
			if (queryModel().indexOf("802") != -1) {
				isHH802U = true;
			} else {
				attempt++;
				Thread.sleep(200);
			}
		}
		
		if (!isHH802U) {
			serialPortConnection.close();
			throw new PowerItlException(
					portName + " is not connected to an Omega HH802U!");
		}
	}
	
	public class MeasureTask extends TimerTask {
		@SuppressWarnings("unused")
		public void run() {
			// this sort of detailed construction of the command string
			// is probably unnecessary
			char commandStartCode = '#';							// 1 byte
			int commandLength = 1 + 2 + 2 + 2 + 1 + 2;
			String commandLengthString = String.format(
					"%02x", commandLength).toUpperCase();			// 2 bytes
			String commandID = String.format("%02x", id);			// 2 bytes
			String commandChannel = String.format("%02x", channel);	// 2 bytes
			char commandCode = 'N'; 								// 1 byte
			String incompleteCommand = commandStartCode + 
					commandLengthString + commandID + commandChannel + commandCode;
			int commandChecksum = 0;
			for (int i = 0; i < incompleteCommand.length(); i++) {
				commandChecksum = commandChecksum + (int)incompleteCommand.charAt(i);
			}
			String command = 
					incompleteCommand + 
					String.format("%02x", (commandChecksum & 0x00FF)).toUpperCase() + 
					"\r\n";
						
			try {
				serialPortConnection.write(command);	
				byte[] response;
				Vector<Byte> accumResponse = new Vector<Byte>();
				
				// any valid response will have far less than 255 bytes;
				// we'll set it to this until we have the actual response
				// length as given by the accumulated response
				int reportedLength = 255;
				int byteCnt;
				
				// It's possible that the HH802U won't respond at all; 
				// don't wait more than 150ms for a full response.
				int loopCnt = 0;
				while(loopCnt < 6 && accumResponse.size() < reportedLength) {

					Thread.sleep(25);
					response = serialPortConnection.readBytes();
					
					// We can't know when the full response will come
					// in and without at least the second byte, we don't know
					// how long it'll be. If we're not careful, we might even
					// get two responses!
					// Copy bytes from response to accumResponse until we have
					// at least the second byte (which reports the response 
					// length), at which point set the reportedLength so we can
					// stop accumulating once we have enough bytes. Any extra 
					// bytes can be thrown away.
					byteCnt = 0;
					while (byteCnt < response.length && 
							accumResponse.size() < reportedLength) {
						
						accumResponse.add(response[byteCnt]);
						if (accumResponse.size() > 1) {
							// I can't simple cast response[1] to an int because if
							// response [1] > 0x80, it'll be interpreted as a negative
							// integer!
							reportedLength = (int)(accumResponse.get(1) & 0x00FF);
						}
						byteCnt++;
					}
					
					loopCnt++;
				}
				
				if (accumResponse.size() == reportedLength) {
					// this might be a valid response; parse
					// it as far as getting the checksum so that
					// can be tested
					int checksumIndex = 14;
					int responseChecksum = (int)(accumResponse.get(checksumIndex) & 0x00FF);
					
					if (checkChecksum(accumResponse, checksumIndex)) {
//						System.out.println(System.currentTimeMillis() + ": OK");
						
						// valid response!
						int responseStartCode = (int)(accumResponse.get(0) & 0x00FF);

						int responseID = (int)(accumResponse.get(2) & 0x00FF);
						int responseChannel = (int)(accumResponse.get(3) & 0x00FF);

						datagrams = new Datagram[2];
						datagrams[0] = new Datagram(accumResponse, 4);
						datagrams[1] = new Datagram(accumResponse, 9);
						
						// get the sampleTime last just in case one of the
						// datagrams parsed throws an exception
						sampleTime = System.nanoTime();
					} else {
						OmegaHH802U.this.logPane.append(
								"HH802U response failed checksum", Color.RED);
					}
				} else if (accumResponse.size() == 0) {
					// This needs to be kept in because occasionally the device will 
					// simply refuse to output! In such a case, the program will only
					// sleep 6 times, after which it'll give up on trying to get a
					// valid response
					OmegaHH802U.this.logPane.append("HH802U gave no response",
							Color.RED);
				} else {
					// There's an accumulated response, but it's somehow 
					// not the size it should be; this can happen if at two 
					// bytes were returned by the device but the rest wasn't
					// after the six 25ms loops above
					OmegaHH802U.this.logPane.append(
							"HH082 response is incorrectly sized",
							Color.RED);
				}
			} catch (IOException | InterruptedException e) {
				wLogger.warn(e.getMessage(), e);
				logPane.append(String.format("%s%n%s%n%s", 
						e.getMessage(),
						"Cancelling measurement. Closing serial port.",
						"You must reconnect to the Omega HH802U before taking more measurements."));
				shutdown();
				
				e.printStackTrace();
			} catch (DatagramException e) {
				rLogger.debug("HH802U datagram exception: " + e.getMessage());
				OmegaHH802U.this.logPane.append(
						"HH802U datagram exception: " + e.getMessage(),
						Color.RED);
			}
		}
	}
	
	/**
	 * Query the model so we can verify that we really
	 * are connecting to an Omega HH802U
	 * 
	 * @return the data returned from the instrument in response
	 * to the HH802U query command. If a non-Omega instrument is 
	 * there, it probably won't return anything!  
	 * IOException if the serialPortConnection has trouble
	 * either writing or reading
	 * @throws InterruptedException the thread sleeps between 
	 * sending messages over the serialPortConnection and reading 
	 * the response; this is thrown if the thread is interrupted
	 */
	private String queryModel() 
			throws IOException, InterruptedException {
		String startCode = "#";
		int commandLength = 1 + 2 + 2 + 2 + 1 + 2;
		String lengthString = String.format("%02x", commandLength).toUpperCase();
		String idString = String.format("%02x", id);
		String channelString = String.format("%02x", channel);
		String commandCode = "R";
		String incompleteCommand = 
				startCode + lengthString + idString + channelString + commandCode;

		String command = 
				incompleteCommand +
				calculateChecksum(incompleteCommand) +
				"\r\n";
		
		serialPortConnection.write(command);
		Thread.sleep(50);
		byte[] response = serialPortConnection.readBytes();
		char[] responseChars = new char[response.length];
		for (int i = 0; i < response.length; i++) {
			responseChars[i] = (char)response[i];
		}
		return new String(responseChars);
	}
	
	/**
	 * Check the checksum given in the response against the checksum as
	 * calculated. The checksum is calculated by adding the integer values of
	 * every byte before the checksum position and taking only the two least
	 * significant hex digits (which can be contained in one byte).
	 * This is then compared to the given checksum (which, because it is one
	 * byte in length, ranges from 0x00 to 0xFF).
	 * 
	 * @param input an array of bytes, including the checksum byte
	 * @param checksumPosition the index of the checksum byte
	 * @return if the calculated checksum is equal to the given checksum
	 */
	static private boolean checkChecksum(Vector<Byte> input, int checksumIndex) {
		int calculatedChecksum = 0;
		int givenChecksum = (int)(input.get(checksumIndex) & 0x00FF);
		for (int i = 0; i < checksumIndex; i++) {
			calculatedChecksum += (int)input.get(i);
		}

		return (calculatedChecksum & (0x00FF)) == givenChecksum;
	}
	
	/**
	 * Calculates a checksum by adding together the ASCII values of
	 * each character of the input string. This checksum is then
	 * converted to a string and the last two characters returned.
	 * 
	 * @param string
	 * @return
	 */
	static private String calculateChecksum(String string) {
		int checksum = 0;
		for (int i = 0; i < string.length(); i++) {
			checksum = checksum + (int)string.charAt(i);
		}		
		String checksumString = String.format("%02x", checksum).toUpperCase();
		return checksumString.substring(checksumString.length() - 2);
	}

	@Override
	public int getNumChannels() {
		return NUM_CHANNELS;
	}
	
	@Override
	public boolean isValid(int sensor) {
		if (sensor >= 0 && sensor < NUM_CHANNELS) {
			return (!datagrams[sensor].isBatteryLow() &&
					!Double.isInfinite(datagrams[sensor].getData()));
		} else {
			return false;
		}
	}

	@Override
	public String getUnits(int sensor) {
		if (sensor >= 0 && sensor < NUM_CHANNELS) {
			return datagrams[sensor].getUnits();
		} else {
			return null;
		}
	}

	@Override
	public double getData(int sensor) {
		if (sensor >= 0 && sensor < NUM_CHANNELS) {
			return datagrams[sensor].getData();
		} else {
			return Double.NaN;
		}
	}

	@Override
	public String getError(int sensor) {
		if (sensor >= 0 && sensor < NUM_CHANNELS) {
			if (datagrams[sensor].isBatteryLow()) {
				return "Battery is low!";
			} else if (Double.isInfinite(datagrams[sensor].getData())) {
				return "Thermocouple is disconnected";
			} else {
				return "No error";
			}	
		} else {
			return "Sensor out of bounds: must be between 0 and 1";
		}
	}

	@Override
	public long getSampleTime() {
		return sampleTime;
	}

	@Override
	public Element[] toXml(Document doc) {
		Element[] elements = new Element[NUM_CHANNELS];
		
		for (int i = 0; i < elements.length; i++) {
			elements[i] = doc.createElement("temperatureReading");
			
			// The OM-USB-TC will never take so long to return values that we have
			// to worry that the data is stale, so I've left that check out for the
			// time being.
			if (isValid(i)) {
				elements[i].setAttribute("channel", String.format("%d", i));
				elements[i].setAttribute("units", datagrams[i].getUnits());
				elements[i].appendChild(doc.createTextNode(String.format("%f", datagrams[i].getData())));
			} else {
				elements[i].appendChild(doc.createTextNode(getError(i)));
			}
		}
		
		return elements;
	}
	
	@Override
	public void startDataAcquisition() {
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
