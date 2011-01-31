package com.opower.util.powerpool;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.Connection;

/**
 * More interesting information could be recorded here..
 * 
 * @author jennifer_andre
 *
 */
public class UsedConnectionStatistics {
	   
	final long assignedTime = System.currentTimeMillis();

	public long getAssignedTime() {
		return assignedTime;
	}
  	  
	 
}
