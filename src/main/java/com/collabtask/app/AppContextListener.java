package com.collabtask.app;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/collabtask?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
		String user = System.getenv().getOrDefault("DB_USER", "root");
		String pass = System.getenv().getOrDefault("DB_PASS", "root");
		DataSourceProvider.init(url, user, pass);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) { }
}
