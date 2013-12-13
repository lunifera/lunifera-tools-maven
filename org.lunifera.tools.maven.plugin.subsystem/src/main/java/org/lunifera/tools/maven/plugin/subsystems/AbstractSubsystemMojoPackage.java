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
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.jar.JarArchiver;

/**
 * Build a ESA file from the current project.
 * 
 * @author <a href="cvgaviao@gmail.com">Cristiano Gavião</a>
 */

@Mojo(name = "pack", defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.TEST)
public class AbstractSubsystemMojoPackage extends AbstractSubsystemMojo {

    private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html" };
    private static final String[] DEFAULT_INCLUDES = new String[] { "**/**" };
    private static final String SUBSYSTEM_EXTENSION = "esa";

    /**
     * The archive configuration to use. See <a
     * href="http://maven.apache.org/shared/maven-archiver/index.html">Maven
     * Archiver Reference</a>.
     */
    private final MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Location of the generated ESA file.
     */
    @Parameter(defaultValue = "${project.build.directory}",
            property = "buildDirectory", required = true)
    private File buildDirectory;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will
     * be attached. If this is not given,it will merely be written to the output
     * directory according to the finalName.
     */
    @Parameter(property = "classifier")
    private String classifier;

    /**
     * Path to the default MANIFEST file to use. It will be used if
     * <code>useDefaultManifestFile</code> is set to <code>true</code>.
     */
    @Parameter(
            defaultValue = "${project.build.outputDirectory}/META-INF/MANIFEST.MF",
            readonly = true, required = true)
    private File defaultManifestFile;

    /**
     * List of files to exclude. Specified as fileset patterns which are
     * relative to the input directory whose contents is being packaged into the
     * ESA.
     * 
     * @parameter
     */
    private String[] excludes;

    /**
     * The name of the generated ESA.
     */
    @Parameter(defaultValue = "${project.build.finalName}",
            property = "finalName", required = true)
    private String finalName;

    /**
     * Whether creating the archive should be forced.
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
     * The Jar archiver.
     */
    @Component(role = org.codehaus.plexus.archiver.Archiver.class, hint = "jar")
    private JarArchiver jarArchiver;

    /**
     * Skip creating empty archives.
     */
    @Parameter(defaultValue = "false", property = "skipIfEmpty",
            required = true)
    private boolean skipIfEmpty;

    /**
     * Set this to <code>true</code> to enable the use of the
     * <code>defaultManifestFile</code>.
     */
    @Parameter(defaultValue = "false", property = "useDefaultManifestFile",
            readonly = true, required = true)
    private boolean useDefaultManifestFile;

    private long calculateCrc(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    /**
     * Generates the Subsystem archive file.
     */
    public File createArchive() throws MojoExecutionException {
        File esaFile = getTargetFile(getBuildDirectory(), getFinalName(),
                getClassifier());

        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver(getJarArchiver());

        archiver.setOutputFile(esaFile);

        getArchive().setForced(isForceCreation());

        getArchive().addManifestEntry("InitialProvisioning-Entries",
                SUBSYSTEM_EXTENSION);

        try {
            File contentDirectory = getOutputDirectory();
            if (!contentDirectory.exists()) {
                getLog().warn(
                        "ESA will be empty - no content was marked for inclusion!");
            } else {
                archiver.getArchiver().addDirectory(contentDirectory,
                        getIncludes(), getExcludes());
            }

            File existingManifest = getDefaultManifestFile();

            if (isUseDefaultManifestFile() && existingManifest.exists()
                    && getArchive().getManifestFile() == null) {
                getLog().info(
                        "Adding existing MANIFEST to archive. Found under: "
                                + existingManifest.getPath());
                getArchive().setManifestFile(existingManifest);
            }

            archiver.createArchive(getSession(), getProject(), getArchive());

            return esaFile;
        } catch (Exception e) {
            throw new MojoExecutionException("Error assembling ESA", e);
        }
    }

    /**
     * Generated the ESA file for the current project.
     * 
     * @throws MojoExecutionException
     *             if an error occurred while building the ESA file
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        super.execute();

        if (skipIfEmpty && !getOutputDirectory().exists()) {
            getLog().info("Skipping generation of the ESA file.");
        } else {
            File esaFile = createArchive();
            getBuildContext().refresh(esaFile);
            String classifier = getClassifier();
            if (classifier != null) {
                getProjectHelper().attachArtifact(getProject(), getType(),
                        classifier, esaFile);
            } else {
                getProject().getArtifact().setFile(esaFile);
            }
        }

    }

    protected MavenArchiveConfiguration getArchive() {
        return archive;
    }

    protected File getBuildDirectory() {
        return buildDirectory;
    }

    protected String getClassifier() {
        return classifier;
    }

    /**
     * Default Manifest location. Can point to a non existing file. Cannot
     * return null.
     */
    protected File getDefaultManifestFile() {
        return defaultManifestFile;
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

    protected JarArchiver getJarArchiver() {
        return jarArchiver;
    }

    protected File getTargetFile(File basedir, String finalName,
            String classifier) {
        if (classifier == null) {
            classifier = "";
        } else
            if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
                classifier = "-" + classifier;
            }

        return new File(basedir, finalName + classifier + "."
                + SUBSYSTEM_EXTENSION);
    }

    protected String getType() {
        return "esa";
    }

    protected boolean isForceCreation() {
        return forceCreation;
    }

    protected boolean isSkipIfEmpty() {
        return skipIfEmpty;
    }

    protected boolean isUseDefaultManifestFile() {
        return useDefaultManifestFile;
    }

    /**
     * Stores the mimetype as an uncompressed file in the ZipOutputStream.
     * 
     * @param resultStream
     * @throws IOException
     */
    private void writeMimeType(ZipOutputStream resultStream) throws IOException {
        byte[] mimetypeBytes = "application/vnd.osgi.subsystem"
                .getBytes("UTF-8");
        ZipEntry mimetypeZipEntry = new ZipEntry("mimetype");
        mimetypeZipEntry.setMethod(ZipEntry.STORED);
        mimetypeZipEntry.setSize(mimetypeBytes.length);
        mimetypeZipEntry.setCrc(calculateCrc(mimetypeBytes));
        resultStream.putNextEntry(mimetypeZipEntry);
        resultStream.write(mimetypeBytes);
    }
}
