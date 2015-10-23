package com.itl.comm;

/**
 * Exceptions specific to serial ports
 * @author kgraba
 *
 */
public class SerialPortException extends Exception {
	private static final long serialVersionUID = -1485458882923568014L;

	public SerialPortException() {
		super();
	}
	
	public SerialPortException(String message) {
		super(message);
	}
	
	public SerialPortException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SerialPortException(Throwable cause) {
		super(cause);
	}
}
