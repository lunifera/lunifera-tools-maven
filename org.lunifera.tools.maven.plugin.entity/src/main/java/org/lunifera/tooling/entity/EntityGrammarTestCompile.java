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

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.xtext.util.Strings.concat;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.lunifera.dsl.entity.xtext.compiler.batch.EntityGrammarBatchCompiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Goal which compiles Entity test sources.
 * 
 * @goal testCompile
 * @phase generate-test-sources
 * @requiresDependencyResolution test
 */
public class EntityGrammarTestCompile extends AbstractEntityGrammarCompilerMojo {
	/**
	 * Location of the generated test files.
	 * 
	 * @parameter default-value="${basedir}/src/test/generated-sources/entity"
	 * @required
	 */
	private String testOutputDirectory;
	/**
	 * Location of the temporary compiler directory.
	 * 
	 * @parameter default-value="${project.build.directory}/entity-test"
	 * @required
	 */
	private String testTempDirectory;

	@Override
	protected void internalExecute() throws MojoExecutionException {
		final String defaultValue = project.getBasedir() + "/src/test/generated-sources/entity";
		getLog().debug("Output directory '" + testOutputDirectory + "'");
		getLog().debug("Default directory '" + defaultValue + "'");
		if (defaultValue.equals(testOutputDirectory)) {
			readEntityEclipseSetting(project.getBuild().getTestSourceDirectory(), new Procedure1<String>() {
				public void apply(String entityOutputDir) {
					testOutputDirectory = entityOutputDir;
					getLog().info("Using Entity output directory '" + testOutputDirectory + "'");
				}
			});
		}
		testOutputDirectory = resolveToBaseDir(testOutputDirectory);
		compileTestSources(entityGrammarBatchCompilerProvider.get());
	}

	protected void compileTestSources(EntityGrammarBatchCompiler entity2BatchCompiler) throws MojoExecutionException {
		List<String> testCompileSourceRoots = Lists.newArrayList(project.getTestCompileSourceRoots());
		String testClassPath = concat(File.pathSeparator, getTestClassPath());
		project.addTestCompileSourceRoot(testOutputDirectory);
		compile(entity2BatchCompiler, testClassPath, testCompileSourceRoots, testOutputDirectory);
	}

	@SuppressWarnings("deprecation")
	protected List<String> getTestClassPath() {
		Set<String> classPath = Sets.newLinkedHashSet();
		classPath.add(project.getBuild().getTestSourceDirectory());
		try {
			classPath.addAll(project.getTestClasspathElements());
		} catch (DependencyResolutionRequiredException e) {
			throw new WrappedException(e);
		}
		addDependencies(classPath, project.getTestArtifacts());
		return newArrayList(filter(classPath, FILE_EXISTS));
	}

	@Override
	protected String getTempDirectory() {
		return testTempDirectory;
	}

}
