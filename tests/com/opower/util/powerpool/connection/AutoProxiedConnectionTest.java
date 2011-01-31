package com.opower.util.powerpool.connection;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opower.util.powerpool.ConnectionPool;
import com.opower.util.powerpool.connection.AutoProxiedConnection;
import com.opower.util.powerpool.connection.ProxiedConnection;

public class AutoProxiedConnectionTest {

	Mockery context;
	ConnectionPool dummyPool;
	Connection dummyConnection; 
	
	@Before
	public void setUp() throws Exception {
	   context = new Mockery();
	   dummyConnection =   context.mock(Connection.class);
	   dummyPool = context.mock(ConnectionPool.class);
	
	}
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testConnectionMethodCallsUnderlyingConnectionMethod() throws SQLException {
		final Connection connection =AutoProxiedConnection.wrapConnection(dummyConnection, dummyPool);
		final Statement dummyStatement = context.mock(Statement.class);
		context.checking(new Expectations() {{oneOf(dummyConnection).createStatement(); will(returnValue(dummyStatement)); }});
		 
		Statement st = connection.createStatement();
	    Assert.assertNotNull(st);
	    Assert.assertEquals(dummyStatement, st);
		 
	    context.assertIsSatisfied();
	}

	@Test
	public void testCloseSetsANullConnectionAndCallsReleaseConnection() throws SQLException {
	
 		final Connection connection = AutoProxiedConnection.wrapConnection(dummyConnection, dummyPool);
  
		Assert.assertTrue(connection instanceof ProxiedConnection);
		
		context.checking(new Expectations() {{oneOf(dummyPool).releaseConnection(connection); }});
		 
	 	connection.close();
	 	
	 	Assert.assertTrue(connection.isClosed());
	  
	    context.assertIsSatisfied();
	}
 
	
	@Test
	public void testDoubleCloseIsOkAndDoesntCallReleaseConnectionTwice() throws SQLException {

 		final Connection connection = AutoProxiedConnection.wrapConnection(dummyConnection, dummyPool);
    
		context.checking(new Expectations() {{oneOf(dummyPool).releaseConnection(connection); }});
		// 1st invocation of connection close.
		connection.close();
	 	Assert.assertTrue(connection.isClosed());
		// the second invocation should not have releaseConnection called again.	     
	    context.checking(new Expectations() {{never(dummyPool).releaseConnection(connection); }});

		connection.close();
	 	Assert.assertTrue(connection.isClosed());
	     
		context.assertIsSatisfied();
			
	}
}
