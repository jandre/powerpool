package com.opower.util.powerpool;

import static org.junit.Assert.*;

import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opower.util.powerpool.provider.NewConnectionProvider;

public class SettingsImplTest {


	Mockery context; 
	NewConnectionProvider provider;
	@Before
	public void setUp() throws Exception {
		context = new Mockery();
		context.mock(NewConnectionProvider.class);
	}

	@After
	public void tearDown() throws Exception {
		context.assertIsSatisfied();
	}

	@Test
	public final void testSetMaximumConnectionsResetsToUnlimitedIfSizeLessThanOrEqualToZero() {
		 SettingsImpl impl = new SettingsImpl(provider) ;
		 
		 impl.setMaximumConnections(5);

		 Assert.assertEquals(5, impl.getMaximumConnections());
		 impl.setMaximumConnections(-100);
		 
		 Assert.assertEquals(Settings.UNLIMITED_CONNECTIONS, impl.getMaximumConnections());
		 
	}	 

	 

}
