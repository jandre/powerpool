package com.opower.util.powerpool.connection;

import java.sql.Connection;

import com.opower.util.powerpool.ConnectionPool;

public interface ProxiedConnectionProvider  {

		ProxiedConnection newProxiedConnection(Connection connection,
				ConnectionPool pool);
		
		
		 
		public static final class AutoProxyProvider implements ProxiedConnectionProvider {
			@Override
			public ProxiedConnection newProxiedConnection(Connection connection,
					ConnectionPool pool) {
				return AutoProxiedConnection.wrapConnection(connection, pool);
			}
		}

		public static final class ManualProxyProvider implements ProxiedConnectionProvider {
			@Override
			public ProxiedConnection newProxiedConnection(Connection connection,
					ConnectionPool pool) {
				return new ManualProxiedConnection(connection, pool);
			}
		}
}
