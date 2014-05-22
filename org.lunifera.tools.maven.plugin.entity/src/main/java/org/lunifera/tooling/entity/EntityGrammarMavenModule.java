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

import org.eclipse.xtext.generator.trace.ITraceURIConverter;
import org.lunifera.dsl.entity.xtext.EntityGrammarRuntimeModule;

public class EntityGrammarMavenModule extends EntityGrammarRuntimeModule {

	public Class<? extends ITraceURIConverter> bindITraceURIConverter() {
		return MavenTraceURIConverter.class;
	}



}
