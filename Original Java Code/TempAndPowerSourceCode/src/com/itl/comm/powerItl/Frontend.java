package com.itl.comm.powerItl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.itl.comm.PowerMeter;
import com.itl.comm.SerialPortException;
import com.itl.comm.ThermocoupleDaq;
import com.itl.comm.chroma66200.Chroma66200;
import com.itl.comm.omega.OM_USB_TC.OM_USB_TC;
import com.itl.comm.omega.hh802u.OmegaHH802U;
import com.itl.comm.prologix.WT210.YokogawaWT210;
import com.itl.comm.yokogawa.WT330.WT330;
import com.itl.swing.LogTextPane;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("serial")
public class Frontend extends JFrame implements ActionListener {
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
	
	/** the SimpleDateFormat used in the logPane */
	private static final SimpleDateFormat SDF = 
			new SimpleDateFormat("HH:mm:ss");
	
	/**
	 * This is an XML file containing settings information
	 * for which (if any) power meter to use and which (if
	 * and) thermocouple DAQ to use, as well as information
	 * pertinent to the particular instruments.
	 * <p>
	 * This is a member instead of being entirely scoped 
	 * inside the readSettings method because I might decide
	 * to make it possible to run this program with different
	 * settings names, as determined by a command-line 
	 * argument.
	 * 
	 * @see PowerItl.xsd
	 */
	private File settingsFile;
	
	/** The PowerMeter object */
	PowerMeter pm;
	
	/** The ThermocoupleDaq object */
	ThermocoupleDaq tcd;
	
	// Server stuff
	int serverPort;
	HttpServer server;
	PowerItlHandler handler;
	
	/** the GUI element that'll display important notes to the user */
	LogTextPane logPane;
	
	/** 2015-03-24: Randy requested a pause button */
	JButton pauseButton;
	
	/** 
	 * 2015-03-24: Rather than check the power meter and thermocouple daq to 
	 * see if their measuringTimers are running to get the paused/unpaused 
	 * state, we'll use this boolean to track the state. This has the advantage
	 * of not having to worry about possible timing issues or the possibility
	 * that the various instruments may not be running their measuringTimers.
	 */
	boolean isRunning;
	
	/**
	 * Initialize the frontend. Right now, the frontend simply has
	 * a text pane on it for informing the user of important messages
	 * (as well as the usual buttons for minimizing or closing the
	 * frontend, if you want to count those).
	 */
	public Frontend() {
		rLogger.info(String.format("Creating new %s", Frontend.class.getName()));
		
		// initialize and place the log so that we can display 
		// start-up messages on it
		logPane = new LogTextPane(SDF);
		logPane.setEditable(true);
		logPane.setFocusable(false);
		JScrollPane logView = new JScrollPane(
				logPane,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logView.setPreferredSize(new Dimension(525, 150));
		
		pauseButton = new JButton("Pause");
		pauseButton.addActionListener(this);
		
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		p.add(logView, c);
		
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.PAGE_END;
		c.gridy = 1;
		p.add(pauseButton, c);
		
		this.setContentPane(p);
		
		// basic house-keeping
		this.setTitle("Temperature and Power DAQ");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.pack();
		this.setMinimumSize(this.getPreferredSize());
		//this.setExtendedState(Frame.ICONIFIED); // Starts minimized
		this.setVisible(true);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				rLogger.info("Window Closing");
				logPane.append("user-initiated shut-down", Color.BLACK);
				shutdown();
				
				System.exit(0);
			}
		});
		
		try {
			readSettings();
		} catch (ParserConfigurationException | SAXException | IOException | PowerItlException | 
				SerialPortException | InterruptedException | NullPointerException e) {
			wLogger.fatal(
					"Could not read the settings file: " + e.getMessage(), e);
			logPane.append(
					"Could not read the settings file: " + e.getMessage(), Color.RED);
			
			// Any of these exceptions are serious issues which
			// threaten the integrity of the whole program. If
			// any of them are encountered, ensure that any 
			// connections that were created are closed and 
			// shutdown as soon as possible.
			shutdown();
			System.exit(1);
		}
		
		if (pm != null) {
			// start its measuringTimer (use its logPane to send message!)
			//logPane.append("Now acquiring data from power meter.", Color.BLACK);
		}
		
		if (tcd != null) {
			// start its measuringTimer (use its logPane to send message!)
			//logPane.append("Now acquiring data from thermocouple DAQ.", Color.BLACK);
		}
		
		isRunning = true;
		
		try {
			initializeServer();
		} catch (ParserConfigurationException | IOException e) {
			wLogger.fatal("Could not initialize the server: " + e.getMessage(), e);
			logPane.append("Could not initialize the server: " + e.getMessage(), Color.RED);
			
			shutdown();
			System.exit(1);
		}
	}
	
	/**
	 * Checks to see if the PowerMeter and ThermocoupleDaq 
	 * have been initialized and if so, invokes their shutdown
	 * methods to ensure that no threads continue to sample data 
	 * and that COM ports are properly freed up.
	 * <p>
	 * This is its own method because there are a few situations
	 * in which we want to be really, really sure that the
	 * resources are released.
	 */
	private void shutdown() {
		if (pm != null) {
			pm.shutdown();
		}
		  
		if (tcd != null) {
			tcd.shutdown();
		}
		
		if (server != null) {
			server.stop(0);
		}
	}
	
	/**
	 * Read the settings file and try to create appropriate powerMeter
	 * and thermocoupleDaq objects.
	 * 
	 * @throws ParserConfigurationException if the XML config file 
	 * cannot be properly parsed
	 * @throws SAXException if the XML config file 
	 * cannot be properly parsed
	 * @throws IOException if something's wrong with the XML config
	 * file, or if any of the communication ports can't be set up
	 * properly, or if initializeServer() fails
	 * @throws PowerItlException if there's a specific failure among
	 * one of the components or settings (e.g., if COM3 works but is
	 * not connected to the meter named in the config file)
	 * @throws InterruptedException 
	 * @throws SerialPortException 
	 */
	private void readSettings() 
			throws ParserConfigurationException, SAXException, IOException, 
			PowerItlException, SerialPortException, InterruptedException,
			NullPointerException {
		
		settingsFile = new File(".\\TempAndPowerSettings.xml");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document doc = builder.parse(settingsFile);
		Element root = doc.getDocumentElement();
		
		Element pmConfig = 
				(Element) root.getElementsByTagName("powerMeter").item(0);
		String pmType = pmConfig.getAttribute("type");
		if (pmType.equalsIgnoreCase("Yokogawa WT210")) {
			pm = new YokogawaWT210(pmConfig, logPane);
		} else if (pmType.equalsIgnoreCase("Chroma 66200")) {
			pm = new Chroma66200(pmConfig, logPane);
		} else if (pmType.equalsIgnoreCase("Yokogawa WT330")) {
			pm = new WT330(pmConfig, logPane);
		} else if (pmType.equalsIgnoreCase("Null")) {
			pm = null;
		} else {
			throw new PowerItlException("powerMeter type is unrecognized.");
		}
		
		Element tcdConfig = 
				(Element) root.getElementsByTagName("thermocoupleDaq").item(0);
		String tcdType = tcdConfig.getAttribute("type");
		if (tcdType.equalsIgnoreCase("Omega HH802U")) {
			tcd = new OmegaHH802U(tcdConfig, logPane);
		} else if (tcdType.equalsIgnoreCase("Omega OM-USB-TC")) {
			tcd = new OM_USB_TC(tcdConfig, logPane);
		} else if (tcdType.equalsIgnoreCase("Null")) {
			tcd = null;
		} else {
			throw new PowerItlException("thermocoupleDaq type is unrecognized.");
		}
		
		Element servlet = (Element) root.getElementsByTagName("servlet").item(0);
		serverPort = Integer.parseInt(servlet.getElementsByTagName("port").item(0).getTextContent());
	}
	
	/**
	 * Initialize our HTTP server
	 * 
	 * @throws IOException if the InetSocketAddress throws an IOException
	 * @throws ParserConfigurationException if the handler can't be created for
	 * some crazy reason. 
	 */
	private void initializeServer() 
			throws IOException, ParserConfigurationException {
		handler = new PowerItlHandler(SDF, pm, tcd);
		
		// Create the address using a hostname so we have a convenient way of 
		// referring to it.
		String hostname = Advapi32Util.registryGetStringValue(
				WinReg.HKEY_LOCAL_MACHINE, 
				"SYSTEM\\CurrentControlSet\\Control\\ComputerName\\ComputerName", 
				"ComputerName");
		InetSocketAddress address = new InetSocketAddress(
				hostname, serverPort);
		server = HttpServer.create(address, 0);
		
		// For our needs, we only need a single context: /daq for convenience.
		String context = "/daq";
		
		server.createContext(context, handler);
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		
		String message = String.format(
				"Server has started and is operating on http://%s:%d%s",
				hostname, serverPort, context);
		
		rLogger.info(message);
		logPane.append(message, Color.BLACK);
	}

	/**
	 * When the user hits the pause button, stop the data acquisition for both 
	 * instruments if they're running or start them again if it's not.
	 * @param evt
	 */
	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		
		if (src == pauseButton) {
			if (isRunning) {
				// pause the data acqusition
				isRunning = false;
				if (pm != null) {
					pm.stopDataAcquisition();
				}
				
				if (tcd != null) {
					tcd.stopDataAcquisition();
				}
				
				logPane.append(
						"Data acquisition has been paused. Server is still running.", 
						Color.BLACK);
				pauseButton.setText("Unpause");
			} else {
				// unpause the data acquisition
				isRunning = true;
				if (pm != null) {
					try {
						pm.startDataAcquisition();
					} catch (IOException | InterruptedException e) {
						wLogger.fatal("Could not start power meter data acquisition: " + e.getMessage(), e);
						logPane.append("Could not start power meter data acquisition: " + e.getMessage(), Color.RED);
					}
				}
				
				if (tcd != null) {
					tcd.startDataAcquisition();
				}
				
				logPane.append("Data acquisition has been unpaused.", Color.BLACK);
				pauseButton.setText("Pause");
			}
		}
		
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				@SuppressWarnings("unused")
				Frontend f = new Frontend();
			}
		});
	}
}
