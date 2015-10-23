package com.itl.comm.omega.OM_USB_TC;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.jna.Native;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;

@SuppressWarnings("serial")
public class LoopFrontend extends JFrame implements ActionListener {
	public static final File LIBRARY_DIRECTORY = new File("c:\\foo");
	
	private static final boolean DEBUGGING_ENABLED = true;

	/** JNA wrapper for the Universal Library Temperature dll */
	ULTempLib ulTemperature; 
	
	Timer measuringTimer;
	
	int[] errCodes;
	FloatByReference[] readings;
	
	JFormattedTextField[] tempFields;
	JButton startReading;
	
	public LoopFrontend() {
		ulTemperature = createULTemperature();
		
		System.out.println("Board name: " + getBoardName(0));
		System.out.println("Config info: " + getConfig(
				2, // = BOARDINFO
				0, // boardNum
				0, // devNum
				208)); // = BINUMTEMPCHANS
		
		// initialize data arrays
		errCodes = new int[8];
		readings = new FloatByReference[8];
		for (int i = 0; i < 8; i++) {
			readings[i] = new FloatByReference();
		}
		
		// initialize elements
		tempFields = new JFormattedTextField[8];
		for (int i = 0; i < tempFields.length; i++) {
			tempFields[i] = new JFormattedTextField();
			tempFields[i].setColumns(8);
			tempFields[i].setEditable(false);
		}

		startReading = new JButton("Start Reading");
		startReading.addActionListener(this);

		// place elements
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.weighty = 0;

		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		p.add(startReading, c);

		c.gridwidth = 1;
		c.gridheight = 1;
		for (int i = 0; i < tempFields.length; i++) {
			c.gridy = i + 1;

			c.gridx = 0;
			c.anchor = GridBagConstraints.LINE_START;
			p.add(new JLabel("Channel " + i), c);
			c.gridx = 1;
			p.add(tempFields[i], c);
		}

		this.setContentPane(p);
		this.setTitle("OM-USB-TC Frontend");

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (measuringTimer != null) {
					measuringTimer.cancel();
				}

				System.exit(0);
			}
		});

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setLocation(50, 50);
		this.pack();
		this.setVisible(true);
	}
	
	public int readThermocouple(int channel, FloatByReference reading) {
		// constants for reading
		final int boardNum = 0;
		int errorCode = ulTemperature.cbTIn(boardNum, channel, 
				ULTempLib.CELSIUS, reading, ULTempLib.NOFILTER);
		
//		return reading.getValue();
		return errorCode;
	}
	
	/**
	 * Creates an appropriate library interface
	 * @return
	 */
	public static ULTempLib createULTemperature() {
		ULTempLib ulTemperatureLib;
		
		if (DEBUGGING_ENABLED) {
			System.out.println("Now loading library...");
		}
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
		ulTemperatureLib.cbErrHandling(
				ULTempLib.DONTPRINT, ULTempLib.DONTSTOP);
		
		long stopTime = System.currentTimeMillis();
		if (DEBUGGING_ENABLED) {
			System.out.printf("Library load complete (took %.1fs)%n", (stopTime-startTime)/1000.0);
		}
		
		return ulTemperatureLib;
	}
	
	public int getConfig(int infoType, int boardNum, int devNum,
			int configItem) {
		IntByReference configVal = new IntByReference();
		
		int error = ulTemperature.cbGetConfig(infoType, boardNum, devNum, 
				configItem, configVal);
		System.out.println("Error: " + error + " (" + getErrorMessage(error) + ")");
		return configVal.getValue();
	}
	
	/**
	 * Given an error code, return a slightly more usable error message
	 * @param errCode
	 * @return
	 */
	public String getErrorMessage(int errCode) {
		// I wonder if 400 bytes is long enough?
		StringByReference errorString = new StringByReference(400);
		
		ulTemperature.cbGetErrMsg(errCode, errorString);
		
		return errorString.getPointer().getString(0);
	}
	
	public String getBoardName(int boardNum) {
		// I wonder if 400 bytes is long enough?
		StringByReference boardName = new StringByReference(400);
		
		ulTemperature.cbGetBoardName(boardNum, boardName);
		
		return boardName.getPointer().getString(0);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@SuppressWarnings("unused")
			public void run() {
				LoopFrontend frontend = new LoopFrontend();
			}
		});
	}

	public void actionPerformed(ActionEvent evt) {
		if (measuringTimer != null) {
			measuringTimer.cancel();
		}
		measuringTimer = new Timer();
		measuringTimer.schedule(new MeasureTask(), 0, 500);
	}
	
	private class MeasureTask extends TimerTask {
		public void run() {
			for (int i = 0; i < tempFields.length; i++) {
				readings[i] = new FloatByReference();
				errCodes[i] = readThermocouple(i, readings[i]);
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for (int i = 0; i < tempFields.length; i++) {
						if (errCodes[i] == 0) {
							tempFields[i].setText(String.format("%.2f %sC", 
									readings[i].getValue(), (char)176));
							tempFields[i].setToolTipText(null);
						} else {
							tempFields[i].setText(String.format("Error %d", errCodes[i]));
							tempFields[i].setToolTipText(getErrorMessage(errCodes[i]));
						}
					}
				}
			});
		}
	}
}
