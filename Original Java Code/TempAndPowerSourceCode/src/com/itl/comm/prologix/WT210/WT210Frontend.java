package com.itl.comm.prologix.WT210;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Executors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itl.comm.SerialPortConnection;
import com.itl.comm.SimpleHandler;
import com.itl.general.LogEntry;
import com.itl.general.MeasurementLog;
import com.itl.swing.LogTextPane;
import com.sun.net.httpserver.HttpServer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

@SuppressWarnings("serial")
public class WT210Frontend extends JFrame implements ActionListener {
	private File settingsFile;
	private static final File DEFAULT_SAVE_DIRECTORY = new File(".\\");
	
	private static final SimpleDateFormat SDF = 
		new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
	private static final SimpleDateFormat FILENAME_DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd");
	
	//TODO: format same as incoming format
	private static final DecimalFormat ENGINEERING_FORMAT =
		new DecimalFormat("##0.####E00");
	
	SerialPortConnection serialPortConnection;
	MeasurementLog measurementLog;
	
	// Connection stuff
	JComboBox<String> serialPortComboBox;
	JButton refreshSerialPortButton;
	JFormattedTextField gpibPortField;
	LogTextPane generalTextPane;
	
	// Data collection stuff
	JButton toggleDataCollectionButton;
	Timer measuringTimer;
	
	JButton saveDataButton;
	JFormattedTextField reportNumberField;
	File saveDirectory;
	
	// Some of this stuff is definitely unnecessary
	Vector<String> dataColumnNames;
	DefaultTableModel dataTableModel;
	
	Vector<String> averageColumnNames;
	DefaultTableModel averageTableModel;
	
	// Server stuff
	static final int DEFAULT_PORT = 3985;
	int port;
	HttpServer server;
	SimpleHandler handler;
	
	public WT210Frontend() {
		readSettings();
		
		// Data initialization
		measurementLog = new MeasurementLog();
		
		// Connection component initialization
		serialPortComboBox = new JComboBox<String>();
		serialPortComboBox.setModel(
				new DefaultComboBoxModel<String>(
						getSerialPortVector()));
		serialPortComboBox.addActionListener(this);
		
		refreshSerialPortButton = new JButton("Refresh Ports");
		refreshSerialPortButton.addActionListener(this);
		
		//TODO: actually format this control!
		gpibPortField = new JFormattedTextField();
		gpibPortField.setColumns(2);
		gpibPortField.setEnabled(false);
		gpibPortField.addActionListener(this);
		
		generalTextPane = new LogTextPane();
		generalTextPane.setEditable(true);
		generalTextPane.setFocusable(false);
		JScrollPane generalTextView = new JScrollPane(
				generalTextPane,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		generalTextView.setPreferredSize(new Dimension(550, 150));
		
		toggleDataCollectionButton = new JButton("Start Data Collection");
		toggleDataCollectionButton.setEnabled(false);
		toggleDataCollectionButton.addActionListener(this);
		
		// TODO: make own table model to allow for more measurements and
		// make them non-editable in a more elegant way
		dataColumnNames = new Vector<String>();
		dataColumnNames.addElement("Time");
		dataColumnNames.addElement("Voltage");
		dataColumnNames.addElement("Amperage");
		dataColumnNames.addElement("Wattage");
		
		Vector<Vector<String>> data = new Vector<Vector<String>>();
		
		dataTableModel = new DefaultTableModel(data, dataColumnNames) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable dataTable = new JTable(dataTableModel);
		dataTable.setFocusable(false);
		dataTable.setPreferredScrollableViewportSize(new Dimension(500, 250));
		JScrollPane dataView = new JScrollPane(
				dataTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		dataTable.setFillsViewportHeight(true);
		
		averageColumnNames = new Vector<String>();
		averageColumnNames.addElement("");
		averageColumnNames.addElement("Voltage");
		averageColumnNames.addElement("Amperage");
		averageColumnNames.addElement("Wattage");
		
		averageTableModel = new DefaultTableModel(averageColumnNames, 3) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		averageTableModel.setValueAt("Current", 0, 0);
		averageTableModel.setValueAt("5-minute average", 1, 0);
		averageTableModel.setValueAt("10-minute average", 2, 0);
		JTable averageTable = new JTable(averageTableModel);
		averageTable.setFocusable(false);
		averageTable.setPreferredScrollableViewportSize(new Dimension(500, 48));
		JScrollPane averageView = new JScrollPane(
				averageTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		saveDataButton = new JButton("Save Data Log");
		saveDataButton.addActionListener(this);
		
		NumberFormat reportNumberFormat = NumberFormat.getIntegerInstance();
		reportNumberFormat.setGroupingUsed(false);
		reportNumberField = new JFormattedTextField(reportNumberFormat);
		reportNumberField.setColumns(5);
		
		// Connection component layout
		JPanel connectPanel = new JPanel(new GridBagLayout());
		GridBagConstraints connectConstraints = new GridBagConstraints();
		connectConstraints.insets = new Insets(2, 2, 2, 2);
		
		connectConstraints.gridx = 0;
		connectConstraints.gridy = 0;
		connectConstraints.fill = GridBagConstraints.NONE;
		connectConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		connectConstraints.gridwidth = 2;
		connectConstraints.weightx = 0;
		connectConstraints.weighty = 0;
		connectPanel.add(refreshSerialPortButton, connectConstraints);
		
		connectConstraints.gridx = 0;
		connectConstraints.gridy++;
		connectConstraints.fill = GridBagConstraints.HORIZONTAL;
		connectConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
		connectConstraints.gridwidth = 1;
		connectConstraints.weightx = 0;
		connectConstraints.weighty = 0;
		connectPanel.add(new JLabel("Serial Port"), connectConstraints);
		
		connectConstraints.gridx = 1;
		connectConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		connectPanel.add(serialPortComboBox, connectConstraints);
		
		connectConstraints.gridx = 0;
		connectConstraints.gridy++;
		connectConstraints.fill = GridBagConstraints.NONE;
		connectConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
		connectConstraints.gridwidth = 1;
		connectConstraints.weightx = 0;
		connectConstraints.weighty = 0;
		connectPanel.add(new JLabel("GPIB Port"), connectConstraints);
		
		connectConstraints.gridx = 1;
		connectConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		connectPanel.add(gpibPortField, connectConstraints);
		
		connectConstraints.gridx = 2;
		connectConstraints.gridy = 0;
		connectConstraints.gridwidth = 1;
		connectConstraints.gridheight = 3;
		connectConstraints.fill = GridBagConstraints.BOTH;
		connectConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
		connectConstraints.weightx = 0.5;
		connectConstraints.weighty = 0.5;
		connectPanel.add(generalTextView, connectConstraints);
		
		// Data component layout
		JPanel dataPanel = new JPanel(new GridBagLayout());
		GridBagConstraints dataConstraints = new GridBagConstraints();
		dataConstraints.insets = new Insets(2, 2, 2, 2);
		
		dataConstraints.gridx = 0;
		dataConstraints.gridy = 0;
		dataConstraints.fill = GridBagConstraints.BOTH;
		dataConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		dataConstraints.gridwidth = 2;
		dataConstraints.weightx = 0.5;
		dataConstraints.weighty = 1;
		dataPanel.add(dataView, dataConstraints);
		
		dataConstraints.gridx = 0;
		dataConstraints.gridy = 1;
		dataConstraints.fill = GridBagConstraints.HORIZONTAL;
		dataConstraints.anchor = GridBagConstraints.LAST_LINE_START;
		dataConstraints.gridwidth = 2;
		dataConstraints.weightx = 0.5;
		dataConstraints.weighty = 0;
		dataPanel.add(averageView, dataConstraints);
		
		dataConstraints.gridx = 0;
		dataConstraints.gridy = 2;
		dataConstraints.fill = GridBagConstraints.NONE;
		dataConstraints.anchor = GridBagConstraints.PAGE_START;
		dataConstraints.gridwidth = 1;
		dataConstraints.weightx = 0.5;
		dataConstraints.weighty = 0;
		dataPanel.add(toggleDataCollectionButton, dataConstraints);
		
		// Save panel layout
		JPanel savePanel = new JPanel(new GridBagLayout());
		GridBagConstraints saveConstraints = new GridBagConstraints();
		saveConstraints.insets = new Insets(2, 2, 2, 2);
		
		saveConstraints.gridx = 0;
		saveConstraints.gridy = 0;
		saveConstraints.gridwidth = 1;
		saveConstraints.fill = GridBagConstraints.VERTICAL;
		saveConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
		saveConstraints.weightx = 0;
		saveConstraints.weighty = 0;
		savePanel.add(new JLabel("Report Number:"), saveConstraints);
		
		saveConstraints.gridx = 1;
		saveConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		savePanel.add(reportNumberField, saveConstraints);
		
		saveConstraints.gridx = 0;
		saveConstraints.gridy = 1;
		saveConstraints.gridwidth = 2;
		saveConstraints.fill = GridBagConstraints.NONE;
		saveConstraints.anchor = GridBagConstraints.PAGE_START;
		saveConstraints.weightx = 0;
		saveConstraints.weighty = 0;
		savePanel.add(saveDataButton, saveConstraints);
		
		// Overall layout
		JPanel contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints contentConstraints = new GridBagConstraints();
		contentConstraints.insets = new Insets(2, 2, 2, 2);
		
		contentConstraints.gridx = 0;
		contentConstraints.gridy = 0;
		contentConstraints.fill = GridBagConstraints.HORIZONTAL;
		contentConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		contentConstraints.weightx = 0.5;
		contentConstraints.weighty = 0;
		contentPanel.add(connectPanel, contentConstraints);
		
		contentConstraints.gridx = 0;
		contentConstraints.gridy = 1;
		contentConstraints.fill = GridBagConstraints.BOTH;
		contentConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		contentConstraints.weightx = 0.5;
		contentConstraints.weighty = 0.5;
		contentPanel.add(dataPanel, contentConstraints);
		
		contentConstraints.gridx = 0;
		contentConstraints.gridy = 2;
		contentConstraints.fill = GridBagConstraints.BOTH;
		contentConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		contentConstraints.weightx = 0.5;
		contentConstraints.weighty = 0.5;
		contentPanel.add(savePanel, contentConstraints);
		
		initializeServer();
		
		this.setContentPane(contentPanel);
		this.setTitle("WT210 Frontend");
		
		// Ensure that the commPort is closed when exiting
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				if (serialPortConnection != null) {
					serialPortConnection.close();
				}
				if (measuringTimer != null) {
					measuringTimer.cancel();
				}
				if (server != null) {
					server.stop(0);
				}
				System.exit(0);
			}
		});
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
	
	@SuppressWarnings("unchecked")
	private static Vector<String> getSerialPortVector() {
		Vector<String> serialPortVector = new Vector<String>();
		CommPortIdentifier portIdentifier;
		Enumeration<CommPortIdentifier> portEnum = 
			CommPortIdentifier.getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			portIdentifier = portEnum.nextElement();
			if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				serialPortVector.addElement(portIdentifier.getName());
			}
		}
		
		return serialPortVector;
	}
	
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == serialPortComboBox) {
			if (serialPortConnection != null) {
				serialPortConnection.close();
			} try {
				String response;
				serialPortConnection = new SerialPortConnection(
						(String)serialPortComboBox.getSelectedItem(), 9600, 8, 1, SerialPort.PARITY_EVEN);
				generalTextPane.append(
						"Changing to port " + (String)serialPortComboBox.getSelectedItem() + "\n", Color.BLACK);
				
				// check for Prologix box
				serialPortConnection.write("++ver\n");
				Thread.sleep(20);
				response = serialPortConnection.read();
				
				if (response.toLowerCase().indexOf("prologix") != -1) {
					generalTextPane.append("Controller found: " + response, Color.GREEN);
					gpibPortField.setEnabled(true);
					
					// Check for Yokogawa WT210
					serialPortConnection.write("++addr\n");
					Thread.sleep(20);
					response = serialPortConnection.read();
					
					// strip off the newline character and send the result to
					// the gpibPortField
					int charIndex;
					charIndex = response.indexOf(10);
					
					if (charIndex > 0) {
						gpibPortField.setText(response.substring(0, charIndex - 1));
					}
					
					isConnectedToYokogawa();
					
				} else {
					generalTextPane.append("Prologix device not found!\n", Color.RED);
					gpibPortField.setEnabled(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (evt.getSource() == refreshSerialPortButton) {
			serialPortComboBox.setModel(new DefaultComboBoxModel<String>(getSerialPortVector()));
		} else if (evt.getSource() == gpibPortField) {
			if (serialPortConnection != null) {
				try {
					generalTextPane.append(
							"Changing GPIB port to " + gpibPortField.getText() + "\n", Color.BLACK);
					serialPortConnection.write("++addr " + gpibPortField.getText() + "\n");
					Thread.sleep(20);
					isConnectedToYokogawa();
					
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		} else if (evt.getSource() == toggleDataCollectionButton) {
			// Yeah, this is a dumb way of doing this.  But it should
			// only be set here!
			if (toggleDataCollectionButton.getText().toLowerCase().indexOf("start") != -1) {
				toggleDataCollectionButton.setText("Stop Data Collection");
				
				serialPortComboBox.setEnabled(false);
				gpibPortField.setEnabled(false);
				
				if (measuringTimer != null) {
					measuringTimer.cancel();
				}
				measuringTimer = new Timer();
				measuringTimer.schedule(new MeasureTask(), 0, 1000);
			} else if (toggleDataCollectionButton.getText().toLowerCase().indexOf("stop") != -1) {
				if (measuringTimer != null) {
					measuringTimer.cancel();
				}
				measuringTimer = new Timer();
				measuringTimer.schedule(new PostMeasureTask(), 0, 100);
				
				toggleDataCollectionButton.setText("Start Data Collection");
				serialPortComboBox.setEnabled(true);
				gpibPortField.setEnabled(true);
				measuringTimer.cancel();
			}
		} else if (evt.getSource() == saveDataButton) {
			try {
				String filename = reportNumberField.getText() + 
					"_" + 
					FILENAME_DATE_FORMAT.format(System.currentTimeMillis()) +
					".csv";
				File file = new File(saveDirectory, filename);
				LogEntry logEntry;
				PrintWriter pw = new PrintWriter(
						new FileWriter(file));
				pw.format("%s, %s, %s, %s, %s, %s, %s%n",
						"Time",
						"Voltage",
						"Amperage",
						"Wattage");
				for (int i = 0; i < measurementLog.getLogSize(); i++) {
					logEntry = measurementLog.getEntry(i);
					try {
						pw.print(SDF.format(logEntry.timeStamp) + ", ");
					} catch (IllegalArgumentException iea) {
						pw.print(logEntry.timeStamp + ", ");
					}
					
					for (int j = 0; j < measurementLog.getDataLength() - 1; j++) {
						pw.print(ENGINEERING_FORMAT.format(logEntry.data[j]) + ", ");
					}
					
					pw.println(ENGINEERING_FORMAT.format(logEntry.data[logEntry.data.length - 1]));
				}
				pw.close();
				generalTextPane.append(
						"Log saved at " + file.getAbsolutePath() + "\n",
						Color.BLACK);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Error!", "Error", JOptionPane.ERROR_MESSAGE);
				generalTextPane.append(ioe.getMessage() + "\n", Color.RED);
				ioe.printStackTrace();
			}
		}
	}
	
	private class MeasureTask extends TimerTask {
		public void run() {
			try {
				serialPortConnection.write("meas:val?\n");
				Thread.sleep(100);
				String[] measurementStrings = serialPortConnection.read().split(",");
				
				// if the length is not three, something's gone wrong
				if (measurementStrings.length == 3) {
					// TODO: if number of rows is too large, start removing them
					// from table
					Vector<String> newRow = new Vector<String>();
					long now = System.currentTimeMillis();
					newRow.addElement(SDF.format(now));
					newRow.addElement(measurementStrings[0]);
					newRow.addElement(measurementStrings[1]);
					newRow.addElement(measurementStrings[2]);
					dataTableModel.addRow(newRow);
					
					double[] newData = new double[measurementStrings.length];
					double parsedValue;
					double[] average5min = new double[measurementStrings.length];
					double[] average10min = new double[measurementStrings.length];
					
					try {
						for (int i = 0; i < measurementStrings.length; i++) {
							parsedValue = Double.parseDouble(measurementStrings[i]);
							if (parsedValue == Double.parseDouble("9.9E+37")) {
								newData[i] = Double.POSITIVE_INFINITY;
							} else if (parsedValue == Double.parseDouble("9.91E+37")) {
								newData[i] = Double.NaN;
							} else {
								newData[i] = parsedValue;
							}
						}
						
						measurementLog.addEntry(new LogEntry(now, newData));
						average5min = measurementLog.calculateAverages(now - 5*60*1000);
						average10min = measurementLog.calculateAverages(now - 10*60*1000);

						for (int i = 0; i < measurementStrings.length; i++) {
							averageTableModel.setValueAt(
									ENGINEERING_FORMAT.format(newData[i]), 0,
									i + 1);
							averageTableModel.setValueAt(
									ENGINEERING_FORMAT.format(average5min[i]),
									1, i + 1);
							averageTableModel.setValueAt(
									ENGINEERING_FORMAT.format(average10min[i]),
									2, i + 1);
						}
					} catch (NumberFormatException nfe) {
						generalTextPane.append(nfe.getMessage() + "\n",
								Color.RED);
					}
					
					DocumentBuilderFactory dbf = 
							DocumentBuilderFactory.newInstance();
					try {
						Element child;
						
						DocumentBuilder db = dbf.newDocumentBuilder();
						Document document = db.newDocument();
						
						Element root = document.createElement("xml");
						document.appendChild(root);
						
						child = document.createElement("instrument");
						child.setTextContent(
								"Yokogawa WT210 Digital Power Meter");
						root.appendChild(child);
						
						child = document.createElement("time");
						child.setTextContent(String.format("%d", now));
						root.appendChild(child);
						
						child = document.createElement("volts");
						child.setTextContent(measurementStrings[0].trim());
						root.appendChild(child);
						
						child = document.createElement("amps");
						child.setTextContent(measurementStrings[1].trim());
						root.appendChild(child);
						
						child = document.createElement("watts");
						child.setTextContent(measurementStrings[2].trim());
						root.appendChild(child);
						
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						OutputFormat format = new OutputFormat(document);
						format.setIndenting(true);
						XMLSerializer serializer = new XMLSerializer(
								out, format);
						serializer.serialize(document);
						
						handler.setText(out.toString());
					} catch (ParserConfigurationException e) {
						generalTextPane.append(e.getMessage(), Color.RED);
					}
				}

				else {
					generalTextPane.append(
							"Could not read all three data readings; discarding buffer\n", Color.RED);
				}
				
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class PostMeasureTask extends TimerTask {
		public void run() {
			try {
				int elementCount = 0;
				double powerFactor = 0.0;
				double athd = 0.0;
				double vthd = 0.0;
				String temp;
				String[] dataStream = new String[20]; 
				
				serialPortConnection.write(
						":meas:norm:item:v:all off; " +
						":meas:norm:item:a:all off; " +
						":meas:norm:item:w:all off; " +
						":meas:norm:item:pf:all on; " +
						"\n");
				Thread.sleep(250);
				
				serialPortConnection.write(":meas:val?\n");
				Thread.sleep(100);
				
				temp = serialPortConnection.read();
				powerFactor = Double.parseDouble(temp);
				if (powerFactor != Double.POSITIVE_INFINITY || powerFactor != Double.NaN)
					elementCount++;
				
				// Turn harmonics on
				generalTextPane.append("Initiating Harmonics...", Color.BLACK);
				serialPortConnection.write(":harm:stat on\n"); //Error 420
				Thread.sleep(2100); // WT210 User's Manual (pg240)
				
				// TODO Check to make sure harmonics are actually on
				generalTextPane.append("Harmonics On\n", Color.GREEN);
				
				// Enable voltage harmonics
				serialPortConnection.write("meas:harm:item:pres vpattern\n");
				Thread.sleep(100);
				
				// Query voltage harmonics
				serialPortConnection.write("meas:harm:val?\n");
				Thread.sleep(500);
				dataStream = serialPortConnection.read().split(",");
				
				// Parse distortion value from voltage harmonics reading
				vthd = Double.parseDouble(dataStream[1]);
				if (vthd != Double.POSITIVE_INFINITY || vthd != Double.NaN)
					elementCount++;
				
				
				// Enable current harmonics
				serialPortConnection.write("meas:harm:item:pres apattern\n");
				Thread.sleep(100);
				
				// Query current harmonics
				serialPortConnection.write("meas:harm:val?\n");
				Thread.sleep(500);
				dataStream = serialPortConnection.read().split(",");
				
				// Parse distortion value from current harmonics reading
				athd = Double.parseDouble(dataStream[1]);
				if (athd != Double.POSITIVE_INFINITY || athd != Double.NaN)
					elementCount++;
				
				// Turn harmonics off
				serialPortConnection.write(":harm:stat off\n"); //Error 420
				Thread.sleep(2000); // WT210 User's Manual (pg240)
				generalTextPane.append("Harmonics Off\n", Color.GREEN);
				
				// Reset standard measurements
				setMeasurements(); 

				if (elementCount == 3) {
					Vector<String> newRow = new Vector<String>();
					newRow.addElement("Harmonic Measurements");
					newRow.addElement("PF: " + powerFactor);
					newRow.addElement("ATHD: " + athd);
					newRow.addElement("VTHD: " + vthd);
					dataTableModel.addRow(newRow);
				} else {
					generalTextPane.append("Harmonics failed.\n", Color.RED);
				}
				
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * Sets the WT210 to only output voltage, amperage,
	 * and wattage.
	 * 
	 * @throws IOException if the serialPortConnection has
	 * problems writing
	 */
	private void setMeasurements() throws IOException {
		serialPortConnection.write (
				":meas:norm:item:v:all on; " +
				":meas:norm:item:a:all on; " +
				":meas:norm:item:w:all on; " +
				":meas:norm:item:va:all off; " +
				":meas:norm:item:var:all off; " +
				":meas:norm:item:pf:all off; " +
				":meas:norm:item:deg:all off; " +
				":meas:norm:item:vhz:all off; " +
				":meas:norm:item:ahz:all off; " +
				":meas:norm:item:wh:all off; " +
				":meas:norm:item:whp:all off; " +
				":meas:norm:item:whm:all off; " +
				":meas:norm:item:ah:all off; " +
				":meas:norm:item:ahp:all off; " +
				":meas:norm:item:ahm:all off; " +
				":meas:norm:item:vpk:all off; " +
				":meas:norm:item:apk:all off; " +
				":meas:norm:item:time off; " +
				":meas:norm:item:math off; \n");
	}
	
	private boolean isConnectedToYokogawa() throws IOException, InterruptedException {
		serialPortConnection.write("*idn?\n");
		Thread.sleep(20);
		
		if (serialPortConnection.read().toLowerCase().indexOf("yokogawa") != -1) {
			generalTextPane.append(
					"Yokogawa present\n", 
					Color.GREEN);

			setMeasurements();
			toggleDataCollectionButton.setEnabled(true);
			
			return true;
		} else {
			generalTextPane.append(
					"Yokogawa instrument not found!\n", 
					Color.RED);
			toggleDataCollectionButton.setEnabled(false);
			return false;
		}
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
			settingsFile = new File("wt210settings.ini");
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(settingsFile));
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
			ioe.printStackTrace();
		}
		
		if (saveDirectory == null) {
			saveDirectory = DEFAULT_SAVE_DIRECTORY;
		}
		
		if (port == 0) {
			port = DEFAULT_PORT;
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				WT210Frontend myFrontend = new WT210Frontend();
				myFrontend.pack();
				myFrontend.setMinimumSize(myFrontend.getPreferredSize());
				myFrontend.setLocation(50, 50);
				myFrontend.setVisible(true);
			}
		});
	}
}
