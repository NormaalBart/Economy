package me.bartvv.economy.sql;

import com.zaxxer.hikari.HikariDataSource;

import me.bartvv.economy.Economy;

public abstract class MySQL {

	private Economy economy;
	private HikariDataSource hikari;

	public MySQL(Economy rebelNetwork) {
		this.economy = rebelNetwork;
	}

	public void close() {
		if (hikari != null) {
			this.hikari.close();
		}
	}

	public void createConnection(String host, Integer port, String database, String user, String password) {
		this.hikari = new HikariDataSource();
		this.hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		this.hikari.addDataSourceProperty("serverName", host);
		this.hikari.addDataSourceProperty("port", port);
		this.hikari.addDataSourceProperty("databaseName", database);
		this.hikari.addDataSourceProperty("user", user);
		this.hikari.addDataSourceProperty("password", password);
	}

	public HikariDataSource getHikari() {
		return this.hikari;
	}

	protected abstract void createTable(String tableName);
}