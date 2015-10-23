package com.itl.comm.omega;

public class DatagramException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1167139990831009213L;

	public DatagramException() {
		super();
	}
	
	public DatagramException(String message) {
		super(message);
	}
	
	public DatagramException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DatagramException(Throwable cause) {
		super(cause);
	}
}
