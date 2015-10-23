package com.itl.comm.omega.OM_USB_TC;

import java.awt.Color;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itl.comm.ThermocoupleDaq;
import com.itl.comm.powerItl.PowerItlException;
import com.itl.swing.LogTextPane;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * This class is responsible for managing the OM-USB-TC instrument, made by 
 * Measurement Computing and sold by Omega. In order for this to work on a 
 * particular computer, InstaCal must have been installed first (to copy the 
 * library files to the computer) and InstaCal must have been run at least once 
 * (so that CB.CFG has been created, a prerequisite for using the library 
 * files). 
 * 
 * @author kgraba
 *
 */
public class OM_USB_TC implements ThermocoupleDaq {
	/** 
	 * rLogger (root logger) has a single appender to write to a file.
	 * This will be used to write debug and info messages.
	 * Since it's the root logger (from which every other
	 * logger is descended), it'll also get whatever we send
	 * to the warning logger.
	 */
	Logger rLogger = Logger.getLogger(OM_USB_TC.class);
	
	/**
	 * The warning logger has a single appender to write
	 * to the console. It's just for warnings and errors.
	 */
	Logger wLogger = Logger.getLogger("consoleLogger");
	
	/** 
	 * The directory that holds the .dlls necessary to interface
	 * with the instrument. This directory is set when InstaCal
	 * is installed.
	 * <p>
	 * This is a member just in case we ever need to grab other
	 * .dll files from this directory.
	 */
	File rootDir;
	
	/** 
	 * The library file used to interface with the instrument. 
	 * It's found inside the rootDir and which file is used 
	 * (cbw64.dll or cbw32.dll) is determined by the architecture
	 * of the machine the program is running on.
	 */
	File libraryFile;
	
	/**
	 * The directory that holds CB.CFG, the configuration file 
	 * created by InstaCal. Note that this file must be present
	 * before the library files can be used to interface with
	 * the OM-USB-TC!
	 * <p>
	 * This is a member just in case we ever get the chance to
	 * create our own configuration files, or in case we ever
	 * want to use more than one configuration file.
	 */
	File configDir;
	
	/** JNA wrapper for the Universal Library Temperature dll */
	ULTempLib ulTemperature; 
	
	/** The board number as given by InstaCal */
	int board;
	
	Timer measuringTimer;
	
	public static final int NUM_CHANNELS = 8;
	
	double[] readings;
	int[] errCodes;
	
	private boolean isConnected;
	
	/** 
	 * the last time the device sent back data (even if it 
	 * indicated an error doing so)
	 */
	long sampleTime;
	
	/** the GUI element that'll display important notes to the user */
	LogTextPane logPane;
	
	public OM_USB_TC(Element thermocoupleDaq, LogTextPane logPane) 
			throws PowerItlException {
		this.logPane = logPane;
		
		// there are 8 temperature sensors on the OM-USB-TC
		readings = new double[8];
		errCodes = new int[8];
		
		board = Integer.parseInt(
				thermocoupleDaq.getElementsByTagName("board").
				item(0).getTextContent());
		
		// Since loading the library can take a while, put it in its own 
		// thread. Naturally, we can only start the MeasureTask after 
		// everything's been loaded
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					connect();
					isConnected = true;
					
					startDataAcquisition();
				} catch (PowerItlException e) {
					isConnected = false;
					
					rLogger.error(e.getMessage(), e);
					OM_USB_TC.this.logPane.append(e.getMessage(), Color.RED);
					
					// Because the runnable cannot throw an exception
					// that can be caught be the calling program, just
					// end things now? We don't want to call System.exit(),
					// since that may leave other resources unclosed.
					OM_USB_TC.this.shutdown();
				} catch (Win32Exception e) {
					isConnected = false;
					
					// if the registry values could not be found, then catch the 
					// original exception (so it can be logged) and throw an
					// error that
					rLogger.error(e.getMessage(), e);
					
					// the Win32Exception message isn't terribly helpful to a
					// non-programmer, so just make up a slightly more informative
					// message
					OM_USB_TC.this.logPane.append("Universal Library registry " +
							"key could not be found. The OM-USB-TC has not been " +
							"connected.", Color.RED);
					
					// Because the runnable cannot throw an exception
					// that can be caught be the calling program, just
					// end things now? We don't want to call System.exit(),
					// since that may leave other resources unclosed.
					OM_USB_TC.this.shutdown();
				}
			}
		});
		
		t.start();
	}
	
	/**
	 * This method doesn't handle connection per se, but rather
	 * sets up the ULTemperature interface which handles the
	 * connection. It does, however, check to make sure that the 
	 * board number given is actually an OM-USB-TC. As such, it's 
	 * analogous to the connect methods of other instrument 
	 * classes.
	 * 
	 * @throws PowerItlException if the board specified is not 
	 * actually an Omega OM-USB-TC
	 */
	public void connect() 
			throws PowerItlException, Win32Exception {

		rLogger.info("Now loading Universal Library.");
		logPane.append("Now loading Universal Library.");
		long startTime = System.currentTimeMillis();
		
		String bitModel = System.getProperty("sun.arch.data.model");
		
		// InstaCal (and its installer, of course) is 32-bit. So
		// if this is on a 64-bit machine, its registry settings 
		// and installation directory will have been subject to the
		// 64/32 bit node redirect.
		// This program can't rely on the same thing because it's
		// most likely that on a 64-bit machine, the JVM will also
		// be the 64-bit version and so not subject to the 64/32 bit
		// redirect.
		
		HKEY regRoot = WinReg.HKEY_LOCAL_MACHINE;
		String ulKeyPath;
		String libraryName;
		
		if (bitModel.equals("64")) {
			ulKeyPath = "Software\\Wow6432Node\\Universal Library";
			libraryName = "cbw64";
		} else if (bitModel.equals("32")) {
			ulKeyPath = "Software\\Universal Library";
			libraryName = "cbw32";
		} else {
			throw new RuntimeException("Unknown data model!");
		}
		
		rLogger.debug(String.format("Getting registry key %s%s", "HKLM:\\", ulKeyPath));
		
		if (Advapi32Util.registryValueExists(regRoot, ulKeyPath, "ConfigDir")) {
			// It turns out that the ConfigDir value is created only 
			// for machines running Windows 7 or better.
			// The native libraries will throw an exception if 
			// CB.CFG isn't found wherever they expect to find
			// it.
			
			configDir = new File(Advapi32Util.registryGetStringValue(
					regRoot, ulKeyPath, "ConfigDir"));
			rLogger.debug("Universal Library Config Dir: " + configDir);
			File configFile = new File(configDir, "CB.CFG");
			if (!configFile.exists()) {
				// If CB.CFG has not been created, the native libraries will
				// throw an exception which JNA will pass on. I'm not sure
				// what sort of costs are associated with that and they're
				// not terribly descriptive, so that's why I'm throwing our
				// own exception here.
				throw new PowerItlException(
						"Universal Library configuration file does not exist.");
			}
		}
		

		rootDir = new File(Advapi32Util.registryGetStringValue(
				regRoot, ulKeyPath, "RootDir"));
		libraryFile = new File(rootDir, libraryName + ".dll");

		System.load(libraryFile.getAbsolutePath());

		ulTemperature = (ULTempLib) Native.loadLibrary(
				libraryFile.getName().substring(0, libraryFile.getName().lastIndexOf('.')), 
				ULTempLib.class);

		ulTemperature.cbErrHandling(
				ULTempLib.DONTPRINT, ULTempLib.DONTSTOP);

		if (!getBoardName(board).equals("USB-TC")) {
			throw new PowerItlException(
					"Board " + board + " is not an Omega OM-USB-TC!");
		}
		
		long stopTime = System.currentTimeMillis();
		
		logPane.append("Universal Library loaded.", Color.BLACK);
		rLogger.info(String.format(
				"Universal Library loaded; took %.1fs", 
				(stopTime-startTime)/1000.0));
	}

	private class MeasureTask extends TimerTask {
		public void run() {
			for (int i = 0; i < readings.length; i++) {
				FloatByReference reading = new FloatByReference();
				errCodes[i] = ulTemperature.cbTIn(board, i, 
						ULTempLib.CELSIUS, reading, 
						ULTempLib.NOFILTER);
				
				sampleTime = System.nanoTime();
				if (errCodes[i] == 0) {
					readings[i] = reading.getValue();
				}
			}
		}
	}
	
	public int getConfig(int infoType, int boardNum, int devNum,
			int configItem) {
		IntByReference configVal = new IntByReference();
		
		return configVal.getValue();
	}
	
	public String getErrorMessage(int errCode) {
		// ERRSTRLEN = 256
		StringByReference errorString = new StringByReference(
				ULTempLib.ERRSTRLEN);
		
		ulTemperature.cbGetErrMsg(errCode, errorString);
		
		return errorString.getPointer().getString(0);
	}
	
	public String getBoardName(int boardNum) {
		// BOARDNAMELEN = 25
		StringByReference boardName = new StringByReference(
				ULTempLib.BOARDNAMELEN);
		
		ulTemperature.cbGetBoardName(boardNum, boardName);
		
		return boardName.getPointer().getString(0);
	}
	
	@Override
	public int getNumChannels() {
		return NUM_CHANNELS;
	}
	
	@Override
	public boolean isValid(int sensor) {
		if (sensor >= 0 && sensor < NUM_CHANNELS) {
			return (errCodes[sensor] == 0);
		} else {
			return false;
		}
	}

	@Override
	public String getUnits(int sensor) {
		return "deg C";
	}

	@Override
	public double getData(int sensor) {
		return readings[sensor];
	}

	@Override
	public String getError(int sensor) {
		if (sensor >= 0 && sensor < NUM_CHANNELS) {
			return getErrorMessage(errCodes[sensor]);
		} else {
			return "Sensor out of bounds: must be between 0 and 7";
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
			
			if (isValid(i)) {
				elements[i].setAttribute("channel", String.format("%d", i));
				elements[i].setAttribute("units", "deg C");
				elements[i].appendChild(doc.createTextNode(String.format("%f", readings[i])));
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
			measuringTimer.schedule(new MeasureTask(), 0, 200);
		}
	}
	
	@Override
	public void stopDataAcquisition() {
		if (measuringTimer != null) {
			measuringTimer.cancel();
		}
	}
	
	/**
	 * In this case, we don't have to worry about directly
	 * dealing with a serial port connection, we just have to
	 * ensure that the measuring timer task no longer runs.
	 */
	@Override
	public void shutdown() {
		stopDataAcquisition();
	}
}
