/**
 * Copyright (c) 2011 - 2014, Lunifera GmbH (Gross Enzersdorf)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Based on Xtend Maven Plugin
 * 
 * Contributors: 
 * 		Florian Pirchner - Initial implementation
 */
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