package com.itl.comm;

import gnu.io.CommPortIdentifier;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SerialPortSettingsDialog extends JDialog 
		implements ActionListener {
	public final static String[] PARITY_OPTIONS = {
		"None", "Odd", "Even", "Mark", "Space"};
	
	JComboBox<String> serialPortComboBox;
	JButton refreshSerialPortVectorButton;
	JFormattedTextField baudRateField;
	JFormattedTextField dataBitsField;
	JFormattedTextField stopBitsField;
	JComboBox<String> parityComboBox;
	JButton connectButton;
	JButton cancelButton;
	
	JOptionPane optionPane;
	
	int chosenOption;
	public static final int CANCEL_OPTION = 0;
	public static final int CONNECT_OPTION = 1;
	
	public SerialPortSettingsDialog(Component parent) {
		initializeComponents();
		placeComponents();
		
		this.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		this.setTitle("Serial Port Settings");
		this.pack();
		this.setResizable(false);
	}
	
	private void initializeComponents() {
		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		numberFormat.setGroupingUsed(false);
		
		serialPortComboBox = new JComboBox<String>();
		if (serialPortComboBox.getModel().getSize() > 1) {
			serialPortComboBox.setSelectedIndex(1);
		}
		serialPortComboBox.setToolTipText(
				"Choose a serial port");
		serialPortComboBox.setPreferredSize(new Dimension(69, 25));
		
		refreshSerialPortVectorButton = new JButton("Refresh");
		refreshSerialPortVectorButton.setToolTipText(
				"Refresh the list of serial ports");
		refreshSerialPortVectorButton.addActionListener(this);
		
		baudRateField = new JFormattedTextField(numberFormat);
		baudRateField.setColumns(5);
		baudRateField.setValue(SerialPortConnection.DEFAULT_BAUD_RATE);
		baudRateField.setToolTipText(
				"Set the baud rate. Common choices are 9600 and 19200.");
		
		dataBitsField = new JFormattedTextField(numberFormat);
		dataBitsField.setColumns(2);
		dataBitsField.setValue(SerialPortConnection.DEFAULT_DATA_BITS);
		dataBitsField.setToolTipText(
				"Set the number of data bits.  Usually 8.");
		
		stopBitsField = new JFormattedTextField(numberFormat);
		stopBitsField.setColumns(2);
		stopBitsField.setValue(SerialPortConnection.DEFAULT_STOP_BITS);
		stopBitsField.setToolTipText(
				"Set the number of stop bits.  Usually 1 or 2.");
		
		parityComboBox = new JComboBox<String>(
				new DefaultComboBoxModel<String>(PARITY_OPTIONS));
		parityComboBox.setSelectedIndex(SerialPortConnection.DEFAULT_PARITY);
		parityComboBox.setToolTipText(
				"Choose parity. 'None' will often work.");
		
		connectButton = new JButton("Connect");
		connectButton.setToolTipText(
				"Connect to the serial port using these options.");
		connectButton.addActionListener(this);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText(
				"Close this window without connecting.");
		cancelButton.addActionListener(this);
	}
	
	private void placeComponents() {
		JPanel settingsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints settingsConstraints = new GridBagConstraints();
		Insets minimumInsets = new Insets(2, 4, 2, 4);
		settingsConstraints.insets = minimumInsets;
		
		settingsConstraints.gridx = 0;
		settingsConstraints.gridy = 0;
		settingsConstraints.gridwidth = 1;
		settingsConstraints.gridheight = 1;
		settingsConstraints.anchor = GridBagConstraints.LINE_START;
		settingsConstraints.fill = GridBagConstraints.HORIZONTAL;
		settingsConstraints.weightx = 0;
		settingsConstraints.weighty = 0;
		settingsPanel.add(new JLabel("Port"), settingsConstraints);
		settingsConstraints.gridy++;
		settingsPanel.add(new JLabel("Baud Rate"), settingsConstraints);
		settingsConstraints.gridy++;
		settingsPanel.add(new JLabel("Data Bits"), settingsConstraints);
		settingsConstraints.gridy++;
		settingsPanel.add(new JLabel("Stop Bits"), settingsConstraints);
		settingsConstraints.gridy++;
		settingsPanel.add(new JLabel("Parity"), settingsConstraints);
		
		int maxComponentHeight = 0;
		maxComponentHeight = Math.max(
				maxComponentHeight, serialPortComboBox.getPreferredSize().height);
		maxComponentHeight = Math.max(
				maxComponentHeight, baudRateField.getPreferredSize().height);
		maxComponentHeight = Math.max(
				maxComponentHeight, dataBitsField.getPreferredSize().height);
		maxComponentHeight = Math.max(
				maxComponentHeight, stopBitsField.getPreferredSize().height);
		maxComponentHeight = Math.max(
				maxComponentHeight, parityComboBox.getPreferredSize().height);
		maxComponentHeight = Math.max(
				maxComponentHeight, refreshSerialPortVectorButton.getPreferredSize().height);
		
		settingsConstraints.insets = new Insets(
				0 + maxComponentHeight - serialPortComboBox.getPreferredSize().height, 
				4, 
				2, 
				4);
		settingsConstraints.gridx = 1;
		settingsConstraints.gridy = 0;
		settingsConstraints.gridwidth = 1;
		settingsConstraints.gridheight = 1;
		settingsConstraints.anchor = GridBagConstraints.LINE_START;
		settingsConstraints.fill = GridBagConstraints.NONE;
		settingsConstraints.weightx = 0;
		settingsConstraints.weighty = 0;
		settingsPanel.add(serialPortComboBox, settingsConstraints);
		
		settingsConstraints.insets = new Insets(
				0 + maxComponentHeight - baudRateField.getPreferredSize().height, 
				4, 
				2, 
				4);
		settingsConstraints.gridy++;
		settingsPanel.add(baudRateField, settingsConstraints);
		
		settingsConstraints.insets = new Insets(
				0 + maxComponentHeight - dataBitsField.getPreferredSize().height, 
				4, 
				2, 
				4);
		settingsConstraints.gridy++;
		settingsPanel.add(dataBitsField, settingsConstraints);
		
		settingsConstraints.insets = new Insets(
				0 + maxComponentHeight - stopBitsField.getPreferredSize().height, 
				4, 
				2, 
				4);
		settingsConstraints.gridy++;
		settingsPanel.add(stopBitsField, settingsConstraints);
		
		settingsConstraints.insets = new Insets(
				0 + maxComponentHeight - parityComboBox.getPreferredSize().height, 
				4, 
				2, 
				4);
		settingsConstraints.gridy++;
		settingsPanel.add(parityComboBox, settingsConstraints);
		
		settingsConstraints.insets = new Insets(
				0 + maxComponentHeight - refreshSerialPortVectorButton.getPreferredSize().height, 
				4, 
				2, 
				4);
		settingsConstraints.gridx = 2;
		settingsConstraints.gridy = 0;
		settingsConstraints.gridwidth = 1;
		settingsConstraints.gridheight = 1;
		settingsConstraints.anchor = GridBagConstraints.LINE_START;
		settingsConstraints.fill = GridBagConstraints.NONE;
		settingsConstraints.weightx = 0;
		settingsConstraints.weighty = 0;
		settingsPanel.add(refreshSerialPortVectorButton, settingsConstraints);
		
		JPanel contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.PAGE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.weighty = 0;
		contentPanel.add(settingsPanel, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		c.weighty = 0;
		contentPanel.add(connectButton, c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		c.weighty = 0;
		contentPanel.add(cancelButton, c);
		
		this.setContentPane(contentPanel);
	}

	@SuppressWarnings("unchecked")
	public Vector<String> getSerialPortVector() {
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
		if (evt.getSource() == refreshSerialPortVectorButton) {
			serialPortComboBox.setModel(
					new DefaultComboBoxModel<String>(getSerialPortVector()));
		} else if (evt.getSource() == connectButton) {
			chosenOption = CONNECT_OPTION;
			this.setVisible(false);
			//TODO: stub
		} else if (evt.getSource() == cancelButton) {
			chosenOption = CANCEL_OPTION;
			this.setVisible(false);
			//TODO: stub
		}
	}
	
	public int showDialog() {
		// refresh in case the serial port vector has changed
		// (which it probably has, if the user is trying to connect
		// again!)
		serialPortComboBox.setModel(
				new DefaultComboBoxModel<String>(getSerialPortVector()));
		setVisible(true);
		return chosenOption;
	}
	
	public String getPortName() {
		return (String)serialPortComboBox.getSelectedItem();
	}
	
	public int getBaudRate() {
		return (Integer)baudRateField.getValue();
	}
	
	public int getDataBits() {
		return (Integer)dataBitsField.getValue();
	}
	
	public int getStopBits() {
		return (Integer)stopBitsField.getValue();
	}
	
	public int getParity() {
		return parityComboBox.getSelectedIndex();
	}
}
