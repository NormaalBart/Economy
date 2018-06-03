package me.bartvv.economy.commands;

import static me.bartvv.economy.Utils.displayCurrency;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.bartvv.economy.Economy;
import me.bartvv.economy.interfaces.CallBackSingle;
import me.bartvv.economy.manager.User;
import me.bartvv.economy.message.Message;

public class BalanceCMD implements CommandExecutor {

	private Economy economy;

	public BalanceCMD(Economy economy) {
		this.economy = economy;
		this.economy.getCommand("balance").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String cmdLabel, String[] args) {
		User target = null;
		Player targetPlayer = null;

		if (args.length == 0) {
			if (cs instanceof Player) {
				targetPlayer = (Player) cs;
				target = this.economy.getUser(targetPlayer.getUniqueId(), targetPlayer.getName());
			} else {
				cs.sendMessage("Please specify player!");
				return true;
			}
		} else if (args.length == 1) {
			targetPlayer = Bukkit.getPlayer(args[0]);
			if (targetPlayer == null) {
				if (cs.hasPermission("economy.balance.offline")) {
					cs.sendMessage(Message.GETTING_BALANCE.toString().replace("{PLAYER}", args[0]));

					this.getBalance(args[0], new CallBackSingle<BigDecimal>() {
						@Override
						public void onQueryDone(BigDecimal object) {
							if (object == null) {
								cs.sendMessage(Message.PLAYER_NEVER_JOINED.toString());
								return;
							}
							cs.sendMessage(Message.BALANCE.toString().replace("{BALANCE}", displayCurrency(object))
									.replace("{PLAYER}", args[0]));
							return;
						}

					});
					return true;
				}
				cs.sendMessage(Message.NO_PLAYER.toString());
				return true;
			}

			target = this.economy.getUser(targetPlayer.getUniqueId(), targetPlayer.getName());
		}

		cs.sendMessage(Message.BALANCE.toString().replace("{BALANCE}", displayCurrency(target.getTotal()))
				.replace("{PLAYER}", targetPlayer.getName()));
		return true;
	}

	private void getBalance(String name, CallBackSingle<BigDecimal> callback) {

		String SELECT_ECONOMY = "SELECT money FROM " + economy.getData().getServerName() + " WHERE name = ?";
		new BukkitRunnable() {

			@Override
			public void run() {
				try {
					Connection connection = economy.getData().getHikari().getConnection();
					PreparedStatement statement = connection.prepareStatement(SELECT_ECONOMY);
					statement.setString(1, name);

					ResultSet resultSet = statement.executeQuery();

					BigDecimal decimal = resultSet.next() ? resultSet.getBigDecimal("money") : null;

					statement.close();
					connection.close();

					callback.onQueryDone(decimal);

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(economy);

	}
}