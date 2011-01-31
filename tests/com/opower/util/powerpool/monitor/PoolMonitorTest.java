package com.opower.util.powerpool.monitor;


import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Expectation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opower.util.powerpool.ConnectionManager; 

public class PoolMonitorTest {

	Mockery context;
	ConnectionManager manager;
	
	
	@Before
	public void setUp() throws Exception {
		context = new Mockery();
		manager = context.mock(ConnectionManager.class);
	}

	@After
	public void tearDown() throws Exception {
	}
	 
	@Test
	public void testMinimumCycleResetsToTenSecondsIfLower() throws InterruptedException {
		PoolMonitor monitor = new PoolMonitor(manager, -100);
		
		Assert.assertEquals(10, monitor.cyclePeriodSeconds);
		 monitor = new PoolMonitor(manager, 0);
		
		Assert.assertEquals(10, monitor.cyclePeriodSeconds);
		monitor = new PoolMonitor(manager, 9);
		
		Assert.assertEquals(10, monitor.cyclePeriodSeconds);
 		
	}

	// not an ideal test, because of race/thread scheduling could fail :(
	@Test
	public void testPoolMonitorCallsExpireIdleConnections() throws InterruptedException {
		PoolMonitor monitor = new PoolMonitor(manager, 10);
		Assert.assertEquals(monitor.thread_ended, false);
		context.checking(new Expectations() {{ atLeast(1).of(manager).expireIdleConnections();}});
		try 
		{ 
			monitor.start();
			 
			Thread.sleep(11 * 1000);
		} catch (InterruptedException e) {} 
		finally {
			monitor.stop();
		}
		// wait for stop.
		
		Thread.sleep(1 * 1000);
		Assert.assertEquals(monitor.thread_ended, true);
		
		
		
		
	}

}
