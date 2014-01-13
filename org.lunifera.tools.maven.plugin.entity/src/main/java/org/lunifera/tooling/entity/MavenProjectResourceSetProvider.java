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

import org.apache.maven.project.MavenProject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Provider;

public class MavenProjectResourceSetProvider implements Provider<ResourceSet> {

	private MavenProject project;

	public MavenProjectResourceSetProvider(MavenProject project) {
		super();
		this.project = project;
	}

	public ResourceSet get() {
		ResourceSet rs = new XtextResourceSet();
		MavenProjectAdapter.install(rs, project);
		return rs;
	}
}
