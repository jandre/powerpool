package com.opower.util.powerpool;

public interface ConnectionPool {
	
	java.sql.Connection getConnection() throws java.sql.SQLException;
	
	void releaseConnection(java.sql.Connection con) throws java.sql.SQLException;
	
}
