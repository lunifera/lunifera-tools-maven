/*******************************************************************************
 * Copyright (c) 2013, 2014 C4biz Softwares ME, Loetz KG.
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Build a ESA file from the current project.
 * 
 * @author <a href="cvgaviao@gmail.com">Cristiano Gavião</a>
 */

@Mojo(name = "pack-esa", defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MojoPackSubsystemEsa extends AbstractMojo {

    private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html" };
    private static final String[] DEFAULT_INCLUDES = new String[] { "**/**" };

    /**
     * Will allow generation of Initial Provisioning meta-data.
     */
    @Parameter(property = "deliverableByIP", defaultValue = "false")
    private boolean deliverableByIP;

    /**
     * Location where the ESA artifact file will be generated.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true,
            readonly = true)
    private File buildDirectory;

    /**
     * A custom classifier to be added to the generated artifact file name.
     */
    @Parameter(property = "classifier")
    private String classifier;

    /**
     * The Jar archiver.
     */
    @Component(role = org.codehaus.plexus.archiver.Archiver.class, hint = "esa")
    private SubsystemArchiverComponent esaArchiver;
    /**
     * List of files to exclude. Specified as fileset patterns which are
     * relative to the input directory whose contents is being packaged into the
     * ESA.
     * 
     * @parameter
     */
    private String[] excludes;

    /**
     * The final name of the generated ESA artifact file.
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true,
            readonly = true)
    private String finalName;

    /**
     * Whether creating the archiveConfiguration should be forced.
     */
    @Parameter(defaultValue = "false", property = "forceCreation",
            required = true)
    private boolean forceCreation;

    /**
     * List of files to include. Specified as fileset patterns which are
     * relative to the input directory whose contents is being packaged into the
     * ESA.
     * 
     * @parameter
     */
    private String[] includes;

    /**
     * Skip creating empty archives.
     */
    @Parameter(defaultValue = "false", property = "skipIfEmpty",
            required = true)
    private boolean skipIfEmpty;

    /**
     * Generates the Subsystem archiveConfiguration file.
     */
    public File createArchive() throws MojoExecutionException {

        try {
            File outputDirectory = getOutputDirectory();
            if (!outputDirectory.exists()) {
                if (skipIfEmpty) {
                    throw new MojoExecutionException(
                            "An empty ESA file is forbidden.");
                } else {
                    getLog().warn(
                            "ESA will be empty - no content was marked for inclusion!");
                }
            } else {
                esaArchiver.addDirectory(outputDirectory, getIncludes(),
                        getExcludes());
            }

            File esaFile = getTargetFile(getBuildDirectory(), getFinalName(),
                    getClassifier());
            esaArchiver.setDestFile(esaFile);
            esaArchiver.createArchive();

            return esaFile;
        } catch (Exception e) {
            throw new MojoExecutionException("Error assembling ESA file.", e);
        }
    }

    /**
     * Generate the ESA file for the current project.
     * 
     * @throws MojoExecutionException
     *             if an error occurred while building the ESA file
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        super.execute();

        if (skipIfEmpty || !getOutputDirectory().exists()) {
            getLog().info("Skipping generation of the ESA file.");
        } else {
            File esaFile = createArchive();
            getBuildContext().refresh(esaFile);
            String classifier = getClassifier();
            if (classifier != null) {
                getProjectHelper().attachArtifact(getProject(),
                        SubsystemArchiverComponent.SUBSYSTEM_EXTENSION,
                        classifier, esaFile);
            } else {
                getProject().getArtifact().setFile(esaFile);
            }
        }

    }

    protected File getBuildDirectory() {
        return buildDirectory;
    }

    protected String getClassifier() {
        return classifier;
    }

    private String[] getExcludes() {
        if (excludes != null && excludes.length > 0) {
            return excludes;
        }
        return DEFAULT_EXCLUDES;
    }

    protected String getFinalName() {
        return finalName;
    }

    private String[] getIncludes() {
        if (includes != null && includes.length > 0) {
            return includes;
        }
        return DEFAULT_INCLUDES;
    }

    protected File getTargetFile(File basedir, String finalName,
            String classifier) {

        // TODO When Deployment spec were out we need to have a classifier for
        // deployment archives that contain only DEPLOYMENT.MF name than the
        // Subsytem archive.
        if (classifier == null) {
            classifier = "";
        } else
            if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
                classifier = "-" + classifier;
            }

        return new File(basedir, finalName + classifier + "."
                + SubsystemArchiverComponent.SUBSYSTEM_EXTENSION);
    }

    protected boolean isForceCreation() {
        return forceCreation;
    }

    protected boolean isSkipIfEmpty() {
        return skipIfEmpty;
    }

    @Override
    protected String shortDescription() {
        
        return "subsystem archive generation";
    }

}
