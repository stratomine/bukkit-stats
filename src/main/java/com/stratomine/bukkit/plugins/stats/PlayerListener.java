package com.stratomine.bukkit.plugins.stats;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
	
	private static final String PLAYERS_METRIC = "players";
	
	private StatsPlugin plugin;
	
	public PlayerListener(StatsPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.increment(PLAYERS_METRIC);
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.decrement(PLAYERS_METRIC);
	}
	
}
