package com.itl.comm;

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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

import com.itl.swing.LogTextPane;

@SuppressWarnings("serial")
public class SerialPortFrontend extends JFrame implements ActionListener{
	SerialPortConnection serialPortConnection;
	
	JMenuItem connectMenuItem;
	JMenuItem closeMenuItem;
	JMenuItem exitMenuItem;
	
	LogTextPane incomingTextPane;
	DefaultTableModel incomingDataTableModel;
	JTable incomingDataTable;
	
	JTextField outgoingTextField;
	JButton sendButton;
	
	SerialPortSettingsDialog settingsDialog;
	
	public SerialPortFrontend() {
		settingsDialog = new SerialPortSettingsDialog(this);
		
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
		
		sendButton = new JButton("Send!");
		sendButton.addActionListener(this);
		
		incomingTextPane = new LogTextPane();
		incomingTextPane.setFocusable(false);
		JScrollPane incomingTextView = new JScrollPane(
				incomingTextPane,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		incomingTextView.setPreferredSize(new Dimension(400, 400));
		
		outgoingTextField = new JTextField();
		outgoingTextField.addActionListener(this);
		
		incomingDataTableModel = new DefaultTableModel(0, 0);
		incomingDataTable = new JTable(incomingDataTableModel);
		JScrollPane incomingDataTableScrollPane = new JScrollPane(
				incomingDataTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		incomingDataTable.setPreferredScrollableViewportSize(
				new Dimension(500, 400));
		incomingDataTable.setFillsViewportHeight(true);
		incomingDataTable.getTableHeader().setReorderingAllowed(false);
		incomingDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		incomingDataTable.setColumnSelectionAllowed(true);
		incomingDataTable.setRowSelectionAllowed(false);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		contentPanel.add(incomingTextView, c);
		
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		contentPanel.add(incomingDataTableScrollPane, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		contentPanel.add(outgoingTextField, c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		contentPanel.add(sendButton, c);
		
		this.setContentPane(contentPanel);
		this.setJMenuBar(menuBar);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (serialPortConnection != null) {
					serialPortConnection.close();
				}
				System.exit(0);
			}
		});
		
		this.setTitle("Serial Port Frontend");
		this.pack();
		this.setMinimumSize(this.getPreferredSize());
		this.setLocation(50, 50);
		this.setVisible(true);
		showSettingsAndConnect();
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Since the frontend constructor itself takes care
				// of packing, setting location, setting itself
				// visible, and so on, just create a new one here
				// and do nothing else.
				new SerialPortFrontend();
			}
		});
	}

	public void actionPerformed(ActionEvent evt) {
		if ((evt.getSource() == sendButton) || 
				(evt.getSource() == outgoingTextField)) {
			try {
				// write data out
				serialPortConnection.write(outgoingTextField.getText());
				incomingTextPane.append((outgoingTextField.getText() + "\n"), Color.GREEN);
				outgoingTextField.setText("");
				outgoingTextField.grabFocus();
				
				// Give instrument a chance to respond!
				// TODO: do this better
				Thread.sleep(100);
//				incomingTextPane.append(reader.read(), Color.RED);
				byte[] response = serialPortConnection.readBytes();
				
				if (response.length > 0) {
					while (response.length > incomingDataTableModel.getColumnCount()) {
						incomingDataTableModel.addColumn(
								incomingDataTableModel.getColumnCount());
					}
					// This is a bit of a kludge, but it seems to be necessary
					for (int i = 0; i < incomingDataTableModel.getColumnCount(); i++) {
						incomingDataTable.getColumnModel().getColumn(i).setPreferredWidth(25);
					}
					
					String[] hexValues = new String[response.length];
					char[] charValues = new char[response.length];
					
					for (int i = 0; i < response.length; i++) {
						hexValues[i] = String.format("%02x", response[i]);
						charValues[i] = (char)response[i];
					}
					incomingDataTableModel.addRow(hexValues);
					incomingTextPane.append(new String(charValues), Color.RED);

//					String s1 = String.format("%02x%02x", response[5], response[6]);
//					System.out.println(((double)Integer.parseInt(s1, 16))/10);
//					String s2 = String.format("%02x%02x", response[10], response[11]);
//					System.out.println(((double)Integer.parseInt(s2, 16))/10);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		} else if (evt.getSource() == connectMenuItem) {
			showSettingsAndConnect();
		} else if (evt.getSource() == closeMenuItem) {
			if (serialPortConnection != null) {
				serialPortConnection.close();
				this.setTitle("Serial Port Frontend");
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
}