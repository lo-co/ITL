package com.itl.comm;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SimpleHandler implements HttpHandler {
	String text;
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET")) {
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "text/xml");
			exchange.sendResponseHeaders(200, 0);
			
			OutputStream responseBody = exchange.getResponseBody();
			byte[] bodyText = text.getBytes();
			responseBody.write(bodyText);
			responseBody.close();
		}
	}
}
