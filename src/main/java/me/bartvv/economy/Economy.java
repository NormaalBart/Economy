package me.bartvv.economy;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;

import me.bartvv.economy.api.VaultRegister;
import me.bartvv.economy.commands.BalanceCMD;
import me.bartvv.economy.commands.BaltopCMD;
import me.bartvv.economy.commands.EconomyCMD;
import me.bartvv.economy.commands.MigrateEcoEssentialsCMD;
import me.bartvv.economy.commands.PayCMD;
import me.bartvv.economy.listeners.JoinEvent;
import me.bartvv.economy.listeners.NoEssentialsEco;
import me.bartvv.economy.listeners.QuitEvent;
import me.bartvv.economy.manager.User;
import me.bartvv.economy.sql.DataSQL;
import me.bartvv.economy.sql.MySQL;

public class Economy extends JavaPlugin {

	private transient BigDecimal startMoney;
	private transient Map<UUID, User> users;
	private transient MySQL getDataSQL;
	private transient static FileManager messages, options;

	@Override
	public void onEnable() {

		if (!(getServer().getPluginManager().getPlugin("Vault") != null
				&& getServer().getPluginManager().isPluginEnabled("Vault"))) {
			getLogger().log(Level.WARNING, "Vault not found!");
			getLogger().log(Level.WARNING, "Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.users = Maps.newHashMap();
		this.getDataSQL = new DataSQL(this);

		Class<net.milkbowl.vault.economy.Economy> eco = net.milkbowl.vault.economy.Economy.class;
		VaultRegister vaultRegister = new VaultRegister(this);
		Plugin plugin = getServer().getPluginManager().getPlugin("Vault");

		getServer().getServicesManager().register(eco, vaultRegister, plugin, ServicePriority.Highest);
	
		getServer().getOnlinePlayers().forEach(
				player -> this.users.put(player.getUniqueId(), new User(player.getUniqueId(), player.getName())));

		new JoinEvent(this);
		new QuitEvent(this);
		new BalanceCMD(this);
		new BaltopCMD(this);
		new PayCMD(this);
		new NoEssentialsEco(this);
		new EconomyCMD(this);
		new MigrateEcoEssentialsCMD(this);

		messages = new FileManager(this, "messages.yml", 10);
		options = new FileManager(this, "options.yml", 10);

		startMoney = new BigDecimal(options.getDouble("startMoney"));

		new BukkitRunnable() {

			@Override
			public void run() {
				for (User user : getUsers()) {
					user.save(false);
				}
			}

		}.runTaskTimerAsynchronously(this, (5 * 20 * 60), (5 * 20 * 60));
	}

	@Override
	public void onDisable() {

		for (User user : getUsers()) {
			user.save(false);
		}

		getData().close();
		messages.save();
	}

	public static FileManager getMessages() {
		return messages;
	}
	
	public BigDecimal getStartingMoney() {
		return this.startMoney;
	}

	
	public User getUser(UUID uuid, String name) {
		User user = this.users.get(uuid);
		if (user == null) {
			this.users.put(uuid, new User(uuid, name));
			user = this.users.get(uuid);
		}
		return user;
	}

	public void removeUser(UUID uuid) {
		this.users.remove(uuid);
	}

	public Collection<User> getUsers() {
		if (!users.isEmpty()) {
			return this.users.values();
		}
		return Collections.emptyList();
	}

	public DataSQL getData() {
		return (DataSQL) this.getDataSQL;
	}

	public static FileManager getOptions() {
		return options;
	}

	public User getUser(String playerName) {
		Player player = Bukkit.getPlayer(playerName);
		
		UUID uuid;
		
		if(player == null) {
			uuid = Utils.getUUIDFromString(playerName);
		} else {
			uuid = player.getUniqueId();
		}
		
		return getUser(uuid, playerName);
	}
}