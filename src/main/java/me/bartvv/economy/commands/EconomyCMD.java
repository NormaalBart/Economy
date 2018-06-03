package me.bartvv.economy.commands;

import static me.bartvv.economy.Utils.displayCurrency;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import me.bartvv.economy.Economy;
import me.bartvv.economy.interfaces.CallBackSingle;
import me.bartvv.economy.manager.User;
import me.bartvv.economy.message.Message;

public class EconomyCMD implements CommandExecutor, TabCompleter {

	private Economy economy;

	public EconomyCMD(Economy economy) {
		this.economy = economy;
		economy.getCommand("economy").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String cmdLabel, String[] args) {

		if (!cs.hasPermission("economy.*") && !cs.hasPermission("economy.give") && !cs.hasPermission("economy.take")
				&& !cs.hasPermission("economy.set") && !cs.hasPermission("economy.reset")) {
			cs.sendMessage(Message.NO_PERMISSIONS.toString());
			return true;
		}

		if (args.length < 2) {
			cs.sendMessage(Message.WRONG_USAGE_ECONOMY.toString());
			return true;
		}

		EconomyCMD.EconomyCommand economyCommand;

		if (args[0].equalsIgnoreCase("give")) {
			if (!cs.hasPermission("economy.give") && !cs.hasPermission("economy.*")) {
				cs.sendMessage(Message.NO_PERMISSIONS.toString());
				return true;
			}
			economyCommand = EconomyCommand.GIVE;
		} else if (args[0].equalsIgnoreCase("take")) {
			if (!cs.hasPermission("economy.take") && !cs.hasPermission("economy.*")) {
				cs.sendMessage(Message.NO_PERMISSIONS.toString());
				return true;
			}
			economyCommand = EconomyCommand.TAKE;
		} else if (args[0].equalsIgnoreCase("set")) {
			if (!cs.hasPermission("economy.set") && !cs.hasPermission("economy.*")) {
				cs.sendMessage(Message.NO_PERMISSIONS.toString());
				return true;
			}
			economyCommand = EconomyCommand.SET;
		} else if (args[0].equalsIgnoreCase("reset")) {
			if (!cs.hasPermission("economy.reset") && !cs.hasPermission("economy.*")) {
				cs.sendMessage(Message.NO_PERMISSIONS.toString());
				return true;
			}
			economyCommand = EconomyCommand.RESET;
		} else {
			cs.sendMessage(Message.WRONG_USAGE_ECONOMY.toString());
			return true;
		}

		if (economyCommand == EconomyCommand.RESET) {
			Player target = Bukkit.getPlayer(args[1]);

			if (target != null) {
				User targetUser = this.economy.getUser(target.getUniqueId(), target.getName());
				targetUser.set(economy.getStartingMoney());
				
				target.sendMessage(
						Message.MONEY_SET.toString().replace("{MONEY}", displayCurrency(economy.getStartingMoney())));
				cs.sendMessage(Message.MONEY_SET_SENDER.toString()
						.replace("{MONEY}", displayCurrency(economy.getStartingMoney()))
						.replace("{PLAYER}", target.getName()));
				return true;
			}

			setMoney(EconomyCMD.EconomyCommand.RESET, args[1], economy.getStartingMoney(),
					new CallBackSingle<Succes>() {

						@Override
						public void onQueryDone(Succes succes) {
							if (succes == Succes.DONE) {
								cs.sendMessage(Message.MONEY_SET_SENDER.toString()
										.replace("{MONEY}", displayCurrency(economy.getStartingMoney()))
										.replace("{PLAYER}", args[1]));
								return;
							} else if (succes == Succes.UUID_MISSING) {
								cs.sendMessage(Message.NO_UUID.toString());
								return;
							} else {
								cs.sendMessage(Message.ERROR.toString());
								return;
							}
						}

					});
			return true;
		}

		if (args.length != 3) {
			cs.sendMessage(Message.WRONG_USAGE_ECONOMY.toString());
			return true;
		}

		Player target = Bukkit.getPlayer(args[1]);

		BigDecimal amount = null;
		try {
			amount = new BigDecimal(args[2].replaceAll("[^0-9\\.]", ""));
		} catch (Exception exception) {
			cs.sendMessage(Message.WRONG_USAGE_ECONOMY.toString());
			return true;
		}

		final BigDecimal newAmount = amount;

		if (target == null) {

			setMoney(economyCommand, args[1], newAmount, new CallBackSingle<Succes>() {

				@Override
				public void onQueryDone(Succes succes) {
					if (succes == Succes.DONE) {
						switch (economyCommand) {
						case GIVE:
							cs.sendMessage(Message.MONEY_GIVE_SENDER.toString().replace("{PLAYER}", args[1])
									.replace("{MONEY}", displayCurrency(newAmount)));
							return;
						case SET:
							cs.sendMessage(Message.MONEY_SET_SENDER.toString().replace("{PLAYER}", args[1])
									.replace("{MONEY}", displayCurrency(newAmount)));
							return;
						case TAKE:
							cs.sendMessage(Message.MONEY_TAKE_SENDER.toString().replace("{PLAYER}", args[1])
									.replace("{MONEY}", displayCurrency(newAmount)));
							return;
						default:
							cs.sendMessage(Message.ERROR.toString());
							return;
						}
					} else if (succes == Succes.UUID_MISSING) {
						cs.sendMessage(Message.NO_UUID.toString());
						return;
					} else {
						cs.sendMessage(Message.ERROR.toString());
						return;
					}
				}
			});
			return true;
		} else {
			User targetUser = this.economy.getUser(target.getUniqueId(), target.getName());

			switch (economyCommand) {
			case GIVE:
				targetUser.add(newAmount);

				target.sendMessage(Message.MONEY_GIVE.toString().replace("{MONEY}", displayCurrency(newAmount)));
				cs.sendMessage(Message.MONEY_GIVE_SENDER.toString().replace("{PLAYER}", args[1]).replace("{MONEY}",
						displayCurrency(newAmount)));
				return true;
			case SET:
				targetUser.set(newAmount);

				target.sendMessage(Message.MONEY_SET.toString().replace("{MONEY}", displayCurrency(newAmount)));
				cs.sendMessage(Message.MONEY_SET_SENDER.toString().replace("{PLAYER}", args[1]).replace("{MONEY}",
						displayCurrency(newAmount)));
				return true;
			case TAKE:
				targetUser.remove(newAmount);
				target.sendMessage(Message.MONEY_TAKE.toString().replace("{MONEY}", displayCurrency(newAmount)));
				cs.sendMessage(Message.MONEY_TAKE_SENDER.toString().replace("{PLAYER}", args[1]).replace("{MONEY}",
						displayCurrency(newAmount)));
				return true;
			default:
				break;
			}
		}

		cs.sendMessage(Message.WRONG_USAGE_ECONOMY.toString());
		return true;

	}

	private void setMoney(EconomyCommand command, String name, BigDecimal to, CallBackSingle<Succes> callBack) {
		new BukkitRunnable() {

			@Override
			public void run() {

				Connection connection = null;

				try {
					connection = economy.getData().getHikari().getConnection();

					BigDecimal currentMoney = getMoney(connection);
					UUID uuid = getUUID(connection);

					if (uuid == null) {
						callBack.onQueryDone(Succes.UUID_MISSING);
						connection.close();
						return;
					}

					switch (command) {
					case GIVE:
						currentMoney = currentMoney.add(to);
						break;
					case RESET:
						currentMoney = economy.getStartingMoney();
						;
						break;
					case SET:
						currentMoney = to;
						break;
					case TAKE:
						Boolean canSubtract = currentMoney.subtract(to).compareTo(economy.getStartingMoney()) > 0;

						if (canSubtract) {
							currentMoney = currentMoney.subtract(to);
						} else {
							currentMoney = economy.getStartingMoney();
							;
						}
						break;
					}

					economy.getData().save(uuid, name, currentMoney, false);

					callBack.onQueryDone(Succes.DONE);
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
						callBack.onQueryDone(Succes.UNKNOWN);
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

				return currentMoney == null ? economy.getStartingMoney() : currentMoney;
			}

		}.runTaskAsynchronously(economy);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> toReturn = Lists.newArrayList();
		if (args.length == 1) {
			List<String> possibleReturns = Arrays.asList("set", "take", "give", "reset");
			for (String returns : possibleReturns) {
				if (returns.startsWith(args[0])) {
					toReturn.add(returns);
				}
			}

			if (toReturn.isEmpty()) {
				return possibleReturns;
			}
		}
		return toReturn;
	}

	private enum EconomyCommand {
		SET, RESET, GIVE, TAKE;
	}

	private enum Succes {
		DONE, UUID_MISSING, UNKNOWN;
	}
}