package com.opower.util.powerpool;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPoolExample {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
	 
		SimpleConnectionPool pool = SimpleConnectionPool.createDefaultPool("org.hsqldb.jdbc.JDBCDriver",
				"jdbc:hsqldb:mem:power-test", "sa", "");
		
		Connection connection = pool.getConnection();
		
		// do some stuff with the connection...
		
		pool.releaseConnection(connection);	// or, you can call connection.close();
		
		pool.stop();
		
		

	}
	
}
