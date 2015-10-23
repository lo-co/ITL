package com.itl.comm.powerItl;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

@SuppressWarnings("serial")
public class PowerItlSimHandler extends JFrame implements HttpHandler {
	private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");
	
	/**
	 * We'll use this to create a new XML document every
	 * time we need a response.
	 */
	DocumentBuilder db;
	
	PowerReading[] powerReadings;
	TempReading[] tempReadings;
	
	/** represents a single channel's worth of data from a power meter */
	private class PowerReading {
		/** 0, 1, or 2 */
		private int chanNum;
		
		private double voltageBase;
		private double amperageBase;
		private double powerFactorBase;
		private double thdvBase;
		private double thdaBase;
		
		private double voltage;
		private double amperage;
		private double wattage;
		private double powerFactor;
		private double thdv;
		private double thda;
		
		public PowerReading(int chanNum, double voltageBase, double amperageBase,
				double powerFactorBase, double thdvBase, double thdaBase) {
			this.chanNum = chanNum;
			this.voltageBase = voltageBase;
			this.amperageBase = amperageBase;
			this.powerFactorBase = powerFactorBase;
			this.thdvBase = thdvBase;
			this.thdaBase = thdaBase;
		}
		
		public Element toXml(Document doc) {
			// fiddle with the readings so we can tell that it's updating
			// the "readings" vary with a period of 10 seconds
			Long now = System.currentTimeMillis();
			double d = Math.sin(now*(2*Math.PI/10e3));
			
			voltage = voltageBase + 5*d;
			amperage = amperageBase + 2*d;
			wattage = voltage * amperage;
			powerFactor = powerFactorBase + 0.1*d;
			thdv = thdvBase + 0.2*d;
			thda = thdaBase + 0.2*d;
			
			Element e = doc.createElement("powerReading");
			e.setAttribute("channel", String.format("%d", chanNum));
			
			Element voltageElement = doc.createElement("voltage");
			voltageElement.setAttribute("units", "Volts");
			voltageElement.appendChild(doc.createTextNode(
					String.format("%f", voltage)));
			e.appendChild(voltageElement);

			Element amperageElement = doc.createElement("amperage");
			amperageElement.setAttribute("units", "Amps");
			amperageElement.appendChild(doc.createTextNode(
					String.format("%f", amperage)));
			e.appendChild(amperageElement);

			Element wattageElement = doc.createElement("wattage");
			wattageElement.setAttribute("units", "Watts");
			wattageElement.appendChild(doc.createTextNode(
					String.format("%f", wattage)));
			e.appendChild(wattageElement);

			Element pfElement = doc.createElement("powerFactor");
			pfElement.appendChild(doc.createTextNode(
					String.format("%f", powerFactor)));
			e.appendChild(pfElement);

			Element vdElement = doc.createElement("voltageDistortion");
			vdElement.appendChild(doc.createTextNode(
					String.format("%f", thdv)));
			e.appendChild(vdElement);
			
			Element adElement = doc.createElement("amperageDistortion");
			adElement.appendChild(doc.createTextNode(
					String.format("%f", thda)));
			e.appendChild(adElement);

			return e;
		}
	}
	
	/** represents a single channel's worth of data from a thermocouple reader */	
	private class TempReading {
		/** 0 or 1 */
		private int chanNum;
		
		private double tempBase;
		
		private double temperature;
		
		public TempReading(int chanNum, double tempBase) {
			this.chanNum = chanNum;
			this.tempBase = tempBase;
		}
		
		public Element toXml(Document doc) {
			// fiddle with the temperature so we can tell that it's updating
			// the "temperature" vary by +/- 0.5°C over the course of 10
			// seconds
			Long now = System.currentTimeMillis();
			double d = Math.sin(now*(2*Math.PI/10e3));
			temperature = tempBase + 0.5*d;
			
			Element e = doc.createElement("temperatureReading");
			
			e.setAttribute("channel", String.format("%d", chanNum));
			e.setAttribute("units", "deg C");
			e.appendChild(doc.createTextNode(String.format("%f", temperature)));

			return e;
		}
	}
	
	public PowerItlSimHandler() throws ParserConfigurationException, IOException {
		powerReadings = new PowerReading[3];
		powerReadings[0] = new PowerReading(0, 120, 10, 0.9, 15, 16);
		powerReadings[1] = new PowerReading(1, 60, 5, 0.8, 25, 26);
		powerReadings[2] = new PowerReading(2, 30, 4, 0.6, 45, 46);
		
		tempReadings = new TempReading[2];
		tempReadings[0] = new TempReading(0, 25);
		tempReadings[1] = new TempReading(1, 27);
		
		db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		// server stuff
		String hostname = Advapi32Util.registryGetStringValue(
				WinReg.HKEY_LOCAL_MACHINE, 
				"SYSTEM\\CurrentControlSet\\Control\\ComputerName\\ComputerName", 
				"ComputerName");
		int serverPort = 13986;
		InetSocketAddress address = new InetSocketAddress(
				hostname, serverPort);
		HttpServer server = HttpServer.create(address, 0);
		String context = "/daq";
		
		server.createContext(context, this);
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		
		// Some basic UI stuff, just enough to have something to start and 
		// exit on close
		this.setTitle("PowerItlSimHandler Frame");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setMinimumSize(new Dimension(200, 200));
		this.setVisible(true);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PowerItlSimHandler h = new PowerItlSimHandler();
				} catch (ParserConfigurationException | IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET")) {
			Long now = System.currentTimeMillis();

			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "text/xml");
			exchange.sendResponseHeaders(200, 0);

			// create the XML document
			Document doc = db.newDocument();
			Element root = doc.createElement("StabilizationMeasurement");
			doc.appendChild(root);

			Element time = doc.createElement("time");
			time.appendChild(doc.createTextNode(
					String.format("%d", now)));
			root.appendChild(time);

			Element formattedTime = doc.createElement("formattedTime");
			formattedTime.appendChild(doc.createTextNode(
					SDF.format(now)));
			root.appendChild(formattedTime);

			for (int i = 0; i < powerReadings.length; i++) {
				root.appendChild(powerReadings[i].toXml(doc));
			}
			
			for (int i = 0; i < tempReadings.length; i++) {
				root.appendChild(tempReadings[i].toXml(doc));
			}

			OutputStream out = exchange.getResponseBody();
			OutputFormat format = new OutputFormat(doc);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(doc);
			out.close();
		}
	}
}
