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

import java.io.File;
import java.io.IOException;

import org.apache.maven.project.MavenProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.trace.DefaultTraceURIConverter;
import org.eclipse.xtext.generator.trace.TraceURIHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.RuntimeIOException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class MavenTraceURIConverter extends DefaultTraceURIConverter {

	@Inject
	private TraceURIHelper traceURIHelper;
	
	@Override
	public URI getURIForTrace(XtextResource context) {
		MavenProject project = MavenProjectAdapter.get(context.getResourceSet());
		URI uri = context.getResourceSet().getURIConverter().normalize(context.getURI());
		URI result = deresolve(project, uri);
		return result;
	}

	protected URI deresolve(MavenProject project, URI uri) {
		Iterable<String> roots = Iterables.concat(project.getCompileSourceRoots(), project.getTestCompileSourceRoots());
		for (String rootString : roots) {
			URI root = null;
			try {
				String canonicalPath = new File(rootString).getCanonicalPath();
				canonicalPath += "/";
				root = URI.createFileURI(canonicalPath);
				if (traceURIHelper.isPrefix(root, uri))
					return uri.deresolve(root);
			} catch (IOException e) {
				throw new RuntimeIOException(e);
			}
		}
		throw new RuntimeException("Could not find source folder for '" + uri + "'. Folders:"
				+ Lists.newArrayList(roots));
	}
}
