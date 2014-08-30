package org.lunifera.tools.maven.plugin.subsystems;

/*
 * #%L
 * Lunifera Maven Tools : Subsystem Plugin
 * %%
 * Copyright (C) 2012 - 2014 C4biz Softwares ME, Loetz KG
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

/**
 * The Subsystem-Type header specifies the type for this Subsystem. Three
 * types of Subsystems must be supported: osgi.subsystem.application,
 * osgi.subsystem.composite and osgi.subsystem.feature.
 * 
 * @author cvgaviao
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
