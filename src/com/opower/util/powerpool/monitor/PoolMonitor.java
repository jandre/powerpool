package com.opower.util.powerpool.monitor;

import com.opower.util.powerpool.ConnectionManager;

/** 
 * 
 * A pool monitor monitors used and free items in a connection pool,
 * and enforces things such as idle timeout.
 * 
 * @author jennifer_andre
 *
 */
public class PoolMonitor implements Runnable, Monitor {
	
	final static int MIN_CYCLE_SECONDS = 10;
	
	final int cyclePeriodSeconds;
	
	final ConnectionManager poolManager;
	
	volatile boolean stopped = false;
	
	volatile boolean thread_ended = false;
	
	public PoolMonitor(ConnectionManager poolManager) {
		// default: just every 30 seconds.
		this(poolManager, 30);
	}

	PoolMonitor(ConnectionManager poolManager, int cyclePeriodSeconds) {
		this.poolManager = poolManager;
		if (cyclePeriodSeconds < MIN_CYCLE_SECONDS)
			cyclePeriodSeconds = MIN_CYCLE_SECONDS;
		
		this.cyclePeriodSeconds = cyclePeriodSeconds;
	}
	
	/**
	 * Starts a pool monitor.    
	 * TODO: none of this junk is synchronized, we only call it once internally but we could always be more careful, i suppose.
 	 *
	 */
	public void start() {
		 thread_ended = stopped = false;
		 Thread thread = new Thread(this);
		 thread.setName("PoolMonitor_" + thread.getId());
		 thread.start();
	}
	public void stop() {
		if (stopped)
			return;
		synchronized(poolManager) {
			if (stopped)
				return;
			stopped = true;
			poolManager.notifyAll();
		}
	}
	
	@Override
	public void run() {
		
		while(!stopped) {
			
			synchronized(poolManager) {
				
				poolManager.expireIdleConnections();
					// TODO: anything else? maybe print out some connection stats to log?
			
				try {
					
					poolManager.wait(cyclePeriodSeconds * 1000);
					
				} catch (InterruptedException e) {
					// ok to ignore now, it will be triggered on stop();
				}
			}
		}
		thread_ended = true;
		
	}

	
}
