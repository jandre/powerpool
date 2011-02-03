package com.opower.util.powerpool;


import java.sql.Connection;
import java.sql.SQLException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opower.util.powerpool.ConnectionPool;
import com.opower.util.powerpool.SimpleConnectionPool;
import com.opower.util.powerpool.connection.ManualProxiedConnection;
import com.opower.util.powerpool.connection.ProxiedConnection;
import com.opower.util.powerpool.connection.ProxiedConnectionProvider;
import com.opower.util.powerpool.monitor.Monitor;
import com.opower.util.powerpool.provider.NewConnectionProvider;

public class SimpleConnectionPoolTest {

	Mockery context; 
	Connection dummyConnection; 
	NewConnectionProvider dummyConnectionProvider;
	Settings dummySettings;
	ConnectionManager dummyManager;
	ProxiedConnectionProvider dummyWrapper;
	ProxiedConnection dummyWrappedConnection;
	
	@Before
	public void setUp() throws Exception {
		 context = new Mockery();
		 dummyConnection = context.mock(Connection.class);
		 dummyConnectionProvider = context.mock(NewConnectionProvider.class);
		 dummyManager = context.mock(ConnectionManager.class);
		 dummyWrapper = context.mock(ProxiedConnectionProvider.class);
		 dummyWrappedConnection = context.mock(ProxiedConnection.class);
	}

	@After
	public void tearDown() throws Exception {

		context.assertIsSatisfied();
	}
 	  
	
	@Test
	public void testCreateNewConnectionReturnsAConnection() throws SQLException {
		final SimpleConnectionPool pool = new SimpleConnectionPool( dummyManager, dummyWrapper, Monitor.DUMMY_MONITOR);
	
		context.checking(new Expectations() {{ 
			oneOf(dummyManager).requestConnection(); will(returnValue(dummyConnection));
			oneOf(dummyWrapper).newProxiedConnection(dummyConnection, pool); will(returnValue(dummyWrappedConnection));
		 	 
		}});	
		
		Connection pooledConnection = pool.getConnection();
 		Assert.assertNotNull(pooledConnection); 
		
		context.assertIsSatisfied();
	}
	
	@Test 
	public void testConnectionReturnedIsAProxiedConnection() throws SQLException {
		
		final SimpleConnectionPool pool = new SimpleConnectionPool( dummyManager, dummyWrapper, Monitor.DUMMY_MONITOR);
		
		context.checking(new Expectations() {{ 
			oneOf(dummyManager).requestConnection(); will(returnValue(dummyConnection));
			oneOf(dummyWrapper).newProxiedConnection(dummyConnection, pool); will(returnValue(dummyWrappedConnection));
			oneOf(dummyWrappedConnection).getUnderlyingConnection(); will(returnValue(dummyConnection));
			
			 
		}});	
		
		Connection pooledConnection = pool.getConnection();
 		Assert.assertNotNull(pooledConnection);
		Assert.assertTrue(pooledConnection instanceof ProxiedConnection);
		
		ProxiedConnection value = (ProxiedConnection) pooledConnection;
		Assert.assertEquals(dummyConnection, value.getUnderlyingConnection());
		
		context.assertIsSatisfied();
	}
 
	@Test
	public void testExceptionIsThrownWhenNoConnectionCanBeCreated() throws SQLException {
final SimpleConnectionPool pool = new SimpleConnectionPool( dummyManager, dummyWrapper, Monitor.DUMMY_MONITOR);
		
		context.checking(new Expectations() {{ 
			oneOf(dummyManager).requestConnection(); will(throwException(new SQLException("Failed to create a connection")));
		 	
			 
		}});
		String message ="";
		try {
		Connection pooledConnection = pool.getConnection(); 
		} catch (SQLException e) {
			// a bit redundant..
			message = e.getMessage();
			
		}
		Assert.assertEquals("Failed to create a connection",message);
		
	}
	  
	@Test
	public void testReturningAConnectionCallsManagerToFreeConnection() throws SQLException {
		final SimpleConnectionPool pool = new SimpleConnectionPool( dummyManager, dummyWrapper, Monitor.DUMMY_MONITOR);
	
		context.checking(new Expectations() {{ 
			oneOf(dummyManager).requestConnection(); will(returnValue(dummyConnection));
			oneOf(dummyWrapper).newProxiedConnection(dummyConnection, pool); will(returnValue(dummyWrappedConnection));
			oneOf(dummyWrappedConnection).getUnderlyingConnection(); will(returnValue(dummyConnection));
	 		 
		}});	
		
		Connection pooledConnection = pool.getConnection();
 		Assert.assertNotNull(pooledConnection);
		Assert.assertTrue(pooledConnection instanceof ProxiedConnection);
		
		final ProxiedConnection value = (ProxiedConnection) pooledConnection;
		Assert.assertEquals(dummyConnection, value.getUnderlyingConnection());
		
		context.assertIsSatisfied();
		
		context.checking(new Expectations() {{ 
			oneOf(dummyWrappedConnection).getUnderlyingConnection(); will(returnValue(dummyConnection));
 			oneOf(dummyWrappedConnection).clearConnection();
			oneOf(dummyManager).free(dummyConnection);
		}});
		pool.releaseConnection(pooledConnection);
		
		
	}

	  
	@Test
	public void testFreeingAConnectionNotAssignedByPoolThrowsException() throws SQLException {
		final SimpleConnectionPool pool = new SimpleConnectionPool( dummyManager, dummyWrapper, Monitor.DUMMY_MONITOR);
	
		 
		context.checking(new Expectations() {{ 
			never(dummyWrappedConnection).getUnderlyingConnection(); 
 			never(dummyWrappedConnection).clearConnection();
			never(dummyManager).free(dummyConnection);
		}});
		
		String message = "";
		try { 
		pool.releaseConnection(dummyConnection);
		} catch (SQLException e) {
			message = e.getMessage();
		}
		Assert.assertEquals("You are trying to return a connection to the connection pool that was not allocated by the pool. You silly duck!", message);
		
 
	}
	

	@Test
	public void testCreateNewConnectionReturnsAConnectionWithRealProxyProvider_ManualProxy() throws Exception {
		testCreateNewConnectionReturnsAConnectionWithRealProxyProvider(new ProxiedConnectionProvider.ManualProxyProvider());
  	} 
	
	@Test
	public void testCreateNewConnectionReturnsAConnectionWithRealProxyProvider_AutoProxy() throws Exception {
		testCreateNewConnectionReturnsAConnectionWithRealProxyProvider(new ProxiedConnectionProvider.AutoProxyProvider());
	}  
	
	
	// helper method.
	public void testCreateNewConnectionReturnsAConnectionWithRealProxyProvider(ProxiedConnectionProvider wrapperProvider) throws SQLException {
		SimpleConnectionPool pool = new SimpleConnectionPool( dummyManager , wrapperProvider, Monitor.DUMMY_MONITOR);
	
		context.checking(new Expectations() {{ 
			oneOf(dummyManager).requestConnection(); will(returnValue(dummyConnection));
		}});	
		
		Connection pooledConnection = pool.getConnection();
		Assert.assertNotNull(pooledConnection);
		Assert.assertTrue(pooledConnection instanceof ProxiedConnection);
		
		ProxiedConnection value = (ProxiedConnection) pooledConnection;
		Assert.assertEquals(dummyConnection, value.getUnderlyingConnection());
		
		context.assertIsSatisfied();
	}

	
	@Test
	public void testMonitorGetsStarted() {
		final Monitor dummyMonitor = context.mock(Monitor.class);
	
		context.checking(new Expectations() {{ 
			oneOf(dummyMonitor).start();
		}});	
		SimpleConnectionPool pool = new SimpleConnectionPool( dummyManager , dummyWrapper, dummyMonitor );
		
		context.assertIsSatisfied();
	}

	@Test
	public void testMonitorGetsStopped() {
		final Monitor dummyMonitor = context.mock(Monitor.class);
	
		context.checking(new Expectations() {{ 
			oneOf(dummyMonitor).start();
		}});	
		SimpleConnectionPool pool = new SimpleConnectionPool( dummyManager , dummyWrapper, dummyMonitor );
		
		context.assertIsSatisfied();
		context.checking(new Expectations() {{ 
			oneOf(dummyMonitor).stop();
			oneOf(dummyManager).close();
		}});	 
		pool.stop();
	}



}
