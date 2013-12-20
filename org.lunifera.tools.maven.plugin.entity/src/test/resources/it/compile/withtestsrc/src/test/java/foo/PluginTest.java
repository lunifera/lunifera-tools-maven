package foo;

import org.junit.Assert;
import org.junit.Test;

public class PluginTest {
	
	@Test
	public void assertTrue() throws ClassNotFoundException {
	    Assert.assertNotNull(Class.forName("org.test.withtestsrc.EntityA"));
	}
}