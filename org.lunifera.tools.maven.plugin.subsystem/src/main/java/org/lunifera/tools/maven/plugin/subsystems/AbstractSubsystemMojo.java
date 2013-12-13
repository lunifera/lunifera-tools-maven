/*******************************************************************************
 * Copyright (c) 2013 C4biz Softwares ME, Loetz KG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Cristiano Gavião - initial API and implementation
 *******************************************************************************/
package org.lunifera.tools.maven.plugin.subsystems;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.plexus.build.incremental.BuildContext;

public abstract class AbstractSubsystemMojo extends AbstractMojo {

    @Component
    private BuildContext buildContext;

    @Parameter(property = "deliverableByIP", defaultValue = "false")
    private boolean deliverableByIP;

    /**
     * Directory containing the classes and resource files that should be
     * packaged into the ESA.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}",
            property = "outputDirectory", required = true)
    private File outputDirectory;

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
     * The Maven session"
     */
    @Component
    private MavenSession session;

    public AbstractSubsystemMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!getProject().getArtifact().getType().equals("subsystem")) {
            getLog().warn("Ignoring project " + getProject().getId());
        }
    }

    protected BuildContext getBuildContext() {
        return buildContext;
    }

    protected File getOutputDirectory() {
        return outputDirectory;
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

    public boolean isDeliverableByIP() {
        return deliverableByIP;
    }

}
