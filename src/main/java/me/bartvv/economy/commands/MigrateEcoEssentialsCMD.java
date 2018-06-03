package me.bartvv.economy.commands;

import java.io.File;
import java.math.BigDecimal;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.bartvv.economy.Economy;
import me.bartvv.economy.interfaces.CallBackSingle;
import me.bartvv.economy.message.Message;

public class MigrateEcoEssentialsCMD implements CommandExecutor {

	private Economy economy;

	public MigrateEcoEssentialsCMD(Economy economy) {
		this.economy = economy;
		economy.getCommand("migratecoessentials").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command command, String label, String[] args) {
		if (!cs.hasPermission("economy.mee")) {
			cs.sendMessage(Message.NO_PERMISSIONS.toString());
			return true;
		}

		cs.sendMessage(Message.MIGRATE_DONE.toString());

		startMigrating(new CallBackSingle<MigrateEcoEssentialsCMD.Success>() {

			@Override
			public void onQueryDone(Success object) {

				if (object == Success.DONE) {
					cs.sendMessage(Message.MIGRATE_DONE.toString());
					return;
				} else {
					cs.sendMessage(Message.MIGRATE_ERROR.toString());
					return;
				}

			}
		});

		return true;
	}

	private void startMigrating(CallBackSingle<Success> callBack) {
		new BukkitRunnable() {

			public void run() {
				Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");

				if (plugin == null) {
					callBack.onQueryDone(Success.ERROR);
					throw new RuntimeException("Essentials not installed!");
				}

				File userDir = new File(plugin.getDataFolder() + File.separator + "userdata");
				
				if (userDir.isDirectory()) {
					for (File toScan : userDir.listFiles()) {
						try {
							if (!toScan.getName().endsWith(".yml"))
								continue;
							String stringUUID = toScan.getName().replace(".yml", "");
							UUID uuid = UUID.fromString(stringUUID);
							YamlConfiguration config = YamlConfiguration.loadConfiguration(toScan);
							String accountName = config.getString("lastAccountName");
							String decimal = config.getString("money", "");
							if(decimal == null || decimal.equalsIgnoreCase("")) {
								decimal = "0";
							}
							BigDecimal money = new BigDecimal(decimal);
							economy.getData().save(uuid, accountName, money, false);
						} catch (Exception exception) {
							exception.printStackTrace();
							callBack.onQueryDone(Success.ERROR);
							return;
						}
					}
				} else {
					callBack.onQueryDone(Success.ERROR);
					throw new RuntimeException("Not a directory!");
				}
				callBack.onQueryDone(Success.DONE);
			}
		}.runTaskAsynchronously(this.economy);
	}

	private enum Success {
		DONE, ERROR;
	}
}
