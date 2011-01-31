package com.opower.util.powerpool;

import java.sql.Connection;

public interface ConnectionTester {
	
	boolean isConnectionValid(Connection c);
	
}
