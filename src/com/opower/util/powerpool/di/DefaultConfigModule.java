package com.opower.util.powerpool.di;

import com.google.inject.AbstractModule;
import com.opower.util.powerpool.Settings;
import com.opower.util.powerpool.SettingsImpl;
import com.opower.util.powerpool.connection.ProxiedConnectionProvider;


/*
 * Currently, only binding the proxy class implementation.  i dunno, not sure how much belongs here,
 * ideally the consuming code could determine what framework they wanted to use (guice, spring, etc).
 * 
 */
public class DefaultConfigModule extends AbstractModule {

	
	@Override
	protected void configure() {
		  	bind(ProxiedConnectionProvider.class).to(ProxiedConnectionProvider.ManualProxyProvider.class);
		
	}
	

}
