/*******************************************************************************
 * Copyright (c) 2013, 2014 C4biz Softwares ME, Loetz KG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Cristiano Gavião - initial API and implementation
 *******************************************************************************/
package org.lunifera.tools.maven.plugin.subsystems;

/**
 * The Subsystem-Type header specifies the type for this Subsystem. Three
 * types of Subsystems must be supported: osgi.subsystem.application,
 * osgi.subsystem.composite and osgi.subsystem.feature.
 */
public enum SubsystemType {

	APPLICATION("osgi.subsystem.application"), COMPOSITE(
			"osgi.subsystem.composite"), FEATURE("osgi.subsystem.feature");

	private String value;

	private SubsystemType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	
}
