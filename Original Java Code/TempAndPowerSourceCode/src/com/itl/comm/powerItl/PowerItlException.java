package com.itl.comm.powerItl;

/**
 * Exceptions specific to PowerItl
 * @author kgraba
 *
 */
public class PowerItlException extends Exception {
	private static final long serialVersionUID = -4469273996705368893L;

	public PowerItlException() {
		super();
	}
	
	public PowerItlException(String message) {
		super(message);
	}
	
	public PowerItlException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PowerItlException(Throwable cause) {
		super(cause);
	}
}
