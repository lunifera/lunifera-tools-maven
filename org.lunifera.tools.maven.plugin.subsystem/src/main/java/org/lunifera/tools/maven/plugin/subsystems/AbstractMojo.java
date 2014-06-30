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

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * 
 * @author cvgaviao
 *
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    @Component
    private BuildContext buildContext;

    /**
     * Directory containing the classes and resource files that should be
     * packaged into the ESA.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}",
            property = "outputDirectory", required = true)
    private File outputDirectory;

    /**
     * 
     */
    @Component
    // for Maven 3 only
    private PluginDescriptor pluginDescriptor;

    /**
     * The Maven project.
     */
    @Component
    private MavenProject project;

    /**
     * The Maven project helper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The runtime information for Maven, used to retrieve Maven's version
     * number.
     *
     * @component
     */
    @Component
    private RuntimeInformation runtimeInformation;

    /**
     * The Maven session".
     * <p>
     * Required in order to create the jar artifact.
     */
    @Component
    private MavenSession session;

    /**
     * Set this to <code>true</code> to skip the generation of the Subsystem
     * Manifest file.
     */
    @Parameter(defaultValue = "false", property = "skip", required = true)
    private boolean skip;

    public AbstractMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!runtimeInformation.isMavenVersion("[3.1,)")) {
            throw new UnsupportedOperationException(
                    "Lunifera requires Maven 3.1 or higher.");
        }
        if (!getProject().getArtifact().getType().startsWith("subsystem")) {
            getLog().warn("Ignoring project " + getProject().getId());
        }

        if (skip) {
            getLog().info(
                    "Skiping " + shortDescription() + " for project "
                            + getProject().getId());
            return;
        }

    }

    protected BuildContext getBuildContext() {
        return buildContext;
    }

    protected File getOutputDirectory() {
        return outputDirectory;
    }

    protected PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }

    protected MavenProject getProject() {
        return project;
    }

    protected MavenProjectHelper getProjectHelper() {
        return projectHelper;
    }

    protected MavenSession getSession() {
        return session;
    }

    protected abstract String shortDescription();

}
