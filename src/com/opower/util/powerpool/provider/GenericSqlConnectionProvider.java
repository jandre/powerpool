package com.opower.util.powerpool.provider;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 * TODO: it uses  a driver manager to get a connection.  
 * 
 * @author jennifer_andre
 *
 */
public class GenericSqlConnectionProvider implements NewConnectionProvider {

	 
	final String databaseUrl;
	final String user;
	final String password;
	
	public GenericSqlConnectionProvider(String driverName, String databaseUrl, String user, String password) throws ClassNotFoundException {
 
		// a driver name isn't strictly required.  the driver can be registered outside of this library.
		if (driverName != null && !driverName.isEmpty())
				Class.forName(driverName); 
		
		this.databaseUrl = databaseUrl;
		this.user = user;
		this.password = password;
        
	}
	@Override
	public Connection newConnection() throws SQLException {

        return DriverManager.getConnection(databaseUrl,
              user, password);

	}
	
 
	
	
}
