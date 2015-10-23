package com.itl.comm;

import java.util.Enumeration;

import gnu.io.CommPortIdentifier;

@SuppressWarnings("unchecked")
public class ListPorts {
	// test comment - remove!
	
	public static void main(String[] args) {
		CommPortIdentifier portIdentifier;
		Enumeration<CommPortIdentifier> portEnum = 
			CommPortIdentifier.getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			portIdentifier = portEnum.nextElement();
			System.out.println(portIdentifier.getName() + " - " +
					getPortTypeName(portIdentifier.getPortType()));
		}
	}
	
	private static String getPortTypeName (int portType) {
		switch (portType) {
			case CommPortIdentifier.PORT_I2C:
	            return "I2C";
	        case CommPortIdentifier.PORT_PARALLEL:
	            return "Parallel";
	        case CommPortIdentifier.PORT_RAW:
	            return "Raw";
	        case CommPortIdentifier.PORT_RS485:
	            return "RS485";
	        case CommPortIdentifier.PORT_SERIAL:
	            return "Serial";
	        default:
	            return "unknown type";
		}
	}
}
