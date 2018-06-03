package me.bartvv.economy.listeners;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.google.common.collect.Maps;

import me.bartvv.economy.Economy;

public class NoEssentialsEco implements Listener {
	
	Map<String, String> blockedCommands;
	
	public NoEssentialsEco(Economy economy) {
		this.blockedCommands = Maps.newHashMap();
		this.blockedCommands.put("ebalance", "balance");
		this.blockedCommands.put("ebal", "bal");
		this.blockedCommands.put("emoney", "money");
		this.blockedCommands.put("ebaltop", "baltop");
		this.blockedCommands.put("ebalancetop", "balancetop");
		this.blockedCommands.put("epay", "pay");
		this.blockedCommands.put("eeconomy", "economy");
		this.blockedCommands.put("eeco", "eco");
		
		this.blockedCommands.put("essentials:ebalance", "balance");
		this.blockedCommands.put("essentials:ebal", "bal");
		this.blockedCommands.put("essentials:emoney", "money");
		this.blockedCommands.put("essentials:ebaltop", "baltop");
		this.blockedCommands.put("essentials:ebalancetop", "balancetop");
		this.blockedCommands.put("essentials:epay", "pay");
		this.blockedCommands.put("essentials:eeconomy", "economy");
		this.blockedCommands.put("essentials:eeco", "eco");
		
		economy.getServer().getPluginManager().registerEvents(this, economy);
	}
	
	@EventHandler
	public void on(PlayerCommandPreprocessEvent e) {
		String command = e.getMessage().split(" ")[0].replace("/", "");
		
		System.out.println(command);
		
		if(blockedCommands.containsKey(command.toLowerCase())) {
			String toAdd = e.getMessage();
			
			toAdd = toAdd.replaceFirst("/" + command, "/" + blockedCommands.get(command));
			
			System.out.println(toAdd);
			
			e.setMessage(toAdd);
		}
	}

}