package com.collabtask.app;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceProvider {
	private static HikariDataSource dataSource;

	public static void init(String jdbcUrl, String username, String password) {
		if (dataSource != null) return;
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword(password);
		config.setMaximumPoolSize(10);
		config.setMinimumIdle(2);
		config.setPoolName("CollabTaskPool");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		dataSource = new HikariDataSource(config);
	}

	public static DataSource get() {
		if (dataSource == null) {
			throw new IllegalStateException("DataSource not initialized");
		}
		return dataSource;
	}
}
