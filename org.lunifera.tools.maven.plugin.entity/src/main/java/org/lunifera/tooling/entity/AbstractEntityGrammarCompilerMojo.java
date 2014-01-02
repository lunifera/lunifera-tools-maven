/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.lunifera.tooling.entity;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.xtext.util.Strings.concat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.xtext.xbase.file.ProjectConfig;
import org.eclipse.xtext.xbase.file.RuntimeWorkspaceConfigProvider;
import org.eclipse.xtext.xbase.file.WorkspaceConfig;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.lunifera.dsl.entity.xtext.compiler.batch.EntityGrammarBatchCompiler;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * @author Michael Clay - Initial contribution and API
 */
public abstract class AbstractEntityGrammarCompilerMojo extends AbstractEntityGrammarMojo {
	protected static final Predicate<String> FILE_EXISTS = new Predicate<String>() {

		public boolean apply(String filePath) {
			return new File(filePath).exists();
		}
	};

	@Inject
	protected Provider<EntityGrammarBatchCompiler> entityGrammarBatchCompilerProvider;

	/**
	 * Entity-File encoding argument for the compiler.
	 * 
	 * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
	 */
	protected String encoding;

	/**
	 * Set this to false to suppress the creation of *._trace files.
	 * 
	 * @parameter default-value="true" expression="${writeTraceFiles}"
	 */
	protected boolean writeTraceFiles;

	/**
	 * Location of the Entity settings file.
	 * 
	 * @parameter default-value="${basedir}/.settings/org.eclipse.xtend.core.Entity.prefs"
	 * @readonly
	 */
	private String propertiesFileLocation;

	@Inject
	private RuntimeWorkspaceConfigProvider workspaceConfigProvider;

	protected EntityGrammarBatchCompiler createEntityGrammarBatchCompiler() {
		Injector injector = new EntityGrammarMavenStandaloneSetup().createInjectorAndDoEMFRegistration();
		EntityGrammarBatchCompiler instance = injector.getInstance(EntityGrammarBatchCompiler.class);
		return instance;
	}

	protected void compile(EntityGrammarBatchCompiler entityBatchCompiler, String classPath,
			List<String> sourceDirectories, String outputPath) throws MojoExecutionException {

		configureWorkspace(sourceDirectories, outputPath);
		entityBatchCompiler.setResourceSetProvider(new MavenProjectResourceSetProvider(project));
		Iterable<String> filtered = filter(sourceDirectories, FILE_EXISTS);
		if (Iterables.isEmpty(filtered)) {
			getLog().info(
					"skip compiling sources because the configured directory '" + Iterables.toString(sourceDirectories)
							+ "' does not exists.");
			return;
		}
		getLog().debug("Set temp directory: " + getTempDirectory());
		entityBatchCompiler.setTempDirectory(getTempDirectory());
		getLog().debug("Set DeleteTempDirectory: " + false);
		entityBatchCompiler.setDeleteTempDirectory(false);
		getLog().debug("Set classpath: " + classPath);
		entityBatchCompiler.setClassPath(classPath);
		getLog().debug("Set source path: " + concat(File.pathSeparator, newArrayList(filtered)));
		entityBatchCompiler.setSourcePath(concat(File.pathSeparator, newArrayList(filtered)));
		getLog().debug("Set output path: " + outputPath);
		entityBatchCompiler.setOutputPath(outputPath);
		getLog().debug("Set encoding: " + encoding);
		entityBatchCompiler.setFileEncoding(encoding);
		getLog().debug("Set writeTraceFiles: " + writeTraceFiles);
		entityBatchCompiler.setWriteTraceFiles(writeTraceFiles);
		if (!entityBatchCompiler.compile()) {
			throw new MojoExecutionException("Error compiling entity sources in '"
					+ concat(File.pathSeparator, newArrayList(filtered)) + "'.");
		}
	}

	private void configureWorkspace(List<String> sourceDirectories, String outputPath) throws MojoExecutionException {
		WorkspaceConfig workspaceConfig = new WorkspaceConfig(project.getBasedir().getParentFile().getAbsolutePath());
		ProjectConfig projectConfig = new ProjectConfig(project.getBasedir().getName());
		URI absoluteRootPath = project.getBasedir().getAbsoluteFile().toURI();
		URI relativizedTarget = absoluteRootPath.relativize(new File(outputPath).toURI());
		if (relativizedTarget.isAbsolute()) {
			throw new MojoExecutionException("Output path '" + outputPath + "' must be a child of the project folder '"
					+ absoluteRootPath + "'");
		}
		for (String source : sourceDirectories) {
			URI relativizedSrc = absoluteRootPath.relativize(new File(source).toURI());
			if (relativizedSrc.isAbsolute()) {
				throw new MojoExecutionException("Source folder " + source + " must be a child of the project folder "
						+ absoluteRootPath);
			}
			projectConfig.addSourceFolderMapping(relativizedSrc.getPath(), relativizedTarget.getPath());
		}
		workspaceConfig.addProjectConfig(projectConfig);
		workspaceConfigProvider.setWorkspaceConfig(workspaceConfig);
		if (getLog().isDebugEnabled()) {
			getLog().debug("WS config root: " + workspaceConfig.getAbsoluteFileSystemPath());
			getLog().debug("Project name: " + projectConfig.getName());
			getLog().debug("Project root path: " + projectConfig.getRootPath());
			for (Entry<org.eclipse.xtend.lib.macro.file.Path, org.eclipse.xtend.lib.macro.file.Path> entry : projectConfig
					.getSourceFolderMappings().entrySet()) {
				getLog().debug("Source path: " + entry.getKey() + " -> " + entry.getValue());
			}
		}
	}

	protected abstract String getTempDirectory();

	protected void addDependencies(Set<String> classPath, List<Artifact> dependencies) {
		for (Artifact artifact : dependencies) {
			classPath.add(artifact.getFile().getAbsolutePath());
		}
	}

	protected void readEntityEclipseSetting(String sourceDirectory, Procedure1<String> fieldSetter) {
		if (propertiesFileLocation != null) {
			File f = new File(propertiesFileLocation);
			if (f.canRead()) {
				Properties entitySettings = new Properties();
				try {
					entitySettings.load(new FileInputStream(f));
					// TODO read Entity setup to compute the properties file loc and property name
					String entityOutputDirProp = entitySettings.getProperty("outlet.DEFAULT_OUTPUT.directory", null);
					if (entityOutputDirProp != null) {
						File srcDir = new File(sourceDirectory);
						getLog().debug("Source dir : " + srcDir.getPath() + " exists " + srcDir.exists());
						if (srcDir.exists() && srcDir.getParent() != null) {
							String path = new File(srcDir.getParent(), entityOutputDirProp).getPath();
							getLog().debug("Applying Entity property: " + entityOutputDirProp);
							fieldSetter.apply(path);
						}
					}
				} catch (FileNotFoundException e) {
					getLog().warn(e);
				} catch (IOException e) {
					getLog().warn(e);
				}
			} else {
				getLog().info(
						"Can't find Entity properties under " + propertiesFileLocation + ", maven defaults are used.");
			}
		}
	}

	protected String resolveToBaseDir(final String directory) throws MojoExecutionException {
		File outDir = new File(directory);
		if (!outDir.isAbsolute()) {
			outDir = new File(project.getBasedir(), directory);
		}
		return outDir.getAbsolutePath();
	}
}
