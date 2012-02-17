package com.stratomine.bukkit.plugins.stats;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

public class GraphiteClient {
	
	private final StatsPlugin plugin;
	
	public GraphiteClient(StatsPlugin plugin) {
		this.plugin = plugin;
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
	
	public static String generateLine(String metric, Object value) {
		return String.format("stats.%s %s %s\n", metric, value, System.currentTimeMillis() / 1000);
	}
	
	public StatsPlugin getPlugin() {
		return plugin;
	}
	
	public String getHost() {
		return getPlugin().getGraphiteHost();
	}
	
	public int getPort() {
		return getPlugin().getGraphitePort();
	}
	
}
