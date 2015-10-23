package com.itl.comm.omega.hh802u;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itl.comm.SerialPortConnection;
import com.itl.comm.SerialPortSettingsDialog;
import com.itl.comm.SimpleHandler;
import com.itl.comm.omega.Datagram;
import com.itl.comm.omega.DatagramException;
import com.itl.general.LogEntry;
import com.itl.general.MeasurementLog;
import com.itl.swing.LogTextPane;
import com.sun.net.httpserver.HttpServer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

@SuppressWarnings("serial")
public class OmegaHh802uLoopFrontend extends JFrame 
		implements ActionListener, MouseListener, AdjustmentListener {
	
	private File settingsFile;
	private static final File DEFAULT_SAVE_DIRECTORY = new File(".\\");
	private static final int MAXIMUM_VERIFICATION_ATTEMPTS = 3;
	File saveDirectory;
	
	private static final SimpleDateFormat SDF = 
			new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
	
	// HH802U settings
	public static final int ID = 0;
	public static final int CHANNEL = 0;
	
	SerialPortConnection serialPortConnection;
	Timer measuringTimer;
	MeasurementLog measurementLog;
	
	JMenuItem connectMenuItem;
	JMenuItem closeMenuItem;
	JMenuItem exitMenuItem;
	
	SerialPortSettingsDialog settingsDialog;
	
	LogTextPane generalTextPane;
	DefaultTableModel dataTableModel;
	public static final String[] DATA_TABLE_COLUMN_NAMES = {
			"Time", "t1", "t2"};
	DefaultTableModel deltaTableModel;
	public static final String[] DELTA_TABLE_COLUMN_NAMES = {
		"", "t1", "t2"};
	
	// Server stuff
	static final int DEFAULT_PORT = 3986;
	int port;
	HttpServer server;
	SimpleHandler handler;
	
	public OmegaHh802uLoopFrontend() {
		// General data intialization
		measurementLog = new MeasurementLog();
		
		readSettings();
		initializeAndSetMenuBar();
		JPanel panel = initializeComponents();
		initializeServer();

		settingsDialog = new SerialPortSettingsDialog(this);		
		this.setContentPane(panel);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (measuringTimer != null) {
					measuringTimer.cancel();
				}
				if (serialPortConnection != null) {
					serialPortConnection.close();
				}
				if (server != null) {
					server.stop(0);
				}

				System.exit(0);
			}
		});
		
		this.setTitle("Omega HH802U");
		this.pack();
		this.setMinimumSize(this.getPreferredSize());
		this.setLocation(50, 50);
		this.setVisible(true);
		showSettingsAndConnect();
	}

	private void initializeAndSetMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		
		connectMenuItem = new JMenuItem("Connect to Port", KeyEvent.VK_N);
		connectMenuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		connectMenuItem.addActionListener(this);
		fileMenu.add(connectMenuItem);
		
		closeMenuItem = new JMenuItem("Close Port");
		closeMenuItem.addActionListener(this);
		fileMenu.add(closeMenuItem);
		
		exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitMenuItem.addActionListener(this);
		fileMenu.add(exitMenuItem);
		
		this.setJMenuBar(menuBar);
	}
	
	private JPanel initializeComponents() {
		generalTextPane = new LogTextPane();
		generalTextPane.setEditable(true);
		generalTextPane.setFocusable(false);
		JScrollPane generalTextScrollPane = new JScrollPane(
				generalTextPane,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		generalTextScrollPane.setPreferredSize(
				new Dimension(400, 150));
		generalTextScrollPane.setMinimumSize(
				generalTextScrollPane.getPreferredSize());
		
		dataTableModel = new DefaultTableModel(
				new Object[0][3], DATA_TABLE_COLUMN_NAMES) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable dataTable = new JTable(dataTableModel);
		dataTable.setFocusable(false);
		dataTable.setPreferredScrollableViewportSize(
				new Dimension(300, 250));
		JScrollPane dataScrollPane = new JScrollPane(
				dataTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		dataTable.setFillsViewportHeight(true);
		dataTable.getColumnModel().getColumn(0).setMinWidth(150);
		dataScrollPane.getVerticalScrollBar().addMouseListener(this);
		dataScrollPane.getVerticalScrollBar().addAdjustmentListener(this);
		
		deltaTableModel = new DefaultTableModel(DELTA_TABLE_COLUMN_NAMES, 3) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		deltaTableModel.setValueAt("Current", 0, 0);
		deltaTableModel.setValueAt("1-min max delta", 1, 0);
		deltaTableModel.setValueAt("2-min max delta", 2, 0);
		JTable deltaTable = new JTable(deltaTableModel);
		deltaTable.setFocusable(false);
		deltaTable.setPreferredScrollableViewportSize(
				new Dimension(300, 48));
		JScrollPane deltaScrollPane = new JScrollPane(
				deltaTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		deltaTable.getColumnModel().getColumn(0).setMinWidth(150);
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1;
		c.weighty = 1;
		panel.add(generalTextScrollPane, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1;
		c.weighty = 1;
		panel.add(dataScrollPane, c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LAST_LINE_START;
		c.weightx = 0;
		c.weighty = 0;
		panel.add(deltaScrollPane, c);
		
		return panel;
	}

	private void initializeServer() {
		handler = new SimpleHandler();
		try {
			InetSocketAddress address = new InetSocketAddress(port);
			server = HttpServer.create(address, 0);
			
			server.createContext("/", handler);
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
			generalTextPane.append("Server is operating on port " + port);
		} catch (IOException e) {
			generalTextPane.append(e.getMessage(), Color.RED);
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == connectMenuItem) {
			showSettingsAndConnect();
		} else if (evt.getSource() == closeMenuItem) {
			if (serialPortConnection != null) {
				serialPortConnection.close();
				generalTextPane.append("serialPortConnection closed", Color.BLACK);
				this.setTitle("HH802U Data Acqusition");
			}
			
			if (measuringTimer != null) {
				measuringTimer.cancel();
				generalTextPane.append("MeasureTask cancelled", Color.BLACK);
			}
		} else if (evt.getSource() == exitMenuItem) {
			this.processWindowEvent(
					new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	private void showSettingsAndConnect() {
		int retVal = settingsDialog.showDialog();
		if (retVal == SerialPortSettingsDialog.CONNECT_OPTION) {
			if (serialPortConnection != null) {
				serialPortConnection.close();
			}
			if (measuringTimer != null) {
				measuringTimer.cancel();
			}
			
			if (!settingsDialog.getSerialPortVector().contains(settingsDialog.getPortName())) {
				generalTextPane.append(settingsDialog.getPortName() + 
						" is no longer available. Please refresh the list of available comm ports.", Color.RED);
				return;
			}
			
			try {
				serialPortConnection = new SerialPortConnection(
						settingsDialog.getPortName(),
						settingsDialog.getBaudRate(),
						settingsDialog.getDataBits(),
						settingsDialog.getStopBits(),
						settingsDialog.getParity());
				
				// Ensure that the computer is in fact connected to an HH802U
				int attempt = 0;
				boolean isHH802U = false;
				while (attempt < MAXIMUM_VERIFICATION_ATTEMPTS
						&& !isHH802U) {
					if (getModel().indexOf("802") != -1) {
						isHH802U = true;
					} else {
						attempt++;
						Thread.sleep(500);
					}
				}
				
				if (isHH802U) {
					generalTextPane.append(
							"Connecting to HH802U on " + settingsDialog.getPortName(), 
							Color.BLACK);
					this.setTitle("HH802U Data Acquisition: Connected to " + 
							settingsDialog.getPortName());
					
					measuringTimer = new Timer();
					// fixed-delay so that loops can't execute too quickly in succession
					measuringTimer.schedule(new MeasureTask(), 0, 2000);
				} else {
					generalTextPane.append(
							settingsDialog.getPortName() + " is not connected to an HH802U!",
							Color.RED);
				}
			} catch (Exception e) {
				e.printStackTrace();
				generalTextPane.append(e.getMessage(), Color.RED);
			}
		}
	}
	
	private class MeasureTask extends TimerTask {
		@SuppressWarnings("unused")
		public void run() {
//			System.out.println("Heartbeat");
			
			char commandStartCode = '#';							// 1 byte
			int commandLength = 1 + 2 + 2 + 2 + 1 + 2;
			String commandLengthString = String.format(
					"%02x", commandLength).toUpperCase();			// 2 bytes
			String commandID = String.format("%02x", ID);			// 2 bytes
			String commandChannel = String.format("%02x", CHANNEL);	// 2 bytes
			char commandCode = 'N'; 								// 1 byte
			String incompleteCommand = commandStartCode + 
					commandLengthString + commandID + commandChannel + commandCode;
			int commandChecksum = 0;
			for (int i = 0; i < incompleteCommand.length(); i++) {
				commandChecksum = commandChecksum + (int)incompleteCommand.charAt(i);
			}
			String command = 
					incompleteCommand + 
					String.format("%02x", (commandChecksum & 0x00FF)).toUpperCase() + 
					"\r\n";
						
			try {
				serialPortConnection.write(command);	
				byte[] response;
				Vector<Byte> accumResponse = new Vector<Byte>();
				
				// any valid response will have far less than 255 bytes;
				// we'll set it to this until we have the actual response
				// length as given by the accumulated response
				int reportedLength = 255;
				int byteCnt;
				
				// It's possible that the HH802U won't respond at all; 
				// don't wait more than 150ms for a full response.
				int loopCnt = 0;
				while(loopCnt < 6 && accumResponse.size() < reportedLength) {

					Thread.sleep(25);
					response = serialPortConnection.readBytes();
					
					// We can't know when the full response will come
					// in and without at least the second byte, we don't know
					// how long it'll be. If we're not careful, we might even
					// get two responses!
					// Copy bytes from response to accumResponse until we have
					// at least the second byte (which reports the response 
					// length), at which point set the reportedLength so we can
					// stop accumulating once we have enough bytes. Any extra 
					// bytes can be thrown away.
					byteCnt = 0;
					while (byteCnt < response.length && 
							accumResponse.size() < reportedLength) {
						
						accumResponse.add(response[byteCnt]);
						if (accumResponse.size() > 1) {
							// I can't simple cast response[1] to an int because if
							// response [1] > 0x80, it'll be interpreted as a negative
							// integer!
							reportedLength = (int)(accumResponse.get(1) & 0x00FF);
						}
						byteCnt++;
					}
					
					loopCnt++;
				}
				
				if (accumResponse.size() == reportedLength) {
					// this might be a valid response; try to parse it
					int responseStartCode = (int)(accumResponse.get(0) & 0x00FF);

					int responseID = (int)(accumResponse.get(2) & 0x00FF);
					int responseChannel = (int)(accumResponse.get(3) & 0x00FF);

					Datagram[] datagrams = new Datagram[2];
					datagrams[0] = new Datagram(accumResponse, 4);
					datagrams[1] = new Datagram(accumResponse, 9);

					int checksumIndex = 14;
					int responseChecksum = (int)(accumResponse.get(checksumIndex) & 0x00FF);
					
					if (checkChecksum(accumResponse, checksumIndex)) {
						Vector<String> newRow = new Vector<String>();
						long now = System.currentTimeMillis();
						newRow.addElement(SDF.format(now));
						String dataString0 = null;
						String dataString1 = null;

						if ((datagrams[0].getFunction().equalsIgnoreCase("J") ||
								datagrams[0].getFunction().equalsIgnoreCase("K")) &&
								datagrams[0].getUnits().equalsIgnoreCase("degC")) {

							dataString0 = datagrams[0].getData() + " " + datagrams[0].getUnits();
						} else {
							dataString0 = "-";
							generalTextPane.append(
									"Datagram 0 is not a Celsius temperature measurement", 
									Color.RED);
						}

						if ((datagrams[1].getFunction().equalsIgnoreCase("J") ||
								datagrams[1].getFunction().equalsIgnoreCase("K")) &&
								datagrams[1].getUnits().equalsIgnoreCase("degC")) {

							dataString1 = datagrams[1].getData() + " " + datagrams[1].getUnits();
						} else {
							dataString1 = "-";
							generalTextPane.append(
									"Datagram 1 is not a Celsius temperature measurement", 
									Color.RED);
						}
						newRow.addElement(dataString0);
						newRow.addElement(dataString1);
						dataTableModel.addRow(newRow);
						double[] newData = {datagrams[0].getData(), datagrams[1].getData()};

						measurementLog.addEntry(new LogEntry(now, newData));
						double[] delta1Min = measurementLog.maximumDifferencesAbsolute(now - 1*60*1000, now);
						double[] delta2Min = measurementLog.maximumDifferencesAbsolute(now - 2*60*1000, now);

						deltaTableModel.setValueAt(dataString0, 0, 1);
						deltaTableModel.setValueAt(dataString1, 0, 2);
						deltaTableModel.setValueAt(
								String.format("%.1f %s", delta1Min[0], datagrams[0].getUnits()), 1, 1);
						deltaTableModel.setValueAt(
								String.format("%.1f %s", delta1Min[1], datagrams[1].getUnits()), 1, 2);
						deltaTableModel.setValueAt(
								String.format("%.1f %s", delta2Min[0], datagrams[0].getUnits()), 2, 1);
						deltaTableModel.setValueAt(
								String.format("%.1f %s", delta2Min[1], datagrams[1].getUnits()), 2, 2);

						String handlerString = createXmlString(now, datagrams);

						handler.setText(handlerString);
					} else {
						generalTextPane.append("Response fails checksum; discarding.", Color.BLACK);
					}
				} else if (accumResponse.size() == 0) {
					// This needs to be kept in because occasionally the device will 
					// simply refuse to output! In such a case, the program will only
					// sleep 6 times, after which it'll give up on trying to get a
					// valid response
					generalTextPane.append("No response; carrying on.", Color.BLACK);
				} else {
					// There's an accumulated response, but it's somehow 
					// not the size it should be; this can happen if at two 
					// bytes were returned by the device but the rest wasn't
					// after the six 25ms loops above
					generalTextPane.append("Response is incorrectly sized; discarding", Color.BLACK);
				}
			} catch (IOException e) {
				e.printStackTrace();
				generalTextPane.append("Connection Lost! Stopping data acquisition!", Color.RED);
				
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// This is in its own thread because we really 
						// want to ensure that the TimerTask is 
						// cancelled ASAP. If we wait for the 
						// serialPortConnection to close, it might take 
						// so long that the TimerTask runs again, 
						// causing another exception!
						serialPortConnection.close();
						generalTextPane.append("serialPortConnection closed.", Color.BLACK);
					}
				});
				
				measuringTimer.cancel();
				generalTextPane.append("MeasureTask cancelled.", Color.BLACK);
				OmegaHh802uLoopFrontend.this.setTitle("HH802U Data Acquisition");
			} catch (InterruptedException e) {
				e.printStackTrace();
				generalTextPane.append(e.getMessage() + "; you need to reconnect", Color.RED);
				
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// This is in its own thread because we really 
						// want to ensure that the TimerTask is 
						// cancelled ASAP. If we wait for the 
						// serialPortConnection to close, it might take 
						// so long that the TimerTask runs again, 
						// causing another exception!
						serialPortConnection.close();
						generalTextPane.append("serialPortConnection closed.", Color.BLACK);
					}
				});
				
				measuringTimer.cancel();
				generalTextPane.append("MeasureTask cancelled.", Color.BLACK);
				OmegaHh802uLoopFrontend.this.setTitle("HH802U Data Acquisition");
			} catch (DatagramException e) {
				generalTextPane.append(e.getMessage() + "; discarding", Color.BLACK);
			}
		}
	}
	
	/**
	 * Check the checksum given in the response against the checksum as
	 * calculated. The checksum is calculated by adding the integer values of
	 * every byte before the checksum position and taking only the two least
	 * significant hex digits (which can be contained in one byte).
	 * This is then compared to the given checksum (which, because it is one
	 * byte in length, ranges from 0x00 to 0xFF).
	 * @param input: an array of bytes, including the checksum byte
	 * @param checksumPosition: the index of the checksum byte
	 * @return if the calculated checksum is equal to the given checksum
	 */
	static private boolean checkChecksum(Vector<Byte> input, int checksumIndex) {
		int calculatedChecksum = 0;
		int givenChecksum = (int)(input.get(checksumIndex) & 0x00FF);
		for (int i = 0; i < checksumIndex; i++) {
			calculatedChecksum += (int)input.get(i);
		}
//		System.out.println(
//				"Calculated " + (calculatedChecksum & (0x00FF)) + 
//				" Received " + givenChecksum);
		return (calculatedChecksum & (0x00FF)) == givenChecksum;
	}
	
	/**
	 * Calculates a checksum by adding together the ASCII values of
	 * each character of the input string. This checksum is then
	 * converted to a string and the last two characters returned.
	 * @param string
	 * @return
	 */
	static private String calculateChecksum(String string) {
		int checksum = 0;
		for (int i = 0; i < string.length(); i++) {
			checksum = checksum + (int)string.charAt(i);
		}		
		String checksumString = String.format("%02x", checksum).toUpperCase();
		return checksumString.substring(checksumString.length() - 2);
	}
	
	private void readSettings() {
		File sourceFile = null;
		try {
			sourceFile = new File(
					getClass().
					getProtectionDomain().
					getCodeSource().
					getLocation().
					toURI());
		} catch (URISyntaxException e) {
			//There's something deeply wrong; exit with error
			System.exit(1);
		}
		
		if (sourceFile.isFile()) {
			//This is the jar file
			String containingDirectory = sourceFile.getParent();
			settingsFile = new File(containingDirectory, "settings.ini");
		} else {
			//This is an Eclipse working set
			settingsFile = new File("hh802uSettings.ini");
		}
		
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(settingsFile));
			String line;
			String[] tokens;
			String settingString;
			
			while((line = reader.readLine()) != null) {
				tokens = line.split("=");
				settingString = tokens[tokens.length - 1].trim();
				line = line.toLowerCase();
				if (line.indexOf("savedirectory") != -1) {
					saveDirectory = new File(settingString);
				} else if (line.indexOf("port") != -1) {
					port = Integer.parseInt(settingString);
				}
			}
			
			reader.close();
		} catch (IOException ioe) {
			generalTextPane.append(ioe.getMessage(), Color.RED);
			ioe.printStackTrace();
		}
		
		if (saveDirectory == null) {
			saveDirectory = DEFAULT_SAVE_DIRECTORY;
		}
		if (port == 0) {
			port = DEFAULT_PORT;
		}
	}
	
	private String createXmlString(Long timestamp, Datagram[] datagrams) {
		String output = "";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.newDocument();
			
			Element rootElement = document.createElement("xml");
			document.appendChild(rootElement);
			
			Element instrumentElement = document.createElement("instrument");
			instrumentElement.appendChild(
					document.createTextNode("Omega HH802U Thermometer"));
			rootElement.appendChild(instrumentElement);
			
			Element timeElement = document.createElement("time");
			timeElement.appendChild(document.createTextNode(
					String.format("%d", timestamp)));
			rootElement.appendChild(timeElement);
			
			Element measurementElement;
			Element element;
			for (int i = 0; i < datagrams.length; i++) {
				measurementElement = document.createElement("measurement");
				
				element = document.createElement("sensor");
				element.appendChild(document.createTextNode(
						String.format("%d", (i + 1))));
				measurementElement.appendChild(element);
				
				element = document.createElement("isBatteryLow");
				element.appendChild(document.createTextNode(
						String.format("%b", datagrams[i].isBatteryLow())));
				measurementElement.appendChild(element);
				
				element = document.createElement("function");
				element.appendChild(document.createTextNode(
						datagrams[i].getFunction()));
				measurementElement.appendChild(element);
				
				element = document.createElement("value");
				element.appendChild(document.createTextNode(
						Double.toString((datagrams[i].getData()))));
				measurementElement.appendChild(element);
				
				element = document.createElement("units");
				element.appendChild(document.createTextNode(
						datagrams[i].getUnits()));
				measurementElement.appendChild(element);
				
				rootElement.appendChild(measurementElement);
			}
			
			// So the OutputSteam is used by the XMLSerializer, then
			// its String representation is sent to the handler, which
			// takes that String representation and uses it to create
			// an OutputStream.
			// TODO: refactor this
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OutputFormat format = new OutputFormat(document);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);
			
			output = out.toString();
			out.close();
		} catch(ParserConfigurationException e) {
			generalTextPane.append(e.getMessage(), Color.RED);
		} catch (IOException ioe) {
			generalTextPane.append(ioe.getMessage(), Color.RED);
		}
		
		return output;
	}
	
	private String getModel() 
			throws IOException, InterruptedException {
		String startCode = "#";
		int commandLength = 1 + 2 + 2 + 2 + 1 + 2;
		String lengthString = String.format("%02x", commandLength).toUpperCase();
		String id = String.format("%02x", ID);
		String channel = String.format("%02x", CHANNEL);
		String commandCode = "R";
		String incompleteCommand = 
				startCode + lengthString + id + channel + commandCode;

		String command = 
				incompleteCommand +
				calculateChecksum(incompleteCommand) +
				"\r\n";
		
		serialPortConnection.write(command);
		Thread.sleep(50);
		byte[] response = serialPortConnection.readBytes();
		char[] responseChars = new char[response.length];
		for (int i = 0; i < response.length; i++) {
			responseChars[i] = (char)response[i];
		}
		return new String(responseChars);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Since the frontend constructor itself takes care
				// of packing, setting location, setting itself
				// visible, and so on, just create a new one here
				// and do nothing else.
				new OmegaHh802uLoopFrontend();
			}
		});
	}

	/**
	 * Unnecessary; does nothing
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * Unnecessary; does nothing
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Unnecessary; does nothing
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Remove the auto-scrolling listener so that if the scrollbar 
	 * is being moved around, the mouseReleased will trigger before
	 * it autoscrolls to the bottom (which would otherwise trigger the
	 * code to add the listener!)
	 */
	public void mousePressed(MouseEvent e) {
		JScrollBar src = (JScrollBar)e.getSource();
		src.removeAdjustmentListener(this);
	}

	/**
	 * When a vertical scroll bar is clicked and released, 
	 * check to see if it's being placed at the bottom. If so,
	 * ensure it stays at the bottom as the scrollbar is extended
	 * otherwise, keep it where it is.
	 */
	public void mouseReleased(MouseEvent e) {
//		System.out.println("mouse released");
		JScrollBar src = (JScrollBar)e.getSource();
		if (src.getValue() == (src.getMaximum() - src.getVisibleAmount())) {
			// We've manually scrolled to the bottom, so we want to automatically
			// scroll down as new items are added
			src.addAdjustmentListener(this);
		} else {
			// We've manually scrolled above the bottom, so we don't want to
			// automatically scroll
			src.removeAdjustmentListener(this);
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent evt) {
		JScrollBar src = (JScrollBar)evt.getSource();
		src.setValue(src.getMaximum());
	}
}
