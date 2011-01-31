package com.opower.util.powerpool;

import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionManager {

 
	 void free(Connection connection) throws SQLException;

	 Connection requestConnection() throws SQLException;

	 void expireIdleConnections();
	 
	 void close();
	
	 

}