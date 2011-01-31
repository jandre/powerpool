package com.opower.util.powerpool.connection;

import java.sql.Connection;

public interface ProxiedConnection extends Connection {
	
	
	Connection getUnderlyingConnection();  
    void clearConnection();
}
