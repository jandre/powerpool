package com.opower.util.powerpool;

import com.opower.util.powerpool.provider.NewConnectionProvider;

public interface Settings {

	
	final static int UNLIMITED_CONNECTIONS = Integer.MAX_VALUE;
	 
	final static int DEFAULT_MAXIMUM_IDLE_SECONDS = 60 * 5;
	
	public final static int IDLE_FOREVER = -1;
	
	public abstract void setValidConnectionTester(
			ConnectionTester validConnectionTester);

	public abstract ConnectionTester getValidConnectionTester();

	/*
	 * Set the maximum connections.  Anything >= 0 is a valid value,
	 * anything less than that will reset the maximumConnections to the default (Integer.MAX_VALUE).
	 */
	public abstract void setMaximumConnections(int maximumConnections);

	public abstract int getMaximumConnections();

	public abstract void setMaximumIdleTimeSeconds(int maximumIdleTimeSeconds);

	public abstract int getMaximumIdleTimeSeconds();

	public abstract void setNewConnectionProvider(
			NewConnectionProvider newConnectionProvider);

	public abstract NewConnectionProvider getNewConnectionProvider();

}