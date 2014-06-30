package org.lunifera.tools.maven.plugin.subsystems;

/*
 * #%L
 * Lunifera Maven : Subsystem Plugin
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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * 
 * @author cvgaviao
 *
 */
@Mojo(name = "gen-manifest-feature", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MojoGenSubsystemFeatureManifest extends
		AbstractMojoGenSubsystemManifest {

	public MojoGenSubsystemFeatureManifest() {
	}

	@Override
	protected SubsystemType getSubsystemType() {
		return SubsystemType.FEATURE;
	}

}
