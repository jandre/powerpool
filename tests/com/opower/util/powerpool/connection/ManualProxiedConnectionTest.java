package com.opower.util.powerpool.connection;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test; 

import com.opower.util.powerpool.ConnectionPool;
import com.opower.util.powerpool.connection.ManualProxiedConnection;

public class ManualProxiedConnectionTest {

	Mockery context;
	ConnectionPool dummyPool;
	Connection dummyConnection; 
	
	@Before
	public void setUp() throws Exception {
	   context = new Mockery();
	   dummyConnection = context.mock(Connection.class);
	   dummyPool = context.mock(ConnectionPool.class);
	
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void testConnectionMethodCallsUnderlyingConnectionMethod() throws SQLException {
		final ManualProxiedConnection connection = new ManualProxiedConnection(dummyConnection, dummyPool);
		final Statement dummyStatement = context.mock(Statement.class);
		context.checking(new Expectations() {{oneOf(dummyConnection).createStatement(); will(returnValue(dummyStatement)); }});
		 
		Statement st = connection.createStatement();
	    assertNull(st);
	    assertEquals(dummyStatement, st);
		 
	    context.assertIsSatisfied();
	}
   
	@Test
	public void testCloseSetsANullConnectionAndCallsReleaseConnection() throws SQLException {
		final ManualProxiedConnection connection = new ManualProxiedConnection(dummyConnection, dummyPool);
		context.checking(new Expectations() {{oneOf(dummyPool).releaseConnection(connection); }});
		 
		connection.close();
	    assertNull(connection.getUnderlyingConnection());
		 
	    context.assertIsSatisfied();
	}

	@Test
	public void testDoubleCloseIsOkAndDoesntCallReleaseConnectionTwice() throws SQLException {
		final ManualProxiedConnection connection = new ManualProxiedConnection(dummyConnection, dummyPool); 
		  
		context.checking(new Expectations() {{oneOf(dummyPool).releaseConnection(connection); }});
		// 1st invocation of connection close.
		connection.close();
	    assertNull(connection.getUnderlyingConnection());

		context.assertIsSatisfied();
		// the second invocation should not have releaseConnection called again.	     
	    context.checking(new Expectations() {{never(dummyPool).releaseConnection(connection); }});

		connection.close();
		assertNull(connection.getUnderlyingConnection());
	     
		context.assertIsSatisfied();
			
	}
}
