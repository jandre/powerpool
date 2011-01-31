package com.opower.util.powerpool;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opower.util.powerpool.provider.NewConnectionProvider;

public class ConnectionManagerTest {
	
	Mockery context; 
	Connection dummyConnection; 
	NewConnectionProvider dummyConnectionProvider;
	Settings dummySettings;  

	@Before
	public void setUp() throws Exception {
		 context = new Mockery();
		 dummyConnection = context.mock(Connection.class);
		 dummyConnectionProvider = context.mock(NewConnectionProvider.class);
		 dummySettings = context.mock(Settings.class);
	}

	@After
	public void tearDown() throws Exception {
		context.assertIsSatisfied();
	}
  
	@Test
	public final void testRequestConnectionThrowsExceptionIfNoneCanBeMade() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			oneOf(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider));  
			oneOf(dummyConnectionProvider).newConnection(); will(throwException(new SQLException("Failed to create a connection")));
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(1)); 
		}});	 
 		String message ="";
		try {
	 		Connection connection = manager.requestConnection();
		} catch (SQLException e) {
			// a bit redundant..
			message = e.getMessage();
			
		}
		Assert.assertEquals("Failed to create a connection",message);

 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(0, manager.idleConnections.size());
 		
	}
	@Test
	public final void testRequestConnectionWhenEmptyAddsOneConnectionToQueue() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			oneOf(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider)); 
			oneOf(dummyConnectionProvider).newConnection(); will(returnValue(dummyConnection));
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(1)); 
		}});	
 		Connection connection = manager.requestConnection();
 		assertNotNull(connection);
 		assertEquals(1, manager.usedConnections.size());
 	}
	
	@Test
	public final void testRequestConnectionReturnsIdleConnectionFirst() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			atLeast(1).of(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider)); 
			oneOf(dummyConnectionProvider).newConnection(); will(returnValue(dummyConnection));
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(1)); 
			atLeast(1).of(dummySettings).getValidConnectionTester(); will(returnValue(new ConnectionTester(){

				@Override
				public boolean isConnectionValid(Connection c) {
					
					return true;
				} })); 
		
		}});	
 		Connection connection = manager.requestConnection();
 		assertNotNull(connection);
 		assertEquals(1, manager.usedConnections.size());
 
 		// now, requeue it.
 		manager.free(connection);
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(1, manager.idleConnections.size());
 		
 		// now, request it again.  it should return it from the queue.
 		Connection newConnection = manager.requestConnection();
 		assertNotNull(newConnection);
  		assertEquals(0, manager.idleConnections.size());
  		assertEquals(1, manager.usedConnections.size());
 		
 		assertEquals(newConnection, connection);
 
 		
 	}
 	
	@Test
	public final void testRequestConnectionWhenFullThrowsException() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			atLeast(1).of(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider)); 
			oneOf(dummyConnectionProvider).newConnection(); will(returnValue(dummyConnection));
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(1)); 
		}});	
 		Connection connection = manager.requestConnection();
 		assertNotNull(connection);
 		assertEquals(1, manager.usedConnections.size());
 		
 		// now, add a new connection.
 	 
 		SQLException exception = null;
		try {
			Connection secondConnection = manager.requestConnection();
			 
		} catch (SQLException e) {
			
			exception = e;
		}
		Assert.assertNotNull(exception);
		Assert.assertEquals("Pool usage has exceeded the maximum number of configured connections=1", exception.getMessage());
	 
 		
		context.assertIsSatisfied();
 		
 		
 	}

	@Test
	public final void testFreeConnectionFreesConnection() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			atLeast(1).of(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider)); 
			oneOf(dummyConnectionProvider).newConnection(); will(returnValue(dummyConnection));
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(1));
			atLeast(1).of(dummySettings).getValidConnectionTester(); will(returnValue(new ConnectionTester(){

				@Override
				public boolean isConnectionValid(Connection c) {
					
					return true;
				} })); 
		}});	
 		Connection connection = manager.requestConnection();
 		assertNotNull(connection);
 		assertEquals(1, manager.usedConnections.size());
  		assertEquals(0, manager.idleConnections.size());
 		
 		// now, requeue it.
 		manager.free(connection);
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(1, manager.idleConnections.size());
	}
 
	@Test
	public final void testFreeBadConnectionWillNotAddToIdlePool() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			atLeast(1).of(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider)); 
			oneOf(dummyConnectionProvider).newConnection(); will(returnValue(dummyConnection));
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(1));
			atLeast(1).of(dummyConnection).close();
			atLeast(1).of(dummySettings).getValidConnectionTester(); will(returnValue(new ConnectionTester(){

				@Override
				public boolean isConnectionValid(Connection c) {
					
					return false;
				} })); 
		}});	
 		Connection connection = manager.requestConnection();
 		assertNotNull(connection);
 		assertEquals(1, manager.usedConnections.size());
  		assertEquals(0, manager.idleConnections.size());
 		
 		// now, requeue it. since it's bad it will remove it from the used connections but not add it to idle.
 		manager.free(connection);
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(0, manager.idleConnections.size());
	}
	
	@Test
	public final void testDoubleFreeDoesNotFail() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			atLeast(1).of(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider)); 
			oneOf(dummyConnectionProvider).newConnection(); will(returnValue(dummyConnection));
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(1));
 			atLeast(1).of(dummySettings).getValidConnectionTester(); will(returnValue(new ConnectionTester(){

				@Override
				public boolean isConnectionValid(Connection c) {
				 
					return true;
				} })); 
		}});	
 		Connection connection = manager.requestConnection();
 		assertNotNull(connection);
 		assertEquals(1, manager.usedConnections.size());
  		assertEquals(0, manager.idleConnections.size());
  		
		// now, requeue it.
 		manager.free(connection);
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(1, manager.idleConnections.size());

 		// nothing should change here.
 		manager.free(connection);
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(1, manager.idleConnections.size());
	}

	@Test
	public final void testExpireIdleConnectionsRemovesConnectionsAfterOneSecondTimeout()  throws Exception
	{
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			atLeast(1).of(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider)); 
			oneOf(dummyConnectionProvider).newConnection(); will(returnValue(dummyConnection));
			oneOf(dummyConnection).close();
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(1));
			atLeast(1).of(dummySettings).getMaximumIdleTimeSeconds(); will(returnValue(1));
			atLeast(1).of(dummySettings).getValidConnectionTester(); will(returnValue(new ConnectionTester(){

				@Override
				public boolean isConnectionValid(Connection c) {
					
					return true;
				} })); 
		}});	
 		Connection connection = manager.requestConnection();
 		assertNotNull(connection);
 		assertEquals(1, manager.usedConnections.size());
  		assertEquals(0, manager.idleConnections.size());
  		
		// now, requeue it.
 		manager.free(connection);
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(1, manager.idleConnections.size());
 		
 		// sleep 2 seconds.
 		Thread.currentThread().sleep(2 * 1000);
 		
 		manager.expireIdleConnections();
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(0, manager.idleConnections.size());
 		
	}

	@Test
	public final void testCloseSetsStopped() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		manager.close();
 		Assert.assertTrue(manager.stopped);
	}
	
 	@Test
	public final void testCloseFreesIdleConnections() throws SQLException {
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
		
		context.checking(new Expectations() {{ 
 			oneOf(dummySettings).getNewConnectionProvider(); will(returnValue(dummyConnectionProvider)); 
			oneOf(dummyConnectionProvider).newConnection(); will(returnValue(dummyConnection));
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(100));
			oneOf(dummySettings).getValidConnectionTester(); will(returnValue(new ConnectionTester(){

				@Override
				public boolean isConnectionValid(Connection c) {
					 	return true;
				} })); 
		 
			
			oneOf(dummyConnection).close(); 
		 		}});	

 		Connection connection = manager.requestConnection();
 		assertNotNull(connection);
 		assertEquals(1, manager.usedConnections.size());
 
 		// now, requeue it.
 		manager.free(connection);
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(1, manager.idleConnections.size());
		
 		manager.close();
 		Assert.assertTrue(manager.stopped);

 		assertEquals(0, manager.idleConnections.size());
	}
	

	private  Connection createMockConnection(String name) {
		return context.mock(Connection.class,name);
		
	}
	
	@Test
	public final void testExpireIdleConnectionsKeepsNonIdleConnectionsAfterFiveSecondTimeout()  throws Exception
	{ 
		final String connection1 = "firstconnection", connection2 = "secondconnection"; 
		
		final NewConnectionProvider mockProvider = new NewConnectionProvider() {
			int i = 0;
			@Override
			public Connection newConnection() throws SQLException {
				String key =  (i++ == 0) ?  connection1 :  connection2;
				Connection c = createMockConnection(key);
				 
				return c;
			}
		};
	 			
		ConnectionManagerImpl manager = new ConnectionManagerImpl(dummySettings);
 		context.checking(new Expectations() {{ 
 			atLeast(1).of(dummySettings).getNewConnectionProvider(); will(returnValue(mockProvider)); 
 				 
			atLeast(1).of(dummySettings).getMaximumConnections(); will(returnValue(2));
			atLeast(1).of(dummySettings).getValidConnectionTester(); will(returnValue(new ConnectionTester(){

				@Override
				public boolean isConnectionValid(Connection c) {
					
					return true;
				} })); 
		}});	
 		final Connection connection = manager.requestConnection();
 		final Connection newConnection = manager.requestConnection();
 		assertNotNull(connection);
 		assertNotNull(newConnection);
 		assertNotSame(connection,newConnection);
 		assertEquals(2, manager.usedConnections.size());
  		assertEquals(0, manager.idleConnections.size());
  		
		// now, requeue the first.
  			
  		
 		manager.free(connection);
 		assertEquals(1, manager.usedConnections.size());
 		assertEquals(1, manager.idleConnections.size());
 		
 		// sleep 2 seconds.
 		Thread.currentThread().sleep(2 * 1000);
 		// add a new connection and free it.
 		assertNotNull(newConnection);
 		manager.free(newConnection);
 		assertEquals(0, manager.usedConnections.size());
  		assertEquals(2, manager.idleConnections.size());
  		
  		context.assertIsSatisfied();

  		context.checking(new Expectations() {{ 
 			atLeast(1).of(dummySettings).getMaximumIdleTimeSeconds(); will(returnValue(1));
 			atLeast(1).of(connection).close(); 
 			never(newConnection).close();
 		}});
 		manager.expireIdleConnections();
 		assertEquals(0, manager.usedConnections.size());
 		assertEquals(1, manager.idleConnections.size());
 		
	}

}
