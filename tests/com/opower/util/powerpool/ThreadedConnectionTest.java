package com.opower.util.powerpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import junit.framework.Assert;

import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.util.HsqlDatabaseUtil;

import com.opower.util.powerpool.ConnectionManager;
import com.opower.util.powerpool.ConnectionManagerImpl;
import com.opower.util.powerpool.ConnectionPool;
import com.opower.util.powerpool.Settings;
import com.opower.util.powerpool.SettingsImpl;
import com.opower.util.powerpool.SimpleConnectionPool;
import com.opower.util.powerpool.connection.ProxiedConnection;
import com.opower.util.powerpool.connection.ProxiedConnectionProvider;
import com.opower.util.powerpool.di.ConfigInjector;
import com.opower.util.powerpool.monitor.Monitor;
import com.opower.util.powerpool.provider.NewConnectionProvider;

public class ThreadedConnectionTest {
	
	ConnectionManagerImpl manager;
	SimpleConnectionPool connectionPool;
	Monitor monitor = Monitor.DUMMY_MONITOR;
	
	@Before
	public void setUp() throws Exception {
		SettingsImpl settings = new SettingsImpl(HsqlDatabaseUtil.driver, 
				HsqlDatabaseUtil.databaseUrl, HsqlDatabaseUtil.user, HsqlDatabaseUtil.password);
		
		manager = new ConnectionManagerImpl(settings);
		
		connectionPool = new SimpleConnectionPool(manager, new ProxiedConnectionProvider.AutoProxyProvider(), Monitor.DUMMY_MONITOR);
		
	}

	@After
	public void tearDown() throws Exception { 
	}
 
	
	synchronized void add(HashMap<Connection, Connection> connections, Connection proxiedConnection) {
		 
		connections.put(proxiedConnection, proxiedConnection);
		
	}
	
	@Test
	public void testFiveThreadedConnectionsGetAddedAndRepooled() throws Exception {
		
		
		final SimpleConnectionPool pool = this.connectionPool;
		
		 final HashMap<Connection, Connection> connections = new HashMap<Connection,Connection>();
	 	
		Thread[] threads = new Thread[5];
		for (int i = 0; i < threads.length ; i++) {
			
			Thread t = new Thread(new Runnable() {

				@Override
				public void run()  {

						try {
							Connection con = pool.getConnection();
						  
							HsqlDatabaseUtil.selectARow(con);  
							add(connections, con); 
						 
						} catch (Exception e) {}
				}} );
			
			t.start(); 
			threads[i] = t;
		}
		for (int i = 0; i < threads.length ; i++) {
			threads[i].join(1000 * 10);
			
		} 
		 
		
		Assert.assertEquals(threads.length, connections.size());
		Assert.assertEquals(manager.usedConnections.size(), threads.length);

		Assert.assertEquals(manager.idleConnections.size(), 0);
		
		// now, free all connections
		for (Connection c : connections.keySet()) {
			 c.close();
		}
		
		Assert.assertEquals(manager.usedConnections.size(), 0) ;

		Assert.assertEquals(manager.idleConnections.size(), threads.length);
		
		for (int i = 0; i < threads.length; i++) {
			// get a connection again.

			Connection con = pool.getConnection();		  
			HsqlDatabaseUtil.selectARow(con); 
		}
		

		Assert.assertEquals(manager.usedConnections.size(), threads.length);
 		Assert.assertEquals(manager.idleConnections.size(), 0);
		
		
		
	}
	 
 	
 
	
}
