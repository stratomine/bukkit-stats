package com.stratomine.bukkit.plugins.stats;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

public class Graphite {
	
	private final String host;
	private final int port;
	
	public Graphite(String host) {
		this(host, 2003);
	}
	
	public Graphite(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void update(String metric, Object value) throws UnknownHostException, IOException {
		String line = generateLine(metric, value);
		Socket socket = new Socket(getHost(), getPort());
		
		try {
			Writer writer = new OutputStreamWriter(socket.getOutputStream());
			writer.write(line);
			writer.flush();
			writer.close();
		} finally {
			socket.close();
		}
	}
	
	public String generateLine(String metric, Object value) {
		return String.format("%s %s %s", metric, value, System.currentTimeMillis() / 1000);
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
}
