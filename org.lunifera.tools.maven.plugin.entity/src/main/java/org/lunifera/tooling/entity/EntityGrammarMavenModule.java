package org.lunifera.tooling.entity;

import org.eclipse.xtext.generator.trace.ITraceURIConverter;
import org.lunifera.dsl.entity.xtext.EntityGrammarRuntimeModule;

public class EntityGrammarMavenModule extends EntityGrammarRuntimeModule {

	public Class<? extends ITraceURIConverter> bindITraceURIConverter() {
		return MavenTraceURIConverter.class;
	}



}
