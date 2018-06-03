package me.bartvv.economy.commands;

import static me.bartvv.economy.Utils.displayCurrency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import me.bartvv.economy.Economy;
import me.bartvv.economy.interfaces.CallBackDouble;
import me.bartvv.economy.manager.User;
import me.bartvv.economy.message.Message;

public class BaltopCMD implements CommandExecutor {

	private Economy economy;

	public BaltopCMD(Economy economy) {
		this.economy = economy;
		economy.getCommand("baltop").setExecutor(this);

		updateBalTop();
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String cmdLabel, String[] args) {
		int page = 1;

		if (args.length > 0) {
			try {
				page = Integer.parseInt(args[0]);
			} catch (NumberFormatException nfe) {
				page = 1;
			}
		}
		int from = (page - 1 <= 0 ? 0 : page - 1) * 10;

		if (page < 1) {
			page = 1;
		}

		final int pageFinal = page;

		getBalTop(from, new CallBackDouble<Integer, List<String>>() {

			@Override
			public void onQueryDone(Integer numberOfRows, List<String> list) {
				if (list.isEmpty()) {
					cs.sendMessage(Message.PAGE_NOT_FOUND.toString());
					return;
				}

				cs.sendMessage(Message.INFO_PAGES.toString().replace("{CURRENTPAGE}", "" + pageFinal)
						.replace("{MAXPAGE}", numberOfRows == -1 ? "N/A" : "" + numberOfRows));

				for (String string : list) {
					cs.sendMessage(string);
				}

				cs.sendMessage(Message.READ_NEXT_PAGE.toString().replace("{COMMAND}", "baltop").replace("{PAGE}",
						"" + (pageFinal >= numberOfRows ? pageFinal : pageFinal + 1)));
				return;
			}
		});
		return true;
	}

	private void updateBalTop() {
		new BukkitRunnable() {
			public void run() {
				try {
					for (User user : economy.getUsers()) {
						user.save(false);
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(this.economy, 0, (5 * 20));
	}

	private void getBalTop(Integer from, CallBackDouble<Integer, List<String>> callBack) {

		new BukkitRunnable() {
			@Override
			public void run() {

				Connection connection = null;
				PreparedStatement statement;
				ResultSet result;

				try {
					connection = economy.getData().getHikari().getConnection();

					Integer pages = -1;
					String getRows = "SELECT COUNT(*) FROM " + economy.getData().getServerName() + " WHERE money != 0 AND isPlayer = true";

					statement = connection.prepareStatement(getRows);
					result = statement.executeQuery();

					if (result.next()) {
						pages = result.getInt(1);
					}

					if (!(pages < 10)) {

						if (pages % 10 == 0) {
							pages = pages / 10;
						} else {
							pages = pages / 10 + 1;
						}

					} else {
						pages = 1;
					}

					String query = "SELECT * FROM " + economy.getData().getServerName() + " WHERE money != 0 AND isPlayer = true ORDER BY money DESC LIMIT "
							+ from + "," + 10;

					List<String> output = Lists.newArrayList();

					result.close();
					statement.close();

					statement = connection.prepareStatement(query);
					result = statement.executeQuery();

					int number = from;
					number++;
					while (result.next()) {
						output.add(Message.BALTOP_FORMAT.toString().replace("{PLAYER}", result.getString("name"))
								.replace("{MONEY}", displayCurrency(result.getBigDecimal("money")))
								.replace("{NUMBER}", "" + number));
						number++;
					}

					result.close();
					statement.close();

					if (callBack != null)
						callBack.onQueryDone(pages, output);
				} catch (Exception sqlException) {
					sqlException.printStackTrace();
				} finally {
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			}
		}.runTaskAsynchronously(this.economy);
	}
}