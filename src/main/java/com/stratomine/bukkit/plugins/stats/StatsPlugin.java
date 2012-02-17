package com.stratomine.bukkit.plugins.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class StatsPlugin extends JavaPlugin {

	private String namespace;
	private String graphiteHost;
	private int graphitePort;
	private StatsdClient statsd;
	
	private Map<String, Integer> counters;
	
	public void onEnable() {
		Configuration config = getConfig();
		
		namespace = config.getString("namespace");
		
		graphiteHost = config.getString("graphite.host");
		graphitePort = config.getInt("graphite.port", 2003);
		
		try {
			statsd = new StatsdClient(config.getString("statsd.host"), config.getInt("statsd.port"));
			if (config.contains("namespace")) {
				statsd.setNamespace(config.getString("namespace"));
			}
		} catch (Exception e) {
			error("%s while configuring statsd: %s", e.getClass().getName(), e.getMessage());
			setEnabled(false);
		}
		
		if (isEnabled()) {
			counters = new HashMap<String, Integer>();
			registerEvents();
			info("%s loaded", getDescription().getFullName());
			update("server.startups", 1);
		}
	}
	
	public void onDisable() {
		info("%s disabled", getDescription().getFullName());
		update("server.shutdowns", 1);
	}
	
	private void registerEvents() {
		PluginManager manager = getServer().getPluginManager();
		Listener listener;
		
		listener = new PlayerListener() {
			@Override
			public void onPlayerJoin(PlayerJoinEvent event) {
				increment("player.count");
				update("player.joins", 1);
			}

			@Override
			public void onPlayerQuit(PlayerQuitEvent event) {
				decrement("player.count");
				update("player.quits", 1);
			}

			@Override
			public void onPlayerChat(PlayerChatEvent event) {
				getStatsd().increment("player.chats");
			}

			@Override
			public void onPlayerBedEnter(PlayerBedEnterEvent event) {
				increment("player.sleeping");
			}

			@Override
			public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
				decrement("player.sleeping");
			}
		};
		manager.registerEvent(Event.Type.PLAYER_JOIN, listener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.PLAYER_QUIT, listener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.PLAYER_CHAT, listener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.PLAYER_BED_ENTER, listener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.PLAYER_BED_LEAVE, listener, Event.Priority.Normal, this);
		
		listener = new EntityListener() {
			@Override
			public void onEntityDeath(EntityDeathEvent event) {
				Entity entity = event.getEntity();
				if (entity instanceof Animals) {
					getStatsd().increment("deaths.animals");
				} else if (entity instanceof Monster) {
					getStatsd().increment("deaths.monsters");
				} else if (entity instanceof Player) {
					getStatsd().increment("deaths.players");
				}
			}
		};
		manager.registerEvent(Event.Type.ENTITY_DEATH, listener, Event.Priority.Normal, this);
		
		listener = new WorldListener() {
			@Override
			public void onChunkLoad(ChunkLoadEvent event) {
				getStatsd().increment("chunks.loaded", 0.1);
			}

			@Override
			public void onChunkPopulate(ChunkPopulateEvent event) {
				getStatsd().increment("chunks.populated");
			}

			@Override
			public void onChunkUnload(ChunkUnloadEvent event) {
				getStatsd().increment("chunks.unloaded", 0.1);
			}

			@Override
			public void onStructureGrow(StructureGrowEvent event) {
				getStatsd().increment("grows");
			}

			@Override
			public void onWorldSave(WorldSaveEvent event) {
				getStatsd().increment("world.saves");
			}
		};
		manager.registerEvent(Event.Type.CHUNK_LOAD, listener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.CHUNK_POPULATED, listener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.CHUNK_UNLOAD, listener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.STRUCTURE_GROW, listener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.WORLD_SAVE, listener, Event.Priority.Normal, this);
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
		GraphiteClient graphite = new GraphiteClient(this);
		
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
	
	public StatsdClient getStatsd() {
		return statsd;
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
