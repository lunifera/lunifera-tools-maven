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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "generateIndex", threadSafe = true, requiresProject = false,
        defaultPhase = LifecyclePhase.VERIFY,
        requiresDependencyResolution = ResolutionScope.NONE)
public class MojoGenerateIndex extends AbstractMojo {

    protected Set<File> calculateFileSetToIndex(
            List<PathMatcher> includeMatchers, List<PathMatcher> excludeMatchers)
            throws MojoExecutionException {
        Set<File> filesToIndex = new HashSet<>();
        FilesToIndexVisitor filesToIndexVisitor = new FilesToIndexVisitor(
                includeMatchers, excludeMatchers, filesToIndex, getLog());
        try {
            Files.walkFileTree(getCalculatedRootDirPath(), filesToIndexVisitor);
        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Error searching for files to index.", e);
        }
        return filesToIndex;
    }

    @Override
    protected Path calculateIndexFilePath() throws MojoExecutionException {
        Path repositoryXmlURL;
        if (getIndexRepositoryPath() != null) {
            repositoryXmlURL = new File(getIndexRepositoryPath()).toPath();

        } else {
            if (isCompressed() == true) {
                repositoryXmlURL = new File(
                        getCalculatedRootDirPath().toFile(), REPO_XML_GZ)
                        .toPath();
            } else {
                repositoryXmlURL = new File(
                        getCalculatedRootDirPath().toFile(), REPO_XML).toPath();
            }
        }
        return repositoryXmlURL;
    }

    @Override
    protected Path calculateRootDirPath(String rootDirPathStr)
            throws MojoExecutionException {
        Path rootURL = null;
        if (rootDirPathStr != null) {
            try {
                File rootDirFile = new File(rootDirPathStr);
                if (rootDirFile.isDirectory())
                    rootURL = rootDirFile.toPath();
                else {
                    throw new MojoExecutionException(
                            "rootDir property must point to a directory !");
                }
            } catch (Exception e) {
                getLog().error(e);
                throw new MojoExecutionException(
                        "Root Directory is not valid !");
            }
        } else {
            throw new MojoExecutionException(
                    "Root Directory must be informed !");
        }
        return rootURL;
    }

    @Override
    protected int executeRepositoryIndexer(BindexWrapper bindexWrapper,
            Path indexRepositoryPath, List<PathMatcher> includeMatchers,
            List<PathMatcher> excludeMatchers, Map<String, String> bindexConfig)
            throws MojoExecutionException {
        Set<File> filesToIndex = calculateFileSetToIndex(includeMatchers,
                excludeMatchers);
        return bindexWrapper.generateRepositoryIndex(filesToIndex,
                indexRepositoryPath, getBindexConfig());
    }

    @Override
    protected String shortDescription() {
        return "Generic OSGi Repository Index generator";
    }

}
