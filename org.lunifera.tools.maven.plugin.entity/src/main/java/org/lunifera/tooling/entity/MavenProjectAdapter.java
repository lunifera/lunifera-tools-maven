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
package org.lunifera.tooling.entity;

import java.util.Iterator;

import org.apache.maven.project.MavenProject;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;

public class MavenProjectAdapter extends AdapterImpl {
	public static MavenProject get(ResourceSet rs) {
		for (Adapter a : rs.eAdapters())
			if (a instanceof MavenProjectAdapter)
				return ((MavenProjectAdapter) a).project;
		throw new RuntimeException("The Maven Project is not registered in the ResourceSet");
	}

	public static void install(ResourceSet rs, MavenProject project) {
		Iterator<Adapter> i = rs.eAdapters().iterator();
		while (i.hasNext())
			if (i.next() instanceof MavenProjectAdapter)
				i.remove();
		rs.eAdapters().add(new MavenProjectAdapter(project));
	}

	private MavenProject project;

	private MavenProjectAdapter(MavenProject project) {
		super();
		this.project = project;
	}
}
