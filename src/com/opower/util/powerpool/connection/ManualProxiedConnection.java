package com.opower.util.powerpool.connection;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import com.opower.util.powerpool.ConnectionPool;

/**
 * 
 *  This class is a proxy class for a Connection. 
 *  it ensures that even if "close" is called on the connection,
 *  or if the caller forgets to use releaseConnection() interface,
 *  it returns the object to the connection pool.
 *    
 *  Any attempt to use the connection thereafter will cause an exception
 *  stating that the connection has been returned to the pool.
 *  
 *  See my attempt to be cleverer in AutoProxiedConnection. :(
 *  
 *  By the way: NOT thread safe. 
 *  
 *  TODO:  all of these methods should really have test methods.  
 *  
 * @author jennifer_andre
 *
 */
public class ManualProxiedConnection implements java.sql.Connection, ProxiedConnection {

    private Connection realConnection = null; 
	
	final ConnectionPool pool; 
	
	public ManualProxiedConnection(Connection realConnection,
			ConnectionPool pool) 
	{ 
		this.realConnection = realConnection;
		this.pool = pool;
		
	} 
	
	public void clearConnection() {
		this.realConnection = null;
	}
	
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
		
	}
	
	void assertNotClosed() throws SQLException {
		if (getUnderlyingConnection() == null)
			throw new SQLException("Either close() was called, or the connection has been returned to the connection pool.  You must create a new connection.");
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return getUnderlyingConnection().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return getUnderlyingConnection().isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		return getUnderlyingConnection().createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return getUnderlyingConnection().prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return getUnderlyingConnection().prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return getUnderlyingConnection().nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		 getUnderlyingConnection().setAutoCommit(autoCommit);
		
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return getUnderlyingConnection().getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		getUnderlyingConnection().commit();
		
	}

	@Override
	public void rollback() throws SQLException {
		getUnderlyingConnection().rollback();
		
	}

	@Override
	public void close() throws SQLException {
		
		// in our case, closing a pooled connection merely means returning it to the pool. 

			try {
				if (this.getUnderlyingConnection() != null)
					pool.releaseConnection(this);
	 		} finally {
	 			// this is really redundant.
	 			clearConnection();
			}
		 
	
	}

	@Override
	public boolean isClosed() throws SQLException {
		return this.realConnection != null;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return getUnderlyingConnection().getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		  getUnderlyingConnection().setReadOnly(readOnly);
		
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return getUnderlyingConnection().isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		getUnderlyingConnection().setCatalog(catalog);
		
	}

	@Override
	public String getCatalog() throws SQLException {
		return getUnderlyingConnection().getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		getUnderlyingConnection().setTransactionIsolation(level);
		
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return getUnderlyingConnection().getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return getUnderlyingConnection().getWarnings();
	 
	}

	@Override
	public void clearWarnings() throws SQLException {
		getUnderlyingConnection().clearWarnings();
		
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return getUnderlyingConnection().createStatement(resultSetType, resultSetConcurrency);
		
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return getUnderlyingConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return getUnderlyingConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return getUnderlyingConnection().getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		getUnderlyingConnection().setTypeMap(map);
		
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		getUnderlyingConnection().setHoldability(holdability);
		
	}

	@Override
	public int getHoldability() throws SQLException {
		return getUnderlyingConnection().getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return getUnderlyingConnection().setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return getUnderlyingConnection().setSavepoint(name);
		
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		getUnderlyingConnection().rollback(savepoint);
		
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		getUnderlyingConnection().releaseSavepoint(savepoint);
		
	}

	@Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return getUnderlyingConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return getUnderlyingConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return getUnderlyingConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return getUnderlyingConnection().prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return getUnderlyingConnection().prepareStatement(sql, columnIndexes);
		
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return getUnderlyingConnection().prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		return getUnderlyingConnection().createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return getUnderlyingConnection().createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return getUnderlyingConnection().createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return getUnderlyingConnection().createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return getUnderlyingConnection().isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		getUnderlyingConnection().setClientInfo(name, value);
		
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		getUnderlyingConnection().setClientInfo(properties);
		
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return getUnderlyingConnection().getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
 		return getUnderlyingConnection().getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
 		return getUnderlyingConnection().createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		
		return getUnderlyingConnection().createStruct(typeName, attributes);
			 
	}
 
	public Connection getUnderlyingConnection() {
		return realConnection;
	}

}
