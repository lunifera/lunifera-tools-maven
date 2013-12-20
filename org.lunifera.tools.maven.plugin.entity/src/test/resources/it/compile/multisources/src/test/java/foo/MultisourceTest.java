package foo;

import org.junit.Assert;
import org.junit.Test;

public class MultisourceTest {

	@Test
	public void assertTrue() throws ClassNotFoundException {
	    Assert.assertNotNull(Class.forName("org.test.multisource.EntityA"));
	    Assert.assertNotNull(Class.forName("org.test.multisource.EntityB"));
	    Assert.assertNotNull(Class.forName("org.test.multisource2.EntityC"));
	}
}