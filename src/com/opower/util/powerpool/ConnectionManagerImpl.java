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
import com.opower.util.powerpool.provider.NewConnectionProvider;

/**
 * TODO: Not thread safe.  Any accesses to this class need to lock on this object.
 * 
 * @author jennifer_andre
 *
 */
public class ConnectionManagerImpl implements ConnectionManager   {
	
	Queue<IdleConnection> idleConnections = new LinkedBlockingQueue<IdleConnection>();
 
	HashMap<Connection, UsedConnectionStatistics> usedConnections = new HashMap<Connection, UsedConnectionStatistics>();

	final Settings settings;
	
	volatile boolean stopped = false;
	 
	public ConnectionManagerImpl(Settings settings) throws SQLException {
		this.settings = settings; 
		
	} 
	 
	 
	void dispose(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO: what if connection.close() throws an exception?

		}
	}
   
	Connection tryGetIdleConnection() throws SQLException {

		IdleConnection item = null;

		while((item = this.idleConnections.poll()) != null) {
			{
				final Connection connection = item.getConnection();
				// test the connection.
				if (settings.getValidConnectionTester().isConnectionValid(connection))
					return connection;
				else // throw away this connection, it's bad.
					dispose(connection);

			}

		}
		return null;
	}
 
    private void requeueConnection(Connection connection) {
		// first: check to see if it's still valid before re-pooling it.
		if (settings.getValidConnectionTester().isConnectionValid(connection))
		{
			IdleConnection freedConnection = new IdleConnection(connection);
			this.idleConnections.add(freedConnection);

		} else { 
			// else, just throw it away silently? TODO: exception, maybe?
			dispose(connection);

		}


	}
     
    /* (non-Javadoc)
	 * @see com.opower.util.powerpool.ConnectionManager#free(java.sql.Connection)
	 */
    @Override
	public void free(Connection connection) throws SQLException {

 		 
 		UsedConnectionStatistics statistics = this.usedConnections.get(connection);

 		if (statistics != null) 
 		{ 
 			if (!stopped)
 				requeueConnection(connection);
 			
 			usedConnections.remove(connection);

 		}  
 		// if the connection wasn't in the pool, then no harm/no foul?

 	}
    
    /* (non-Javadoc)
	 * @see com.opower.util.powerpool.ConnectionManager#requestConnection()
	 */
    @Override
	public  Connection requestConnection() throws SQLException {

 		// first, try to get an idle cached connection.
 		Connection connection =  tryGetIdleConnection();

 		if (connection == null) { 
 			
 			final int size = usedConnections.size();
 	
 			if (size >= settings.getMaximumConnections())
 				throw new SQLException("Pool usage has exceeded the maximum number of configured connections=" + settings.getMaximumConnections());
 	
 			connection = 
 				settings.getNewConnectionProvider().newConnection();
 		}
 		
 		usedConnections.put(connection, new UsedConnectionStatistics());

 		return connection; 


 	}
	
	
    /* (non-Javadoc)
	 * @see com.opower.util.powerpool.ConnectionManager#expireIdleConnections()
	 */
    @Override
	public void expireIdleConnections() 
    {
		// simple logic to do this: pop each
		// connection, and if still valid, re-add it to the queue.
		
		final int maxIdleMs = settings.getMaximumIdleTimeSeconds() * 1000;
		
		if (maxIdleMs <= 0)
			return; 			// no idle enforcement.
		
		IdleConnection item = null;
		List<IdleConnection> itemsToRequeue = new ArrayList<IdleConnection>();
		while((item = this.idleConnections.poll()) != null) {
			 
				final Connection connection = item.getConnection();
				// test the connection.
				 if (item.getIdleTimeMilliseconds() > maxIdleMs) {
				// throw away this connection, it's bad.
					dispose(connection);
				 } else {
					 itemsToRequeue.add(item);
				 }
 
		}
		idleConnections.addAll(itemsToRequeue);
		
		
	}




	@Override
	public void close() {
		
		if (stopped)
			return;
		
		// clear all idle connections.
		IdleConnection item;
		while((item = this.idleConnections.poll()) != null) {
			dispose(item.getConnection());
		}
		stopped = true;
		
	}

}
