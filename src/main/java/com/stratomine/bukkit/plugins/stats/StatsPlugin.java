package com.stratomine.bukkit.plugins.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class StatsPlugin extends JavaPlugin {

	private String namespace;
	private String graphiteHost;
	private int graphitePort;
	
	private Map<String, Integer> counters;
	
	public void onEnable() {
		counters = new HashMap<String, Integer>();
		
		Configuration config = getConfig();
		namespace = config.getString("namespace");
		graphiteHost = config.getString("graphite.host");
		graphitePort = config.getInt("graphite.port", 2003);
		
		registerEvents();
		
		info("%s loaded", getDescription().getFullName());
	}
	
	public void onDisable() {
		// Do nothing
	}
	
	private void registerEvents() {
		PluginManager manager = getServer().getPluginManager();
		
		Listener playerListener = new org.bukkit.event.player.PlayerListener() {
			@Override
			public void onPlayerJoin(PlayerJoinEvent event) {
				increment("players");
			}

			@Override
			public void onPlayerQuit(PlayerQuitEvent event) {
				decrement("players");
			}
		};
		manager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
	}
	
	public void increment(String metric, int delta) {
		int value = delta;
		if (getCounters().containsKey(metric)) {
			value += counters.get(metric);
		}
		getCounters().put(metric, value);
		update(metric, value);
	}
	
	public void increment(String metric) {
		increment(metric, 1);
	}
	
	public void decrement(String metric, int delta) {
		increment(metric, delta);
	}
	
	public void decrement(String metric) {
		decrement(metric, -1);
	}
	
	public void update(String metric, int value) {
		metric = normalizeMetric(metric);
		Graphite graphite = new Graphite(this);
		
		try {
			graphite.update(metric, value);
		} catch (Exception e) {
			error("Graphite: %s while attempting to update %s to %d: %s", e.getClass().getName(), metric, value, e.getMessage());
		}
	}
	
	private String normalizeMetric(String metric) {
		return normalizeMetric(getNamespace(), metric);
	}
	
	public static String normalizeMetric(String namespace, String metric) {
		return String.format("%s.%s", namespace.replaceFirst("\\.*$", "").trim(), metric.trim());
	}
	
	public String getNamespace() {
		return namespace;
	}

	public String getGraphiteHost() {
		return graphiteHost;
	}

	public int getGraphitePort() {
		return graphitePort;
	}

	private Map<String, Integer> getCounters() {
		return counters;
	}

	protected Logger getLogger() {
		return getServer().getLogger();
	}
	
	public void log(Level level, String message, Object... objects) {
		message = "[" + getDescription().getName() + "] " + String.format(message, objects);
		getLogger().log(level, message);
	}
	
	public void info(String message, Object... objects) {
		log(Level.INFO, message, objects);
	}
	
	public void error(String message, Object... objects) {
		log(Level.SEVERE, message, objects);
	}
	
}
