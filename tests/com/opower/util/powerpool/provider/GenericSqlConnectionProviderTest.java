package com.opower.util.powerpool.provider;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import test.util.HsqlDatabaseUtil;
 
/**
 * These test cases use HSQLDB to make sure a connection is properly made.
 * 
 * @author jennifer_andre
 *
 */
public class GenericSqlConnectionProviderTest {

	String databaseUrl = "jdbc:hsqldb:mem:power-test";
	String driver = "org.hsqldb.jdbc.JDBCDriver";
	String user = "sa";
	String password = "";
	
	@After
	public void tearDown() throws Exception {
		HsqlDatabaseUtil.dropAllTestSchemaObjects();
	} 
	
	@Test
	public void testValidHsqlConnectionIsReturned() throws SQLException, ClassNotFoundException {
		 
		GenericSqlConnectionProvider provider = new GenericSqlConnectionProvider(driver, databaseUrl, user, password);
		
		Connection connection = provider.newConnection();
		
		Assert.assertNotNull(connection);
		
		// now, for kicks, do something with the connection.
		try {
			Statement stmt = connection.createStatement();
	        
	        stmt.addBatch( "CREATE MEMORY TABLE power_tester(id INTEGER)");
	        stmt.addBatch("INSERT INTO power_tester (id) VALUES (100)");
	        
	        stmt.executeBatch();
	        
	        ResultSet set = stmt.executeQuery("SELECT id FROM power_tester");
	        int id = 0; 
	        while (set.next()) {
	        	id = set.getInt(1); 
	        }
	        
	        Assert.assertEquals(100, id);
	        
	        
		} finally {
			
			connection.close();
		
		}
		
		
	}
	 

 

}
