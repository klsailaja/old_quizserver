package com.ab.tool;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
	
	// JDBC Driver Name & Database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String JDBC_DB_URL = "jdbc:mysql://localhost:3306/SERVER_QUIZ_DB?useUnicode=yes&characterEncoding=UTF-8";
    
    // JDBC Database Credentials
    static final String JDBC_USER = "rajasekhar";
    static final String JDBC_PASS = "rajasekhar";
    
    private static ConnectionPool instance;
    private MyConnectionPool myconnectionPool;
    
    public static ConnectionPool getInstance() throws SQLException {
    	if (instance == null) {
    		instance = new ConnectionPool();
    		instance.myconnectionPool = new MyConnectionPool(JDBC_DB_URL, JDBC_USER, JDBC_PASS);
    		instance.myconnectionPool.setCheckConnections(true);
    		instance.myconnectionPool.setMaxUseTime(2 * 60 * 1000);
    	}
    	return instance;
    }
    
    public Connection getDBConnection() throws SQLException {
    	return myconnectionPool.getConnection();
    }
    
    public Connection getConnectionNotFromPool() throws SQLException {
    	return myconnectionPool.getConnectionNotFromPool();
    }
}

