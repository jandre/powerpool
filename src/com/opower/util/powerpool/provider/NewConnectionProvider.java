package com.opower.util.powerpool.provider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * Something that provides new connections. 
 * 
 * @author jennifer_andre
 *
 */
public interface NewConnectionProvider  {

		Connection newConnection() throws SQLException;
}
