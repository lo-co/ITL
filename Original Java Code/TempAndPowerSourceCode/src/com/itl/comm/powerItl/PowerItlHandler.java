package com.itl.comm.powerItl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itl.comm.PowerMeter;
import com.itl.comm.ThermocoupleDaq;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.xml.internal.ws.developer.MemberSubmissionEndpointReference.Elements;

public class PowerItlHandler implements HttpHandler {
	/**
	 * If a reading is longer than 10e9 nanoseconds, 
	 * it's too old to be considered valid.
	 */
	public static final long TOO_OLD = 10000000000L;
	
	SimpleDateFormat sdf;
	
	/**
	 * The PowerMeter object from which we'll
	 * get power readings (as well as sample time)
	 */
	PowerMeter pm;

	/** 
	 * The ThermocoupleDaq object from which we'll 
	 * get temperature readings (as well as sample time)
	 */
	ThermocoupleDaq tcd;
	
	/**
	 * We'll use this to create a new XML document every
	 * time we need a response.
	 */
	DocumentBuilder db;
	
	/**
	 * @param sdf
	 * @param pm
	 * @param tcd
	 * @throws ParserConfigurationException in case we somehow
	 * can't create a new DocumentBuilder from the 
	 * DocumentBuilderFactory
	 */
	public PowerItlHandler(SimpleDateFormat sdf, PowerMeter pm, ThermocoupleDaq tcd) 
			throws ParserConfigurationException {
		
		this.sdf = sdf;
		this.pm = pm;
		this.tcd = tcd;
		
		db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
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
					sdf.format(now)));
			root.appendChild(formattedTime);
			
			if (pm != null) {
				// 2013-01-15: Nick wants to cover the case that there's
				// no instrument connected.
				
				Element[] powerReadings = pm.toXml(doc);
				for (int i = 0; i < powerReadings.length; i++) {
					root.appendChild(powerReadings[i]);
				}
			}
			
			if (tcd != null) {
				// 2013-01-15: Nick wants to cover the case that there's
				// no instrument connected. So if there's no thermocouple
				// DAQ wanted, there's no point in creating new elements.
				// In that case, it's probably more self-consistent for
				// any application that receives this XML object to understand
				// that there are no thermocouple elements, not expect them
				// but have to parse through them.
				
				Element[] temperatureReadings;
				if ((System.nanoTime() - tcd.getSampleTime()) <= TOO_OLD) {
					// Note that the only Thermocouple Daq that's at all likely to
					// return data too slowly to meet our requirements is the OmegaHH802U.
					temperatureReadings = tcd.toXml(doc);					
				} else {
					temperatureReadings = new Element[tcd.getNumChannels()];
					for (int i = 0; i < temperatureReadings.length; i++) {
						temperatureReadings[i] = doc.createElement("temperatureReading");
						temperatureReadings[i].appendChild(doc.createTextNode("Data is stale"));
					}
				}
				
				for (int i = 0; i < temperatureReadings.length; i++) {
					root.appendChild(temperatureReadings[i]);
				}
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
