package com.opower.util.powerpool.di;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ConfigInjector {
	
	private static Injector injector = Guice.createInjector(new DefaultConfigModule());;
	 
	public static Injector getInjector() {
		return injector;
	}
	public static void setInjector(Injector value) {
		  injector = value;
	}
	 

}
