package com.itl.comm.omega;

import java.util.Vector;

public class Datagram {
	public static final String[] FUNCTIONS = {
		"NONE",			// Placeholder; not in 806 protocol
		"A",			// 1
		"B",			// 2
		"C",			// 3
		"D",			// 4
		"E",			// 5
		"F",			// 6
		"G",			// 7
		"H", 			// 8
		"I", 			// 9
		"J", 			// 10
		"K", 			// 11
		"L", 			// 12
		"M", 			// 13
		"N", 			// 14
		"O",			// 15
		"P",			// 16
		"Q", 			// 17
		"R", 			// 18
		"S",			// 19
		"T",			// 20
		"U", 			// 21
		"V",			// 22
		"W", 			// 23
		"X",			// 24
		"Y",			// 25
		"Z",				// 26
		"PT3926 100 ohm",	// 27
		"PT3916 100 ohm",	// 28
		"PT385 100 ohm",	// 29
		"PT385 200 ohm",	// 30
		"PT385 500 ohm",	// 31
		"PT385 1000 ohm",	// 32
		"nil20",			// 33
		"cu10", 			// 34
		"wet bulb",			// 35
		"dew point",		// 36
		"NTC",				// 37
		"R-22",				// 38
		"R-134A",			// 39
		"R-404A",			// 40
		"R-410A",			// 41
		"SC",				// 42
		"SH",				// 43
		"EFF",				// 44
		"EXCESS AIR",		// 45
		"AIR FREE",			// 46
		"O2",				// 47
		"CO",				// 48
		"CO2",				// 49
		"AMBIENT",			// 50
		"STACK",			// 51
		"FUEL",				// 52
		"Natural Gas",		// 53
		"Oil#2",			// 54
		"Propane",			// 55
		"Pressure",			// 56
		"Velocity",			// 57
		"Flow",				// 58
		"Vacuum Gauge",		// 59
		"INRUSH",			// 60
		"CH",				// 61
		"LOOP",				// 62
		"THD,V",			// 63
		"THD-R,V",			// 64
		"THD-F,V",			// 65
		"THD,A",			// 66
		"THD-R,A",			// 67
		"THD-F,A",			// 68
		"R,120HZ",			// 69
		"R,1KHZ",			// 70
		"C,120HZ,SER",		// 71
		"C,120HZ,PAL",		// 72
		"C,1KHZ,SER",		// 73
		"C,1KHZ,PAL",		// 74
		"L,120HZ,SER",		// 75
		"L,120HZ,PAL",		// 76
		"L,1KHZ,SER",		// 77
		"L,1KHZ,PAL",		// 78
		"A,FAST",			// 79
		"A,SLOW",			// 80
		"C,FAST",			// 81
		"C,SLOW",			// 82
		"1phi 3W,L1",		// 83
		"1phi 3W,L2",		// 84
		"1phi 3W",			// 85
		"1phi 2W",			// 86
		"3phi 3W,L1",		// 87
		"3phi 3W,L2",		// 88
		"3phi 3W",			// 89
		"3phi 4W,L1",		// 90
		"3phi 3W,L1",		// 91
		"3phi 3W,L2",		// 92
		"3phi 3W",			// 93
		"3phi 4W,L2",		// 94
		"3phi 4W,L3",		// 95
		"3phi 4W"			// 96
	};
	public static final String[] UNITS = {
		"NONE",		// Placeholder; not in 806 protocol
		"degC",	// 1
		"degF",	// 2
		"K",		// 3 
		"uA",		// 4
		"mA",		// 5
		"A",		// 6
		"uV",		// 7
		"mV",		// 8
		"V",		// 9
		"ohm",		// 10
		"K-ohm",	// 11
		"M-ohm",	// 12
		"pF",		// 13
		"nF",		// 14
		"uF",		// 15
		"mF",		// 16
		"F",		// 17
		"uH",		// 18
		"mH",		// 19
		"H",		// 20
		"Hz",		// 21
		"KHz",		// 22
		"MHz",		// 23
		"GHz",		// 24
		"dB m",		// 25
		"dB", 		// 26
		"% RH",		// 27
		"%",		// 28
		"psi",		// 29
		"kpa",		// 30
		"inwc",		// 31
		"mmwc",		// 32
		"mbar",		// 33
		"lux",		// 34
		"fc",		// 35
		"kfc",		// 36
		"klux",		// 37
		"m/s",		// 38
		"ft/min",	// 39
		"knots",	// 40
		"mph",		// 41
		"km/h",		// 42
		"mmAq",		// 43
		"cmAq",		// 44
		"umHg",		// 45
		"imHg",		// 46
		"mmHg",		// 47
		"cmHg",		// 48
		"uw/cm2",	// 49
		"mw/cm2",	// 50
		"mils",		// 51
		"um",		// 52
		"cm",		// 53
		"mm",		// 54
		"in",		// 55
		"g",		// 56
		"kg",		// 57
		"inH2O",	// 58
		"mmH2O",	// 59
		"cmH2O",	// 60
		"KW",		// 61
		"KVAR",		// 62
		"KVA",		// 63
		"PF",		// 64
		"Pa",		// 65
		"inH2O",	// 66
		"mmH2O",	// 67
		"mb",		// 68
		"PSI",		// 69
		"fpm",		// 70
		"m/s",		// 71
		"cfm",		// 72
		"m3/hr",	// 73
		"l/s",		// 74
		"pH",		// 75
		"Kg",		// 76
		"bls",		// 77
		"oz",		// 78
		"dB",		// 79
		"OPR",		// 80
		"ms"		// 81
	};
	public static final int RAW_BYTE_LENGTH = 5;
	
	private boolean isBatteryLow;
	private double data;
	private String function;
	private String units;
	
	/**
	 * Given a relevant subset of bytes, parse them
	 * @param rawBytes
	 * @throws DatagramException
	 */
	public Datagram(byte[] rawBytes) throws DatagramException {
		if (rawBytes.length == RAW_BYTE_LENGTH) {
			parseRawBytes(rawBytes);
		} else {
			throw new DatagramException("Not an appropriately sized datagram");
		}
	}
	
	/**
	 * Given the complete response and an index for which 
	 * the relevant datagram starts, grab the relevant subset
	 * of bytes and parse them
	 * @param rawBytes
	 * @param startIndex
	 * @throws DatagramException
	 */
	public Datagram(Vector<Byte> rawBytes, int startIndex) 
			throws DatagramException {
		
		if (rawBytes.size() >= (startIndex + RAW_BYTE_LENGTH)) {
			byte[] rawSubset = new byte[RAW_BYTE_LENGTH];
			for (int i = 0; i < RAW_BYTE_LENGTH; i++) {
				rawSubset[i] = rawBytes.get(startIndex + i);
			}
			parseRawBytes(rawSubset);
		} else {
			throw new DatagramException("Input byte array is not long enough");
		}
	}
	
	/**
	 * Given the complete response and an index for which 
	 * the relevant datagram starts, grab the relevant subset
	 * of bytes and parse them
	 * @param rawBytes
	 * @param startIndex
	 * @throws DatagramException
	 */
	public Datagram(byte[] rawBytes, int startIndex) 
			throws DatagramException {
		
		if (rawBytes.length >= (startIndex + RAW_BYTE_LENGTH)) {
			byte[] rawSubset = new byte[RAW_BYTE_LENGTH];
			for (int i = 0; i < RAW_BYTE_LENGTH; i++) {
				rawSubset[i] = rawBytes[startIndex + i];
			}
			parseRawBytes(rawSubset);
		} else {
			throw new DatagramException("Input byte array is not long enough");
		}
	}
	
	/**
	 * Parse an array of bytes into a single Datagram
	 * @param rawBytes
	 * @throws DatagramException if the function code or units code
	 * is out of bounds
	 */
	private void parseRawBytes(byte[] rawBytes) throws DatagramException {
		isBatteryLow = (rawBytes[0] & 0x80) == 0x80;
		int decimalPointPosition = (rawBytes[0] & 0x70) >>> 4;
		String dataString = String.format(
				"%1x%02x%02x", (rawBytes[0] & 0x0F), rawBytes[1], rawBytes[2]);
		//+OL is 7FFFF, -OL is 80000
		if (dataString.equalsIgnoreCase("7FFFF")) {
			data = Double.POSITIVE_INFINITY;
		} else if (dataString.equalsIgnoreCase("80000")) {
			data = Double.NEGATIVE_INFINITY;
		} else {
			data = ((double)Integer.parseInt(dataString, 16))/
					Math.pow(10, decimalPointPosition);
		}
		
		int functionIndex = (int)(rawBytes[3] & 0x00FF);
		if (functionIndex < Datagram.FUNCTIONS.length) {
			function = Datagram.FUNCTIONS[functionIndex];
		} else {
			throw new DatagramException("Function code is out of bounds!");
		}
		
		int unitsIndex = (int)(rawBytes[4] & 0x00FF);
		if (unitsIndex < Datagram.UNITS.length) {
			units = Datagram.UNITS[(int)(rawBytes[4] & 0x00FF)];
		} else {
			throw new DatagramException("Units code is out of bounds!");
		}
		
	}
	
	public boolean isBatteryLow() {
		return isBatteryLow;
	}
	
	public double getData() {
		return data;
	}
	
	public String getFunction() {
		return function;
	}
	
	public String getUnits() {
		return units;
	}
	
	public String toString() {
		return String.format("isBatterLow = %b, function = %s, data = %s, units = %s",
				isBatteryLow, function, ((Double)data).toString(), units);
	}
}
