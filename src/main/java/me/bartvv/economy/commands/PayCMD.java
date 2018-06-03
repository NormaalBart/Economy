package me.bartvv.economy.commands;

import static me.bartvv.economy.Utils.displayCurrency;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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

public class PayCMD implements CommandExecutor {

	private Economy economy;

	public PayCMD(Economy economy) {
		this.economy = economy;
		this.economy.getCommand("pay").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command command, String label, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage("You need to be a player");
			return true;
		}

		Player player = (Player) cs;
		User userPlayer = this.economy.getUser(player.getUniqueId(), player.getName());

		if (args.length != 2) {
			player.sendMessage(Message.WRONG_PAY_FORMAT.toString());
			return true;
		}
		
		String stringAmount = args[1].replaceAll("[^0-9\\.]", "").replace("-", "");

		if (stringAmount.length() < 1) {
			player.sendMessage(Message.WRONG_PAY_FORMAT.toString());
			return true;
		}

		BigDecimal decimal;
		try {
			decimal = new BigDecimal(stringAmount);
		} catch (NumberFormatException nfe) {
			player.sendMessage(Message.WRONG_PAY_FORMAT.toString());
			return true;
		}
		
		if (userPlayer.getTotal().compareTo(decimal) < 0) {
			player.sendMessage(Message.NOT_ENOUGN_MONEY.toString());
			return true;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			userPlayer.remove(decimal);
			addOfflineMoney(args[0], decimal, new CallBackSingle<Success>() {

				@Override
				public void onQueryDone(Success success) {
					if(success == Success.DONE) {
						player.sendMessage(Message.MONEY_PAY.toString().replace("{PLAYER}",args[0]).replace("{MONEY}",
								displayCurrency(decimal)));
						return;
					} else if (success == Success.NO_PLAYER) {
						player.sendMessage(Message.NO_PLAYER.toString());
						return;
					} else {
						player.sendMessage(Message.ERROR.toString());
						return;
					}
				}
				
			});
			return true;
		}

		User userTarget = this.economy.getUser(target.getUniqueId(), target.getName());

		userPlayer.remove(decimal);
		userTarget.add(decimal);

		player.sendMessage(Message.MONEY_PAY.toString().replace("{PLAYER}", target.getName()).replace("{MONEY}",
				displayCurrency(decimal)));
		target.sendMessage(Message.MONEY_RECEIVE.toString().replace("{PLAYER}", player.getName()).replace("{MONEY}",
				displayCurrency(decimal)));
		return true;
	}

	private void addOfflineMoney(String name, BigDecimal toAdd, CallBackSingle<Success> callBack) {
		new BukkitRunnable() {

			@Override
			public void run() {
				Connection connection = null;
				try {
					connection = economy.getData().getHikari().getConnection();

					UUID uuid = getUUID(connection);
					if (uuid == null) {
						callBack.onQueryDone(Success.NO_PLAYER);
						connection.close();
						return;
					}

					BigDecimal money = getMoney(connection);
					if (money == null) {
						callBack.onQueryDone(Success.NO_PLAYER);
						connection.close();
						return;
					}

					money = money.add(toAdd);

					PreparedStatement statement = connection.prepareStatement(
							"INSERT INTO " + economy.getData().getServerName() + " VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE name=?, money=?");

					statement.setString(1, uuid.toString());
					statement.setString(2, name);
					statement.setBigDecimal(3, money);
					statement.setString(4, name);
					statement.setBigDecimal(5, money);

					statement.execute();
					
					callBack.onQueryDone(Success.DONE);

					statement.close();
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					callBack.onQueryDone(Success.NO_PLAYER);
				} finally {
					try {
						if (connection != null) {
							connection.close();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			private String GET_STRING = "SELECT {VARIABLE} FROM " + economy.getData().getServerName() + " WHERE name=?";

			private UUID getUUID(Connection connection) {
				UUID uuid = null;

				try {
					PreparedStatement uuidStatement = connection
							.prepareStatement(GET_STRING.replace("{VARIABLE}", "uuid"));
					uuidStatement.setString(1, name);

					ResultSet moneyResultSet = uuidStatement.executeQuery();

					if (moneyResultSet.next()) {
						uuid = UUID.fromString(moneyResultSet.getString("uuid"));
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}

				return uuid;
			}

			private BigDecimal getMoney(Connection connection) {
				BigDecimal currentMoney = null;

				try {
					PreparedStatement moneyStatement = connection
							.prepareStatement(GET_STRING.replace("{VARIABLE}", "money"));
					moneyStatement.setString(1, name);

					ResultSet moneyResultSet = moneyStatement.executeQuery();

					if (moneyResultSet.next()) {
						currentMoney = moneyResultSet.getBigDecimal("money");
					} else {
						currentMoney = economy.getStartingMoney();
						;
					}
				} catch (SQLException exception) {
					exception.printStackTrace();
				}

				return currentMoney;
			}

		}.runTaskAsynchronously(this.economy);
	}

	private enum Success {
		DONE, NO_PLAYER, ERROR;
	}
}