package me.bartvv.economy.sql;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.bartvv.economy.Economy;
import me.bartvv.economy.Utils;
import me.bartvv.economy.manager.User;

public class DataSQL extends MySQL {

	private String serverName;
	private Economy economy;

	public DataSQL(Economy economy) {
		super(economy);

		this.economy = economy;
		this.economy.saveResource("mysql.yml", false);

		File file = new File(economy.getDataFolder(), "mysql.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		String host = config.getString("pluginData.host");
		Integer port = config.getInt("pluginData.port");
		String database = config.getString("pluginData.databaseName");
		String user = config.getString("pluginData.user");
		String password = config.getString("pluginData.password");

		super.createConnection(host, port, database, user, password);

		this.serverName = config.getString("pluginData.serverName");

		this.createTable(getServerName());
		tryToAddKolumn();
	}

	@Override
	protected void createTable(String tableName) {
		try {
			Connection connection = getHikari().getConnection();
			PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName
					+ "(UUID varchar(36), name VARCHAR(40), money DECIMAL(13,0), isPlayer BOOLEAN,CONSTRAINT pk_uuid PRIMARY KEY (UUID))");
			statement.execute();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void tryToAddKolumn() {
		try {
			Connection connection = getHikari().getConnection();
			try {
				PreparedStatement statement = connection.prepareStatement("ALTER TABLE " + getServerName()
						+ " ADD `isPlayer` BOOLEAN NOT NULL DEFAULT TRUE AFTER `money`;");
				statement.execute();
			} catch (Exception exc) {
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void save(UUID uuid, String name, BigDecimal money, Boolean async) {
		if (async) {
			new BukkitRunnable() {

				@Override
				public void run() {
					try {

						Connection connection = getHikari().getConnection();

						String INSERT_ECONOMY = "INSERT INTO " + getServerName()
								+ " VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=?, money=?, isPlayer=?";
						PreparedStatement statement = connection.prepareStatement(INSERT_ECONOMY);
						statement.setString(1, uuid.toString());
						statement.setString(2, name);
						statement.setBigDecimal(3, money);

						if (Utils.getUUIDFromString(name).toString().equalsIgnoreCase(uuid.toString())) {
							statement.setBoolean(4, false);
						} else {
							statement.setBoolean(4, true);
						}
						statement.setString(5, name);
						statement.setBigDecimal(6, money);
						if (Utils.getUUIDFromString(name).toString().equalsIgnoreCase(uuid.toString())) {
							statement.setBoolean(7, false);
						} else {
							statement.setBoolean(7, true);
						}
						statement.executeUpdate();
						statement.close();
						connection.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(economy);
		} else {
			try {
				Connection connection = getHikari().getConnection();

				String INSERT_ECONOMY = "INSERT INTO " + getServerName()
						+ " VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=?, money=?, isPlayer=?";
				PreparedStatement statement = connection.prepareStatement(INSERT_ECONOMY);
				statement.setString(1, uuid.toString());
				statement.setString(2, name);
				statement.setBigDecimal(3, money);
				if (Utils.getUUIDFromString(name).toString().equalsIgnoreCase(uuid.toString())) {
					statement.setBoolean(4, false);
				} else {
					statement.setBoolean(4, true);
				}
				statement.setString(5, name);
				statement.setBigDecimal(6, money);
				if (Utils.getUUIDFromString(name).toString().equalsIgnoreCase(uuid.toString())) {
					statement.setBoolean(7, false);
				} else {
					statement.setBoolean(7, true);
				}
				statement.executeUpdate();
				statement.close();

				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void load(User user) {
		String SELECT_ECONOMY = "SELECT money FROM " + getServerName() + " WHERE UUID = ?";
		new BukkitRunnable() {

			@Override
			public void run() {
				try {
					Connection connection = getHikari().getConnection();
					PreparedStatement statement = connection.prepareStatement(SELECT_ECONOMY);
					statement.setString(1, user.getUniqueId().toString());

					ResultSet resultSet = statement.executeQuery();

					BigDecimal decimal = resultSet.next() ? resultSet.getBigDecimal("money")
							: new BigDecimal(0);

					user.add(decimal);

					statement.close();
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(economy);
	}

	public String getServerName() {
		return this.serverName;
	}
}