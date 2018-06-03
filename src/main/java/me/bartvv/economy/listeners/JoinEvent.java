package me.bartvv.economy.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.bartvv.economy.Economy;

public class JoinEvent implements Listener {

	private Economy economy;

	public JoinEvent(Economy economy) {
		this.economy = economy;
		this.economy.getServer().getPluginManager().registerEvents(this, economy);
	}

	@EventHandler
	public void on(PlayerJoinEvent e) {
		if(e.getPlayer().hasPlayedBefore()) {
			this.economy.getUser(e.getPlayer().getUniqueId(), e.getPlayer().getName());	
		} else {
			this.economy.getUser(e.getPlayer().getUniqueId(), e.getPlayer().getName()).add(economy.getStartingMoney());
		}
	}

}