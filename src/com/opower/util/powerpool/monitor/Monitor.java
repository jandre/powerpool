package com.opower.util.powerpool.monitor;

/**
 * A monitor monitors the connection pool and performs period cleanup actions.
 * 
 * @author jennifer_andre
 *
 */
public interface Monitor {
	void start();
	void stop();
	
	public static final Monitor DUMMY_MONITOR = 
		  new Monitor() {

			@Override
			public void start() {
				// nothing.
				
			}

			@Override
			public void stop() { 
				//nothing.
				
			}
 

	};
}
