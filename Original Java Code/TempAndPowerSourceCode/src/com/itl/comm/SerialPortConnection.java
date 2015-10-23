package com.itl.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

//TODO: make the reading function more robust
/**
 * A basic class for connecting to serial ports using gnu.io classes
 * to identify, enumerate, connect to, write to, read from, and close
 * connections.
 * @author kgraba
 *
 */
@SuppressWarnings("unused")
public class SerialPortConnection {
	public final static int DEFAULT_BAUD_RATE = 19200;
	public final static int DEFAULT_DATA_BITS = 8;
	public final static int DEFAULT_STOP_BITS = 1;
	public final static int DEFAULT_PARITY = SerialPort.PARITY_EVEN;
	
	public String portName;
	private int baudRate;
	private int dataBits;
	private int stopBits;
	private int parity;
	
	public CommPort commPort;
	private InputStream in;
	private OutputStream out;
	
	public SerialPortConnection (String portName) throws Exception {
		this(
			portName, 
			DEFAULT_BAUD_RATE, 
			DEFAULT_DATA_BITS, 
			DEFAULT_STOP_BITS, 
			DEFAULT_PARITY);
	}
	
	public SerialPortConnection (
			String portName, int baudRate, int dataBits, int stopBits, int parity) 
					throws SerialPortException, IOException {
		
		this.portName = portName.toUpperCase();
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		
		CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			
			if (!portIdentifier.isCurrentlyOwned()) {
				commPort = portIdentifier.open(
						this.getClass().getName(),2000);

				if (commPort instanceof SerialPort) {
					SerialPort serialPort = (SerialPort)commPort;
					serialPort.setSerialPortParams(
							baudRate, dataBits, stopBits, parity);

					in = serialPort.getInputStream();
					out = serialPort.getOutputStream();	
				} else {
					throw new SerialPortException("Only serial ports are handled!");
				}
			} else {
				throw new SerialPortException("Port " + portName + " is currently in use.");
			}
		} catch (NoSuchPortException e) {
			// It's somewhat lazy to simply catch and 
			// re-throw exceptions like this, but it's the
			// easiest way to collect all of the serial-port
			// related exceptions without catching EVERY
			// exception!
			throw new SerialPortException("Port " + portName + " does not exist!");
		} catch (PortInUseException e) {
			// This really should be caught by the conditional above.
			// But just in case it's not...
			throw new SerialPortException("Port " + portName + " is currently in use!");
		} catch (UnsupportedCommOperationException e) {
			// see above for why this is lazy and why I'm
			// doing it anyway
			throw new SerialPortException(e.getMessage());
		}
	}

	/**
	 * Closes the comm port 
	 */
	public void close() {
		commPort.close();
	}
	
	/** 
	 * Takes a string and sends it out over the comm port
	 */
	public void write(String input) throws IOException {
		out.write((input).getBytes());
	}
	
	/** 
	 * Reads from the comm port until the port has no more 
	 * data in chunks that are at most 1024 bytes long.  That
	 * data is then converted into a string.<p>
	 * Note that there is no delay here.  In the case of 
	 * instruments that are taking data, it may be necessary 
	 * to insert a short delay in the thread to give the 
	 * instrument time to respond usefully.
	 */
	public String read() throws IOException {
		int length;
		byte[] readBuffer = new byte[1024];
		StringBuffer stringBuffer = new StringBuffer();
		
		while ((length = in.read(readBuffer)) > 0) {
			stringBuffer.append(new String(readBuffer, 0, length));
		}
		
		return stringBuffer.toString();
	}
	
	/**
	 * Reads a maximum of 1024 bytes from the comm port
	 * but returns an array of bytes no longer than necessary. 
	 * @return
	 * @throws IOException
	 */
	public byte[] readBytes() throws IOException {
		int length;
		byte[] readBuffer = new byte[1024];
		byte[] output = new byte[0];

		length = in.read(readBuffer);
		output = new byte[length];
		for (int i = 0; i < length; i++) {
			output[i] = readBuffer[i];
		}
		
		return output;
	}
}
