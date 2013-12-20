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
import org.lunifera.dsl.entity.xtext.compiler.EntityGrammarBatchCompiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Goal which compiles Entity sources.
 * 
 * @author Michael Clay - Initial contribution and API
 * @goal compile
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class EntityGrammarCompile extends AbstractEntityGrammarCompilerMojo {
	/** 
	 * Location of the generated source files.
	 * 
	 * @parameter default-value="${basedir}/src/main/generated-sources/entity"
	 * @required
	 */
	private String outputDirectory;
	/**
	 * Location of the temporary compiler directory.
	 * 
	 * @parameter default-value="${project.build.directory}/entity"
	 * @required
	 */
	private String tempDirectory;

	@Override
	protected void internalExecute() throws MojoExecutionException {
		final String defaultValue = project.getBasedir() + "/src/main/generated-sources/entity";
		getLog().debug("Output directory '" + outputDirectory + "'");
		getLog().debug("Default directory '" + defaultValue + "'");
		// IF output is not explicitly set try to read entity prefs from eclipse .settings folder
		if (defaultValue.equals(outputDirectory)) {
			readEntityEclipseSetting(project.getBuild().getSourceDirectory(), new Procedure1<String>() {
				public void apply(String entityOutputDir) {
					outputDirectory = entityOutputDir;
					getLog().info("Using Entity output directory '" + outputDirectory + "'");
				}
			});
		}
		outputDirectory = resolveToBaseDir(outputDirectory);
		compileSources(entityGrammarBatchCompilerProvider.get());
	}

	private void compileSources(EntityGrammarBatchCompiler entity2BatchCompiler) throws MojoExecutionException {
		List<String> compileSourceRoots = Lists.newArrayList(project.getCompileSourceRoots());
		String classPath = concat(File.pathSeparator, getClassPath());
		project.addCompileSourceRoot(outputDirectory);
		compile(entity2BatchCompiler, classPath, compileSourceRoots, outputDirectory);
	}

	@SuppressWarnings("deprecation")
	protected List<String> getClassPath() {
		Set<String> classPath = Sets.newLinkedHashSet();
		classPath.add(project.getBuild().getSourceDirectory());
		try {
			classPath.addAll(project.getCompileClasspathElements());
		} catch (DependencyResolutionRequiredException e) {
			throw new WrappedException(e);
		}
		addDependencies(classPath, project.getCompileArtifacts());
		return newArrayList(filter(classPath, FILE_EXISTS));
	}

	@Override
	protected String getTempDirectory() {
		return tempDirectory;
	}

}
