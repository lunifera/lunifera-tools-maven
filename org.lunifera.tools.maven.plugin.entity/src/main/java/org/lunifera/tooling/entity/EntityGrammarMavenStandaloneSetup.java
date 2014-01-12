package org.lunifera.tooling.entity;

import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.xbase.annotations.XbaseWithAnnotationsStandaloneSetup;
import org.lunifera.dsl.common.xtext.CommonGrammarStandaloneSetup;
import org.lunifera.dsl.entity.xtext.EntityGrammarStandaloneSetup;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class EntityGrammarMavenStandaloneSetup implements ISetup {

	private static Injector injector = null;

	public Injector createInjectorAndDoEMFRegistration() {
		if (injector != null)
			return injector;
		XbaseWithAnnotationsStandaloneSetup.doSetup();
		CommonGrammarStandaloneSetup.doSetup();
		injector = Guice.createInjector(new EntityGrammarMavenModule());
		new EntityGrammarStandaloneSetup().register(injector);
		return injector;
	}
}
