package com.opower.util.powerpool;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IdleConnectionTest {
 
	@Test
	public void testGetIdleTimeMillisecondsIncreasesAsTimePasses() throws Exception {
		long time = System.currentTimeMillis();
		IdleConnection connection = new IdleConnection(null,100);
		long idleTime = connection.getIdleTimeMilliseconds();
		Assert.assertTrue((time - 100) <=  idleTime);
		Thread.sleep(1);
		Assert.assertTrue(idleTime <= connection.getIdleTimeMilliseconds() );
	}
}
