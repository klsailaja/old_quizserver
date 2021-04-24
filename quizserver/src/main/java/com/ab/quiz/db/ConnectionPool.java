package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionPool {
	// JDBC Driver Name & Database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String JDBC_DB_URL = "jdbc:mysql://localhost:3306/telugu_movies_db?useUnicode=yes&characterEncoding=UTF-8";
    
    
    // JDBC Database Credentials
    static final String JDBC_USER = "rajasekhar";
    static final String JDBC_PASS = "rajasekhar";
    
    private BasicDataSource dataSource;
    
    private static ConnectionPool instance;
    
    public static ConnectionPool getInstance() {
    	if (instance == null) {
    		instance = new ConnectionPool();
    		instance.setUp();
    	}
    	return instance;
    }
    
    private void setUp() {
    	if (dataSource == null) {
    		BasicDataSource ds = new BasicDataSource();
    		
    		//ds.setDriverClassName(JDBC_DRIVER);
            ds.setUrl(JDBC_DB_URL);
            ds.setUsername(JDBC_USER);
            ds.setPassword(JDBC_PASS);
 
            ds.setInitialSize(8);
            ds.setMaxOpenPreparedStatements(50);
            ds.setDefaultAutoCommit(true);
            ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            dataSource = ds;
    	}
    }
    
    public Connection getDBConnection() throws SQLException {
    	return dataSource.getConnection();
    }
    
    public void shutDown() throws SQLException {
    	dataSource.close();
    }
}
