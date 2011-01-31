package com.opower.util.powerpool.connection;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import com.opower.util.powerpool.ConnectionPool;


/**
 * 
 * This is an attempt to cleverly replace the "manually" constructed proxy in
 * ManualProxiedConnection.   It uses reflection to inject an InvocationHandler,
 * I dunno how robust or performant this would be IRL.  
 * 
 * 
 * 
 * @author jennifer_andre
 *
 */
public class AutoProxiedConnection implements java.lang.reflect.InvocationHandler   {
   
    final Connection realConnection; 
	
	final ConnectionPool pool; 
	
	volatile boolean closed = false;
	
	private AutoProxiedConnection(Connection realConnection,
			ConnectionPool pool) 
	{  
		this.realConnection = realConnection;
		
		this.pool = pool;
		
	} 
	
	@SuppressWarnings("rawtypes")
	private static Class[] getProxyInterfaces(Connection connection) {
		@SuppressWarnings("rawtypes") 
		Class[] interfaces = new Class[] { Connection.class,  ProxiedConnection.class };

	  	 return interfaces;
	}
	  public static ProxiedConnection wrapConnection(Connection connection,
				ConnectionPool pool) 
		  {  
			 // the new Connection object should also implement ProxiedConnection. add it.
		  	@SuppressWarnings("rawtypes")
			Class[] interfaces = getProxyInterfaces(connection);
		  	 
		  	 return (ProxiedConnection) java.lang.reflect.Proxy.newProxyInstance(
		  			 ProxiedConnection.class.getClassLoader(),
		             interfaces,
		             new AutoProxiedConnection(connection, pool));
		  }
 	  
	  private boolean isConnectionValid() {
		  return !closed;
	  }
	  
	  private boolean isConnectionMethod(Method m) {
		   
			  if (m.getDeclaringClass().equals(Connection.class))
				  return true;
				   
		  return false;
	  }
	  
	  private void returnConnectionToPool(Connection proxiedConnection) throws SQLException
	  {
		  try {
			  if (isConnectionValid()) {
				  pool.releaseConnection(proxiedConnection);
			  }
			  // otherwise, it's already been released.
		  } finally {
			  clearConnection();
		  }
	  } 
	    
	  
	  // now i must implement all of my overrides and new interface methods...  
	  @Override
	  public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable 
		{
		  
		final String methodName = method.getName();
 		 
		// both of these methods should disable the connection and return it to the pool. 
		if (methodName.equals("close") || methodName.equals("finalize"))
		{		
			returnConnectionToPool((Connection) proxy);
			return null;
 		}
		 
		else if (methodName.equals("isClosed")) 
		{
			return !isConnectionValid();
		} 
		else if (methodName.equals("getUnderlyingConnection")) 
		{		
			return getRealConnection();
 		} 
		else if (methodName.equals("clearConnection")) 
		{					
			clearConnection();
			return null;
		} 
		
		// if it's a member of the Connection interface, we have to be careful.
		// i assume all methods are invalid once a connection is closed; this
		// may not be a great assumption.
		else if (isConnectionMethod(method)) { 	 	
			
			if (isConnectionValid())
				return method.invoke(getRealConnection(), args);
 		
			throw new SQLException("You are attempting a connection operation on an invalid connection.  This SQL connection has already been returned to the connection pool.");
		}
		
		// the java.lang.reflect.proxy stuff auto-proxies calls to equals(), hashCode(), and toString() :(
		// if i don't implement "equals" then jmock really breaks quite hard.
		// TODO: hashCode() and toString() should be implemented if i were going to use this in "production".
		if (methodName.equals("equals"))
			return proxy == args[0];
	 
		// otherwise, just invoke the message. 
		 return method.invoke(getRealConnection(), args);
		
	}

	public void clearConnection() {
		closed = true;
	}

	public Connection getRealConnection() {
		return realConnection;
	}

}
