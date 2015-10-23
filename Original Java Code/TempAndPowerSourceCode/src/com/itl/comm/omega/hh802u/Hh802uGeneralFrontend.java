package com.itl.comm.omega.hh802u;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;

import com.itl.comm.SerialPortConnection;
import com.itl.comm.SerialPortSettingsDialog;
import com.itl.comm.omega.Datagram;
import com.itl.comm.omega.DatagramException;
import com.itl.swing.LogTextPane;

@SuppressWarnings({"serial", "unused"})
public class Hh802uGeneralFrontend extends JFrame implements ActionListener {
	private static final String DEFAULT_TITLE = "Omega HH802U General Frontend";
	private static final SimpleDateFormat sdf = 
			new SimpleDateFormat("HH:mm:ss.SSS");
	
	JMenuItem connectMenuItem;
	JMenuItem closeMenuItem;
	JMenuItem exitMenuItem;
	
	SerialPortConnection serialPortConnection;
	SerialPortSettingsDialog settingsDialog;
	
	LogTextPane generalTextPane;
	DefaultTableModel incomingDataTableModel;
	JTable incomingDataTable;
	
	JButton getModelButton;
	JButton measureDataButton;
	JButton changeIdButton;
	JTextField idField;
	JTextField channelField;
	JTextField changeIdField;
	JTextField changeChannelField;
	
	public Hh802uGeneralFrontend() {
		// Nimbus!
//		try {
//		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//		        if ("Nimbus".equals(info.getName())) {
//		            UIManager.setLookAndFeel(info.getClassName());
//		            break;
//		        }
//		    }
//		} catch (UnsupportedLookAndFeelException e) {
//		    // handle exception
//		} catch (ClassNotFoundException e) {
//		    // handle exception
//		} catch (InstantiationException e) {
//		    // handle exception
//		} catch (IllegalAccessException e) {
//		    // handle exception
//		}
		
		settingsDialog = new SerialPortSettingsDialog(this);
		
		initializeAndSetMenuBar();
		JPanel idPanel = initializeIdPanel();
		JPanel controlPanel = initializeControlPanel();
		JPanel commPanel = initializeCommPanel();
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		panel.add(idPanel, c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.weighty = 0;
		panel.add(controlPanel, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		panel.add(commPanel, c);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (serialPortConnection != null) {
					serialPortConnection.close();
				}
				//TODO: stop thread
				System.exit(0);
			}
		});
		
		this.setContentPane(panel);
		this.setTitle(DEFAULT_TITLE);
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
	
	private JPanel initializeIdPanel() {
		idField = new JTextField(2);
		idField.setText("00");
		channelField = new JTextField(2);
		channelField.setText("00");
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		panel.add(new JLabel("ID"), c);
		c.gridy++;
		panel.add(new JLabel("Channel"), c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		panel.add(idField, c);
		c.gridy++;
		panel.add(channelField, c);
		
		panel.setBorder(BorderFactory.createTitledBorder("ID and Channel"));
		
		return panel;
	}
	
	private JPanel initializeControlPanel() {
		getModelButton = new JButton("Get Model");
		getModelButton.addActionListener(this);
		
		measureDataButton = new JButton("Get Measurements");
		measureDataButton.addActionListener(this);
		
		changeIdButton = new JButton("Change ID/Channel");
		changeIdButton.addActionListener(this);
		
		changeIdField = new JTextField(2);
		changeIdField.setText("01");
		changeChannelField = new JTextField(2);
		changeChannelField.setText("02");
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		c.weighty = 0;
		panel.add(getModelButton, c);
		
		c.gridy++;
		panel.add(measureDataButton, c);
		
		c.gridy++;
		panel.add(changeIdButton, c);
		
		c.gridx++;
		panel.add(new JLabel("ID"), c);
		c.gridx++;
		panel.add(changeIdField, c);
		c.gridx++;
		panel.add(new JLabel("Channel"), c);
		c.gridx++;
		panel.add(changeChannelField, c);
		
		panel.setBorder(BorderFactory.createTitledBorder("Controls"));
		
		return panel;
	}
	
	private JPanel initializeCommPanel() {
		generalTextPane = new LogTextPane(sdf);
		generalTextPane.setEditable(true);
		generalTextPane.setFocusable(false);
		JScrollPane generalTextScrollPane = new JScrollPane(
				generalTextPane,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		generalTextScrollPane.setPreferredSize(
				new Dimension(400, 150));
		
		incomingDataTableModel = new DefaultTableModel(0, 0);
		incomingDataTable = new JTable(incomingDataTableModel);
		JScrollPane incomingDataTableScrollPane = new JScrollPane(
				incomingDataTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		incomingDataTable.setPreferredScrollableViewportSize(
				new Dimension(600, 400));
		incomingDataTableScrollPane.setMinimumSize(
				incomingDataTableScrollPane.getPreferredSize());
		incomingDataTable.setFillsViewportHeight(true);
		incomingDataTable.getTableHeader().setReorderingAllowed(false);
		incomingDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		incomingDataTable.setColumnSelectionAllowed(true);
		incomingDataTable.setRowSelectionAllowed(false);
		
		incomingDataTable.getColumnModel().addColumnModelListener(
				new TableColumnModelListener() {
			public void columnSelectionChanged(ListSelectionEvent arg0) {				
			}
			
			public void columnRemoved(TableColumnModelEvent arg0) {	
			}
			
			public void columnMoved(TableColumnModelEvent arg0) {				
			}
			
			public void columnMarginChanged(ChangeEvent evt) {
			}
			
			public void columnAdded(TableColumnModelEvent evt) {
				incomingDataTable.getColumnModel().getColumn(
						incomingDataTable.getColumnModel().getColumnCount() - 1).setPreferredWidth(25);
			}
		});
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 0;
		panel.add(generalTextScrollPane, c);
		
		c.gridx++;
		panel.add(incomingDataTableScrollPane, c);
		
		panel.setBorder(BorderFactory.createTitledBorder("Communications Detail"));
		
		return panel;
	}

	private void showSettingsAndConnect() {
		int retVal = settingsDialog.showDialog();
		if (retVal == SerialPortSettingsDialog.CONNECT_OPTION) {
			if (serialPortConnection != null) {
				serialPortConnection.close();
			}
			try {
				serialPortConnection = new SerialPortConnection(
						settingsDialog.getPortName(),
						settingsDialog.getBaudRate(),
						settingsDialog.getDataBits(),
						settingsDialog.getStopBits(),
						settingsDialog.getParity());
				this.setTitle("Now connected to " + 
						settingsDialog.getPortName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == connectMenuItem) {
			showSettingsAndConnect();
		} else if (evt.getSource() == closeMenuItem) {
			if (serialPortConnection != null) {
				serialPortConnection.close();
			}
			this.setTitle(DEFAULT_TITLE);
		} else if (evt.getSource() == exitMenuItem) {
			this.processWindowEvent(
					new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		} else if (evt.getSource() == getModelButton) {
			getModel();
		} else if (evt.getSource() == measureDataButton) {
			measureData();
		} else if (evt.getSource() == changeIdButton) {
			changeId();
		}
	}
	
	private void getModel() {
		String startCode = "#";
		int commandLength = 1 + 2 + 2 + 2 + 1 + 2;
		String lengthString = String.format("%02x", commandLength).toUpperCase();
		String id = idField.getText();
		String channel = channelField.getText();
		String commandCode = "R";
		String incompleteCommand = 
				startCode + lengthString + id + channel + commandCode;

		String command = 
				incompleteCommand +
				calculateChecksum(incompleteCommand) +
				"\r\n";
		
		try {
			sendCommand(command);
			byte[] response = receiveResponse();
			
			char responseStartCode = (char)response[0];
			int responseLength = (int)(response[1] & 0x00FF);
			
			if (responseLength <= (response.length - 1)) {
				int responseID = (int)response[2];
				int responseChannel = (int)response[3];
				char[] responseModel = new char[12];
				for (int i = 0; i < responseModel.length; i++) {
					responseModel[i] = (char)response[4 + i];
				}
				byte s = response[16];
				byte y = response[17];
				byte z = response[18];
				int checksumPosition = 19;
				int responseChecksum = (int)response[checksumPosition];
				
				// Error checking
				if (checkChecksum(response, checksumPosition)) {
					generalTextPane.append(
							"ID: " + responseID +
							" Chan: " + responseChannel + "%n" +
							"    Data: " + new String(responseModel));
				} else {
					//TODO: make this an exception
					System.out.println("This response fails checksum");
				}
			} else {
				//TODO: make this an exception
				System.out.println("This response fails length check");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}  catch (ArrayIndexOutOfBoundsException ooe) {
			generalTextPane.append("No response!", Color.RED);
		}
	}
	
	private void measureData() {
		String commandStartCode = "#"; // 1 byte
		int commandLength = 1 + 2 + 2 + 2 + 1 + 2;
		String commandLengthString = String.format(
				"%02x", commandLength).toUpperCase(); // 2 bytes
		String commandID = idField.getText(); // 2 bytes
		String commandChannel = channelField.getText(); // 2 bytes
		String commandCode = "N"; // 1 byte
		String incompleteCommand = commandStartCode + 
				commandLengthString + commandID + commandChannel + commandCode;
		String command = 
				incompleteCommand + 
				calculateChecksum(incompleteCommand) + 
				"\r\n";
		
		try {
			sendCommand(command);
			
			// receiveResponse handles the delays
			byte[] response = receiveResponse();
			
			char responseStartCode = (char)response[0];
			int responseLength = (int)response[1];
			
			if (responseLength <= response.length) {
				int responseID = (int)response[2];
				int responseChannel = (int)response[3];
				
				Datagram datagram0 = new Datagram(response, 4);
				Datagram datagram1 = new Datagram(response, 9);
				
				int checksumPosition = 14;
				int responseChecksum = (int)response[checksumPosition];
				if (checkChecksum(response, checksumPosition)) {
					generalTextPane.append(
							"ID: " + responseID +
							" Chan: " + responseChannel + "\n" +
							"    LoBat: " + datagram0.isBatteryLow() +
							" Func: " + datagram0.getFunction() +
							" Data: " + datagram0.getData() + datagram0.getUnits() + "\n" +
							"    LoBat: " + datagram1.isBatteryLow() +
							" Func: " + datagram1.getFunction() +
							" Data: " + datagram1.getData() + datagram1.getUnits());
				} else {
					//TODO: make this an exception
					System.out.println("This response fails checksum");
				}
			} else {
				//TODO: make this an exception
				System.out.println("This response fails length check");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException ooe) {
			generalTextPane.append("No response!", Color.RED);
		} catch (DatagramException de) {
			generalTextPane.append(de.getMessage(), Color.RED);
		}
	}
	
	private void changeId() {
		String commandStartCode = "%"; // 1 byte
		int commandLength = 1 + 2 + 2 + 2 + 5 + 2;
		String commandLengthString = String.format(
				"%02x", commandLength).toUpperCase(); // 2 bytes
		String commandID = idField.getText();
		String commandChannel = channelField.getText();
		String commandCode = String.format("I%02x%02x", 
				Integer.parseInt(changeIdField.getText()), 
				Integer.parseInt(changeChannelField.getText())).
				toUpperCase();
		String incompleteCommand = commandStartCode +
				commandLengthString + commandID + commandChannel + commandCode;
		String command =
				incompleteCommand +
				calculateChecksum(incompleteCommand) +
				"\r\n";
		
		try {
			sendCommand(command);
			byte[] response = receiveResponse();
			
			char reponseStartCode = (char)response[0];
			int responseLength = (int)(response[1] & 0x00FF);
			
			if (responseLength <= response.length) {
				int responseID = (int)(response[2] & 0x00FF);
				int responseChannel = (int)(response[3] & 0x00FF);
				byte mysteryByte = response[4];
				
				int checksumIndex = 5;
				if (checkChecksum(response, checksumIndex)) {
					generalTextPane.append(
							"ID: " + responseID +
							" Chan: " + responseChannel + "\n" +
							"    ??: " + mysteryByte);
				} else {
					//TODO: make this an exception
					System.out.println("This response fails checksum");
				}
			} else {
				//TODO: make this an exception
				System.out.println("This response fails length check");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException ooe) {
			generalTextPane.append("No response!", Color.RED);
		}
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
	static private boolean checkChecksum(byte[] input, int checksumPosition) {
		int calculatedChecksum = 0;
		int givenChecksum = (int)(input[checksumPosition] & 0x00FF);
		for (int i = 0; i < checksumPosition; i++) {
			calculatedChecksum = 
					calculatedChecksum + (int)input[i];
		}
//		System.out.println(
//				"Calculated " + (calculatedChecksum & (0x00FF)) + 
//				" Received " + givenChecksum);
		return (calculatedChecksum & (0x00FF)) == givenChecksum;
	}
	
	private void sendCommand(String command) throws IOException {
		serialPortConnection.write(command);
		String[] hexValues = new String[command.length()];
		int necessaryLength = command.length();
		int oldLength = incomingDataTableModel.getColumnCount();
		for (int i = oldLength; i < necessaryLength; i++) {
			incomingDataTableModel.addColumn(i);
		}
		
		for (int i = 0; i < command.length(); i++) {
			hexValues[i] = String.format("%02x", (int)command.charAt(i));
		}
		incomingDataTableModel.addRow(hexValues);
		generalTextPane.append(command);
	}
	
	private byte[] receiveResponse() throws IOException, InterruptedException {
		byte[] response;
		Vector<Byte> totalResponse = new Vector<Byte>();
		
		while (totalResponse.size() == 0 || totalResponse.lastElement() != 0x00) {
			Thread.sleep(10);
			response = serialPortConnection.readBytes();
			for (int i = 0; i < response.length; i++) {
				totalResponse.add(response[i]);
			}
		}
		
		int necessaryLength = totalResponse.size();
		int oldLength = incomingDataTableModel.getColumnCount();
		for (int i = oldLength; i < necessaryLength; i++) {
			incomingDataTableModel.addColumn(i);
		}

		String[] hexValues = new String[totalResponse.size()];

		for (int i = 0; i < totalResponse.size(); i++) {
			hexValues[i] = String.format("%02x", totalResponse.get(i));
		}
		incomingDataTableModel.addRow(hexValues);
		
		byte[] output = new byte[totalResponse.size()];
		for (int i = 0; i < output.length; i++) {
			output[i] = totalResponse.get(i);
		}
		return output;
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Since the frontend constructor itself takes care
				// of packing, setting location, setting itself
				// visible, and so on, just create a new one here
				// and do nothing else.
				new Hh802uGeneralFrontend();
			}
		});
	}
}
