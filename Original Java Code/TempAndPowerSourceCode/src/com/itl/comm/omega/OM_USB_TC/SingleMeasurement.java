package com.itl.comm.omega.OM_USB_TC;

import java.io.File;

import com.sun.jna.Native;
import com.sun.jna.ptr.*;

public class SingleMeasurement {
	public static final File LIBRARY_DIRECTORY = new File("c:\\foo");
	
	private static final boolean DEBUGGING_ENABLED = true;

	// JNA wrapper for the Universal Library Temperature dll
	ULTempLib ulTemperature; 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SingleMeasurement testPrototype = new SingleMeasurement();
		testPrototype.ulTemperature = createULTemperature();
		
		for (int i = 0; i < 8; i++) {
			float currentTC = -69.2f;
			int errorCode = testPrototype.readThemocouples(i, currentTC);
			System.out.printf("Sensor %02d ", i);
			if (errorCode == 0) {
				System.out.printf("Measurement: %.2f%n", currentTC);
			} else {
				System.out.printf("Error %d: %s%n", 
						errorCode, testPrototype.getLastErrorMessage(errorCode));
			}
		}
	}
	
	/**
	 * Creates an appropriate library interface
	 * @return
	 */
	public static ULTempLib createULTemperature() {
		ULTempLib ulTemperatureLib;
		
		System.out.println("Now loading library...");
		long startTime = System.currentTimeMillis();
		
		String bitModel = System.getProperty("sun.arch.data.model");
		if (bitModel.equals("64")) {
			System.load(LIBRARY_DIRECTORY.getAbsolutePath() + "\\cbw64.dll");
			ulTemperatureLib = (ULTempLib) Native.loadLibrary("cbw64", ULTempLib.class);
		} else if (bitModel.equals("32")) {
			System.load(LIBRARY_DIRECTORY.getAbsolutePath() + "\\cbw32.dll");
			ulTemperatureLib = (ULTempLib) Native.loadLibrary("cbw32", ULTempLib.class);
		} else {
			throw new RuntimeException("Unknown data model!");
		}
		
		// errors: DONTPRINT, DONTSTOP (will record and pass on instead)
		ulTemperatureLib.cbErrHandling(0, 0);
		
		if (DEBUGGING_ENABLED) {
			long stopTime = System.currentTimeMillis();
			System.out.printf("Library load complete (took %.1fs)%n", (stopTime-startTime)/1000.0);
		}
		
		return ulTemperatureLib;
	}

	/**
	 * this will read the first eight (8) thermocouples on the device
	 * and if all of them error out will return an error for the last bad ite
	 * if any returns valid data
	 *  
	 * @param sensorNumber
	 * @param tcValueInCelsius
	 * @return
	 */
	public int readThemocouples(int sensorNumber, float tcValueInCelsius) {
		int retVal = -4242;
		tcValueInCelsius = -9001;
		
		if (ulTemperature != null){
			// it's a valid object
			
			// setup reference pointer
//			FloatByReference myfloat = new FloatByReference();
//			myfloat.setValue(tcValueInCelsius);
			FloatByReference myfloat = new FloatByReference(tcValueInCelsius);
			
			// constants for reading
			final int CELSIUS = 0; 
			final int BOARD_NUM = 0;
			final int FILTER = 0;
			
			retVal = ulTemperature.cbTIn(
					BOARD_NUM, sensorNumber, CELSIUS, myfloat, FILTER);
			
			// return zee value
			tcValueInCelsius = myfloat.getValue();
			
			if (DEBUGGING_ENABLED) {
				System.out.printf("%nDebug: last TC: %.2f; errCode: %d%n",
						myfloat.getValue(), retVal);		
			}
		}
		
		return retVal;
	}
	
	/**
	 * I suspect this doesn't work because cb.GetErrMsg needs a pointer 
	 * to a String rather than a String itself
	 * @param errCode
	 * @return
	 */
	public String getLastErrorMessage(int errCode) {
		StringByReference errorString = new StringByReference(400);
//		String myAnswer = new String();
		
		ulTemperature.cbGetErrMsg(errCode, errorString);
		
		return errorString.getPointer().getString(0);
	}
}
