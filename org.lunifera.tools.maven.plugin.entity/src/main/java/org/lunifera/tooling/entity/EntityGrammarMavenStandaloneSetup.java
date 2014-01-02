package org.lunifera.tooling.entity;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.xbase.annotations.XbaseWithAnnotationsStandaloneSetup;
import org.lunifera.dsl.common.xtext.CommonGrammarStandaloneSetup;
import org.lunifera.dsl.entity.xtext.EntityGrammarStandaloneSetupGenerated;
import org.lunifera.dsl.semantic.entity.EntityPackage;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class EntityGrammarMavenStandaloneSetup implements ISetup {

	private static Injector injector = null;

	public Injector createInjectorAndDoEMFRegistration() {
		if (injector != null)
			return injector;
		XbaseWithAnnotationsStandaloneSetup.doSetup();
		CommonGrammarStandaloneSetup.doSetup();
		EPackage.Registry.INSTANCE.put(EntityPackage.eINSTANCE.getNsURI(),
				EntityPackage.eINSTANCE);
		injector = Guice.createInjector(new EntityGrammarMavenModule());
		new EntityGrammarStandaloneSetupGenerated().register(injector);
		return injector;
	}
}
