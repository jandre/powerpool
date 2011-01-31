package com.opower.util.powerpool;

import java.sql.Connection;

public class IdleConnection 
{
	private  final Connection connection;
	
	private final long lastUsedTime;
 	
	public IdleConnection(Connection connection, long lastUsedTime) {
		this.lastUsedTime = lastUsedTime;
		this.connection = connection;
		
	}
	public IdleConnection(Connection connection)
	{ 	
		this(connection, System.currentTimeMillis());
		
 	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public long getIdleTimeMilliseconds() {
		return System.currentTimeMillis() - lastUsedTime;
	}
}
