package me.bartvv.economy.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.bartvv.economy.Economy;
import me.bartvv.economy.manager.User;

public class QuitEvent implements Listener {

	private Economy economy;

	public QuitEvent(Economy economy) {
		this.economy = economy;
		this.economy.getServer().getPluginManager().registerEvents(this, economy);
	}

	@EventHandler
	public void on(PlayerQuitEvent e) {
		User user = this.economy.getUser(e.getPlayer().getUniqueId(), e.getPlayer().getName());
		user.save(true);
		this.economy.removeUser(e.getPlayer().getUniqueId());
	}
}