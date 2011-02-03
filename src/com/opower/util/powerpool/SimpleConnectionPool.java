package com.opower.util.powerpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.inject.Inject;
import com.opower.util.powerpool.connection.ProxiedConnection;
import com.opower.util.powerpool.connection.ProxiedConnectionProvider;
import com.opower.util.powerpool.di.ConfigInjector;
import com.opower.util.powerpool.di.DefaultConfigModule;
import com.opower.util.powerpool.monitor.Monitor;
import com.opower.util.powerpool.monitor.PoolMonitor;
import com.opower.util.powerpool.provider.GenericSqlConnectionProvider;
import com.opower.util.powerpool.provider.NewConnectionProvider;


/**
 * 
 * A simple connection pool.
 * 
 * All connections returned are wrapped to identify they come from the pool.  Calling "close" on a connection
 * will return it to the pool.
 * 
 * @author jennifer_andre
 *
 */
public class SimpleConnectionPool implements ConnectionPool {
   
	final ConnectionManager manager;
	  
	final ProxiedConnectionProvider connectionWrapper;
	 
	final Monitor monitor;
	
	/**
	 * Creates a SimpleConnectionPool.
	 * 
	 * You may want to use the factory methods instead, e.g.:
	 * 	 SimpleConnectionPool.createDefaultPool(...)
	 * 	 SimpleConnectionPool.createPool(...)
	 * 
	 * @param manager
	 * @param connectionWrapper
	 * @param monitor
	 */
	public SimpleConnectionPool(ConnectionManager manager,  ProxiedConnectionProvider connectionWrapper, Monitor monitor) {
	 	this.manager = manager; 
		this.connectionWrapper = connectionWrapper;
		this.monitor = monitor;
		this.monitor.start();
	}
	
	void stop() {
		monitor.stop();
		synchronized(manager) {
			manager.close();
		}
	}
	
	protected void finalize() throws Throwable {
		try {
			stop();
		} finally {
			super.finalize();
		}
		
	}
	 
	@Override
	public Connection getConnection() throws SQLException { 
		
		// never keep a reference to a ProxiedConnection, otherwise it will never be
		// garbage collected.
		return connectionWrapper.newProxiedConnection(tryGetConnection(), this);
	
	}

	@Override
	public void releaseConnection(Connection con) throws SQLException {

		if (con == null) 
			return;

		// since we only ever hand out ProxiedConnections, we should only ever get them back.
		if (con instanceof ProxiedConnection)
		{
			try 
			{
				free((ProxiedConnection)con);
			}  
			finally {

				((ProxiedConnection)con).clearConnection();
			}

		}   
		else  // TODO: what happens if someone tries to return a connection to the pool that wasn't created by it?  throw exception? why not.
		{
			throw new SQLException("You are trying to return a connection to the connection pool that was not allocated by the pool. You silly duck!");
		}

	}

	void dispose(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO: what if connection.close() throws an exception?

		}
	}
 
	void free(ProxiedConnection pooledConnection) throws SQLException {

		Connection connection = pooledConnection.getUnderlyingConnection();

		if (connection == null) {
			// there's no connection.  i'm ok with ignoring that for now.
			return;
		} 

		synchronized(manager) {
			this.manager.free(connection);
		}
		

	}
 
	private Connection tryGetConnection() throws SQLException {

		 
		synchronized(manager) {
			return manager.requestConnection();
		}
		


	}

	 
    /**
     * Create a pool.
     * 
     * Default: 5 minute idle timeout; create "unlimited" (up to integer.max_value) connections. 
     * @return 
     * @throws ClassNotFoundException 
     * @throws SQLException 
     */
	public static SimpleConnectionPool createDefaultPool(String jdbcDriver, String connectionString, String userName, String password) throws ClassNotFoundException, SQLException {
			
			return createPool(jdbcDriver, connectionString, userName, password, Settings.IDLE_FOREVER, Settings.UNLIMITED_CONNECTIONS);
			
	}
	
	/**
	 * Create a connection pool with the provided connection parameters and idle timeout seconds.
	 * 
	 * @param jdbcDriver
	 * @param connectionString
	 * @param userName
	 * @param password
	 * @param idleTimeoutSeconds Set to Settings.IDLE_FOREVER to ignore this.
	 * @param maxConnections Set to Settings.UNLIMITED_CONNECTIONS to have "unlimited" connections.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static SimpleConnectionPool createPool(String jdbcDriver, String connectionString, String userName, String password,
													int idleTimeoutSeconds, int maxConnections) throws ClassNotFoundException, SQLException {
		GenericSqlConnectionProvider provider = new GenericSqlConnectionProvider(jdbcDriver, connectionString, userName, password);
		Settings settings = new SettingsImpl(provider);
		settings.setMaximumConnections(maxConnections);
		settings.setMaximumIdleTimeSeconds(idleTimeoutSeconds);
		ConnectionManager manager = new ConnectionManagerImpl(settings);
		ProxiedConnectionProvider wrapperProvider = ConfigInjector.getInjector().getInstance(ProxiedConnectionProvider.class);
		
		Monitor monitor = (idleTimeoutSeconds <= 0) ? Monitor.DUMMY_MONITOR : new PoolMonitor(manager);
		return new SimpleConnectionPool(manager, wrapperProvider,monitor);
		
}
	
  /**
   * Create a pool with whatever settings you desire.  
   * 
   * @param settings see com.opower.util.powerpool.Settings
   * @return
   * @throws SQLException
   */
	public static SimpleConnectionPool createPool(Settings settings) throws SQLException {
		ConnectionManager manager = new ConnectionManagerImpl(settings);
		Monitor monitor = (settings.getMaximumIdleTimeSeconds() <= 0) ? Monitor.DUMMY_MONITOR : new PoolMonitor(manager);
	 	ProxiedConnectionProvider wrapperProvider = ConfigInjector.getInjector().getInstance(ProxiedConnectionProvider.class);
		return new SimpleConnectionPool(manager, wrapperProvider, monitor);
		
		
	 
	}
}
