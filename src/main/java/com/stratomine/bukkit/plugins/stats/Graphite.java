package com.stratomine.bukkit.plugins.stats;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;

public class Graphite {
	
	private final StatsPlugin plugin;
	
	public Graphite(StatsPlugin plugin) {
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
			info("Sent \"%s\"", line.replaceAll("\n", "\\\\n"));
		} finally {
			socket.close();
		}
	}
	
	public static String generateLine(String metric, Object value) {
		return String.format("%s %s %s\n", metric, value, System.currentTimeMillis());
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
	
	private void info(String message, Object... objects) {
		log(Level.INFO, message, objects);
	}

	private void log(Level level, String message, Object... objects) {
		message = "[Graphite] " + message;
		getPlugin().log(level, message, objects);
	}
	
}
