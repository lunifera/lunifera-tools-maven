package org.lunifera.tools.maven.plugin.bindex;

/*
 * #%L
 * Lunifera Maven Tools : OSGi Repository Indexer Plugin
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

@Mojo(name = "installOnIndexAggregator", defaultPhase = LifecyclePhase.INSTALL,
        threadSafe = true, requiresProject = true, inheritByDefault = false,
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.NONE)
public class MojoInstallArtifactOnIndexAggregator extends AbstractMojo {

    /**
     * Base directory of the project.
     */
    @Parameter(required = true, property = "basedir", readonly = true)
    private File basedir;

    /**
     * Points to the local repository being used by Aether.
     */
    @Parameter(required = true, property = "localRepository", readonly = true)
    private ArtifactRepository localRepository;

    /**
     * 
     */
    @Parameter(required = true, property = "reactorProjects", readonly = true)
    private List<MavenProject> reactorProjects;

    /**
     * Component used to resolve artifacts being handled by the plugin.
     */
    @Component
    private RepositorySystem repositorySystem;

    protected Set<Artifact> calculateFileSetToIndex(
            List<PathMatcher> includeMatchers, List<PathMatcher> excludeMatchers)
            throws MojoExecutionException {
        Set<Artifact> artifactsToIndex = new HashSet<>();
        if (reactorProjects != null) {
            for (MavenProject mavenProject : reactorProjects) {
                Artifact artifact = mavenProject.getArtifact();
                ArtifactResolutionRequest request = new ArtifactResolutionRequest()
                        .setArtifact(artifact).setLocalRepository(
                                localRepository);
                ArtifactResolutionResult resolutionResult = repositorySystem
                        .resolve(request);
                if (resolutionResult.hasExceptions()) {
                    throw new MojoExecutionException(
                            "Could not resolve artifact: " + artifact,
                            resolutionResult.getExceptions().get(0));
                } else {
                    artifact = (Artifact) resolutionResult.getArtifacts()
                            .iterator().next();
                    if (!artifact.getType().equalsIgnoreCase("pom")) {
                        artifactsToIndex.add(artifact);
                    }
                }
            }
        }
        return artifactsToIndex;
    }

    @Override
    protected Path calculateIndexFilePath() throws MojoExecutionException {
        Path repositoryXmlURL;
        if (getIndexRepositoryPath() != null) {
            repositoryXmlURL = new File(getIndexRepositoryPath()).toPath();
        } else {
            if (isCompressed() == true) {
                repositoryXmlURL = new File(localRepository.getBasedir(),
                        REPO_XML_GZ).toPath();
            } else {
                repositoryXmlURL = new File(localRepository.getBasedir(),
                        REPO_XML).toPath();
            }
        }
        return repositoryXmlURL;
    }

    @Override
    protected Path calculateRootDirPath(String rootUrlStr)
            throws MojoExecutionException {
        Path rootDirPath = null;
        try {
            if (rootUrlStr != null) {
                File rootDirFile = new File(rootUrlStr);
                if (rootDirFile.isDirectory())
                    rootDirPath = rootDirFile.toPath();
                else
                    rootDirPath = rootDirFile.getParentFile().toPath();
            } else {
                if (localRepository != null)
                    rootDirPath = new File(localRepository.getUrl()).toPath();
            }
            return rootDirPath;
        } catch (Exception e) {
            throw new MojoExecutionException("", e);
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!isThisTheLatestChildModule()) {
            getLog().info(
                    "Skipping the repository indexer in this project because it's not the Latest Project in the Reactor.");
            return;
        }
        super.execute();
    }

    @Override
    protected int executeRepositoryIndexer(BindexWrapper bindexWrapper,
            Path indexRepositoryPath, List<PathMatcher> includes,
            List<PathMatcher> excludes, Map<String, String> bindexConfig)
            throws MojoExecutionException {

        Set<Artifact> artifactsToIndex = calculateFileSetToIndex(includes, excludes);

        if (!artifactsToIndex.isEmpty()) {
            return bindexWrapper.updateIndexRepositoryForArtifactSet(
                    artifactsToIndex, indexRepositoryPath, bindexConfig);
        } else {
            getLog().info("There are nothing index...");
            return 0;
        }
    }

    /**
     * Returns true if the current project is located at the Execution Root
     * Directory (where mvn was launched)
     * 
     * @return
     */
    protected boolean isThisTheExecutionRoot() {
        Log log = this.getLog();
        log.debug("Root Folder:" + getSession().getExecutionRootDirectory());
        log.debug("Current Folder:" + basedir);
        boolean result = getSession().getExecutionRootDirectory()
                .equalsIgnoreCase(basedir.toString());
        if (result) {
            log.debug("This is the execution root.");
        } else {
            log.debug("This is NOT the execution root.");
        }
        return result;
    }

    /**
     * Returns true if the current project is the latest one to be running in
     * the maven reactor.
     * 
     * @return
     */
    protected boolean isThisTheLatestChildModule() {
        Log log = this.getLog();
        final int size = reactorProjects.size();
        MavenProject latestProject = (MavenProject) reactorProjects
                .get(size - 1);

        log.debug("Current Project:" + basedir);
        log.debug("Latest Module:" + latestProject);
        boolean result = latestProject == getProject();
        if (result) {
            log.debug("This is the latest module running in the maven reactor.");
        } else {
            log.debug("This is NOT the latest module running in the maven reactor.");
        }
        return result;
    }

    @Override
    protected String shortDescription() {
        return "OSGi index repository generator for Maven repository";
    }

}
