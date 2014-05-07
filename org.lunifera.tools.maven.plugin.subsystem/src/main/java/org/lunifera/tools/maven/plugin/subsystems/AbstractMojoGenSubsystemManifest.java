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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.aries.subsystem.core.archive.DeploymentManifest;
import org.apache.aries.subsystem.core.archive.ExportPackageHeader;
import org.apache.aries.subsystem.core.archive.GenericHeader;
import org.apache.aries.subsystem.core.archive.Header;
import org.apache.aries.subsystem.core.archive.ImportPackageHeader;
import org.apache.aries.subsystem.core.archive.PreferredProviderHeader;
import org.apache.aries.subsystem.core.archive.ProvideCapabilityHeader;
import org.apache.aries.subsystem.core.archive.RequireBundleHeader;
import org.apache.aries.subsystem.core.archive.RequireCapabilityHeader;
import org.apache.aries.subsystem.core.archive.SubsystemContentHeader;
import org.apache.aries.subsystem.core.archive.SubsystemContentHeader.Clause;
import org.apache.aries.subsystem.core.archive.SubsystemExportServiceHeader;
import org.apache.aries.subsystem.core.archive.SubsystemImportServiceHeader;
import org.apache.aries.subsystem.core.archive.SubsystemManifest;
import org.apache.aries.subsystem.core.archive.SubsystemManifestVersionHeader;
import org.apache.aries.subsystem.core.archive.SubsystemSymbolicNameHeader;
import org.apache.aries.subsystem.core.archive.SubsystemTypeHeader;
import org.apache.aries.subsystem.core.archive.SubsystemVersionHeader;
import org.apache.aries.util.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.lunifera.tools.maven.plugin.subsystems.utils.DefaultMaven2OsgiConverter;

public abstract class AbstractMojoGenSubsystemManifest extends AbstractMojo {

    protected static final String SUBSYSTEM_MANIFEST_FILE = "${project.build.outputDirectory}/OSGI-INF/SUBSYSTEM.MF";

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     */
    @Component
    private RepositorySystem repositorySystem;

    // @Component(role =
    // org.apache.maven.artifact.handler.ArtifactHandler.class,
    // hint = "bundle")
    // private ArtifactHandler bundleArtifactHandler;
    // @Component(role =
    // org.apache.maven.artifact.handler.ArtifactHandler.class,
    // hint = "subsystem-feature")
    // private ArtifactHandler featureArtifactHandler;
    //
    // @Component(role =
    // org.apache.maven.artifact.handler.ArtifactHandler.class,
    // hint = "subsystem-composite")
    // private ArtifactHandler compositeArtifactHandler;
    //
    // @Component(role =
    // org.apache.maven.artifact.handler.ArtifactHandler.class,
    // hint = "subsystem-application")
    // private ArtifactHandler applicationArtifactHandler;

    /**
     * The current repository/network configuration of Maven.
     * 
     * @since 0.0.1
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    /**
     * Aries subsystem deployment manifest generator implementation.
     */
    protected DeploymentManifest deploymentManifestComponent;

    /**
     * This indicates that declared dependencies in the POM will be used to
     * populate the subsystem's contents in the manifest file.
     */
    @Parameter(defaultValue = "true")
    protected boolean deriveContentsFromDependencies;

    @Parameter(required = true, property = "localRepository", readonly = true)
    protected ArtifactRepository localArtifactRepository;

    private DefaultMaven2OsgiConverter maven2OsgiConverter = new DefaultMaven2OsgiConverter();

    /**
     * The project's remote repositories to use for the resolution of plugins
     * and their dependencies.
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}",
            property = "remoteRepositories", readonly = true)
    protected List<ArtifactRepository> remoteRepositories;

    /**
     * Subsystem manifest generation instructions.
     */
    @Parameter(alias = "Subsystem-Manifest")
    protected PojoSubsystemManifestConfiguration subsystemManifest;

    /**
     * Aries subsystem manifest generator implementation.
     */
    protected SubsystemManifest subsystemManifestComponent;

    /**
     * The path to SUBSYSTEM.MF file that will be generated and used by the
     * pack-esa mojo.
     * <p>
     * This value changes according to ${project.build.outputDirectory}.
     */
    @Parameter(
            defaultValue = "${project.build.outputDirectory}/OSGI-INF/SUBSYSTEM.MF",
            readonly = true)
    protected File subsystemManifestFile;

    public AbstractMojoGenSubsystemManifest() {
    }

    protected SubsystemManifest buildSubsystemManifest()
            throws MojoExecutionException {
        if (subsystemManifest == null)
            return null;

        org.apache.aries.subsystem.core.archive.SubsystemManifest.Builder subsystemManifestBuilder = new SubsystemManifest.Builder();

        subsystemManifestBuilder.header(new SubsystemManifestVersionHeader());

        String importPackage = subsystemManifest.getImportPackage();
        if (importPackage != null && !importPackage.isEmpty())
            subsystemManifestBuilder.header(new ImportPackageHeader(
                    importPackage));

        String exportPackage = subsystemManifest.getExportPackage();
        if (exportPackage != null && !exportPackage.isEmpty())
            subsystemManifestBuilder.header(new ExportPackageHeader(
                    exportPackage));

        String preferredProvider = subsystemManifest.getPreferredProvider();
        if (preferredProvider != null && !preferredProvider.isEmpty())
            subsystemManifestBuilder.header(new PreferredProviderHeader(
                    preferredProvider));

        String provideCapability = subsystemManifest.getProvideCapability();
        if (provideCapability != null && !provideCapability.isEmpty())
            subsystemManifestBuilder.header(new ProvideCapabilityHeader(
                    provideCapability));

        String requireBundle = subsystemManifest.getRequireBundle();
        if (requireBundle != null && !requireBundle.isEmpty())
            subsystemManifestBuilder.header(new RequireBundleHeader(
                    requireBundle));

        String requireCapability = subsystemManifest.getRequireCapability();
        if (requireCapability != null && !requireCapability.isEmpty())
            subsystemManifestBuilder.header(new RequireCapabilityHeader(
                    requireCapability));

        String subsystemType = getSubsystemType().getValue();
        subsystemManifestBuilder.header(new SubsystemTypeHeader(subsystemType));

        String subsystemExportService = subsystemManifest
                .getSubsystemExportService();
        if (subsystemExportService != null && !subsystemExportService.isEmpty())
            subsystemManifestBuilder.header(new SubsystemExportServiceHeader(
                    subsystemExportService));

        String subsystemImportService = subsystemManifest
                .getSubsystemImportService();
        if (subsystemImportService != null && !subsystemImportService.isEmpty())
            subsystemManifestBuilder.header(new SubsystemImportServiceHeader(
                    subsystemImportService));

        String subsystemDescription = subsystemManifest
                .getSubsystemDescription();
        if (subsystemDescription != null && !subsystemDescription.isEmpty())
            subsystemManifestBuilder.header(new GenericHeader(
                    "Subsystem-Description", subsystemDescription));

        String version = subsystemManifest.getSubsystemVersion();
        if (version != null && !version.isEmpty()) {
            subsystemManifestBuilder.version(version);
        } else {
            subsystemManifestBuilder.header(calculateVersionFromProject());
        }

        String symbolicName = subsystemManifest.getSubsystemSymbolicName();
        if (symbolicName != null && !symbolicName.isEmpty()) {
            subsystemManifestBuilder.header(new SubsystemSymbolicNameHeader(
                    symbolicName));
        } else {
            subsystemManifestBuilder.header(calculateSymbolicNameFromProject());
        }

        String subsystemContent = subsystemManifest.getSubsystemContent();
        if (deriveContentsFromDependencies) {
            Header<?> contentHeader = calculateContentFromProjectDependencies(subsystemContent);
            subsystemManifestBuilder.header(contentHeader);
        } else {
            if (subsystemContent != null && !subsystemContent.isEmpty()) {
                subsystemManifestBuilder.header(new SubsystemContentHeader(
                        subsystemContent));
            }
        }
        return subsystemManifestBuilder.build();
    }

    /**
     * This method will filter the dependencies defined in the project pom and
     * select those that has scope Runtime or Compile and types
     * Subsystem-Application, Subsystem-Feature, Subsystem-Composite or Bundle.
     * By default all content will be mandatory unless the artifact is set as
     * optional.
     * 
     * resolution – (mandatory| optional)
     * 
     * @param subsystemContentHeader
     * 
     * @return the Subsystem-Content header
     * @throws MojoExecutionException
     */
    private Header<?> calculateContentFromProjectDependencies(
            String subsystemContentConfig) throws MojoExecutionException {
        SubsystemContentHeader subsystemContentHeader = null;
        Set<Artifact> artifacts = getProject().getDependencyArtifacts();
        Set<Clause> clauses = new HashSet<Clause>();

        Map<String, MutableClause> clausesMap = extractClausesFromString(subsystemContentConfig);

        for (Artifact artifact : artifacts) {

            getLog().debug("Resolving dependency artifact " + artifact);
            ArtifactResolutionRequest request = new ArtifactResolutionRequest()
                    .setArtifact(artifact)
                    .setRemoteRepositories(remoteRepositories)
                    .setLocalRepository(localArtifactRepository);

            ArtifactResolutionResult resolutionResult = repositorySystem
                    .resolve(request);
            if (resolutionResult.hasExceptions()) {
                throw new MojoExecutionException("Could not resolve artifact: "
                        + artifact, resolutionResult.getExceptions().get(0));
            }
            artifact = (Artifact) resolutionResult.getArtifacts().iterator()
                    .next();

            String type = artifact.getType();
            String scope = artifact.getScope();
            if (scope.equalsIgnoreCase("Runtime")
                    || scope.equalsIgnoreCase("Compile")) {
                MutableClause clause = clausesMap.get(artifact.getArtifactId());
                if (clause == null) {
                    clause = new MutableClause(artifact.getArtifactId());
                }

                if (type.equalsIgnoreCase("jar")
                        || type.equalsIgnoreCase("bundle")) {
                    clause.setAttribute("type", "osgi.bundle");
                } else
                    if (type.equalsIgnoreCase("subsystem-feature")) {
                        clause.setAttribute("type", "osgi.subsystem.feature");

                    } else
                        if (type.equalsIgnoreCase("subsystem-composite")) {
                            clause.setAttribute("type",
                                    "osgi.subsystem.composite");
                        } else
                            if (type.equalsIgnoreCase("subsystem-application")) {
                                clause.setAttribute("type",
                                        "osgi.subsystem.application");
                            }

                if (artifact.isOptional()) {
                    clause.setDirective(Clause.DIRECTIVE_RESOLUTION, "optional");
                }
                clause.setAttribute(Clause.ATTRIBUTE_VERSION,
                        maven2OsgiConverter.getVersion(artifact.getVersion()));

                clauses.add(clause);
            }
        }
        if (!clauses.isEmpty()) {
            subsystemContentHeader = new SubsystemContentHeader(clauses);
        }
        return subsystemContentHeader;
    }

    private Header<?> calculateSymbolicNameFromProject() {
        SubsystemSymbolicNameHeader symbolicNameHeader = null;
        symbolicNameHeader = new SubsystemSymbolicNameHeader(getProject()
                .getArtifactId());
        return symbolicNameHeader;
    }

    private Header<?> calculateVersionFromProject() {
        SubsystemVersionHeader versionHeader = null;
        versionHeader = new SubsystemVersionHeader(
                maven2OsgiConverter.getVersion(getProject().getVersion()));
        return versionHeader;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();

        File source = getProject().getFile();
        if (!getBuildContext().hasDelta(source)) {
            return;
        }

        SubsystemManifest subsystemManifest = buildSubsystemManifest();

        if (subsystemManifest != null) {
            writeSubsystemManifestFile(subsystemManifest);
        } else {
            throw new MojoExecutionException(
                    "Error occurred when building manifest file.");
        }
    }

    private Map<String, MutableClause> extractClausesFromString(
            String subsystemContentConfig) {
        Map<String, MutableClause> map = new HashMap<>();

        if (subsystemContentConfig != null && !subsystemContentConfig.isEmpty()) {
            String[] clauses = subsystemContentConfig.split(",");
            for (int i = 0; i < clauses.length; i++) {
                MutableClause clause = new MutableClause(clauses[i]);
                if (clause != null) {
                    String id = clause.getSymbolicName();
                    map.put(id, clause);
                }
            }
        }
        return map;
    }

    protected abstract SubsystemType getSubsystemType();

    @Override
    protected String shortDescription() {

        return "subsystem manifest generation";
    }

    synchronized void writeSubsystemManifestFile(
            SubsystemManifest subsystemManifest) throws MojoExecutionException,
            MojoFailureException {
        File file = subsystemManifestFile;
        OutputStream fos = null;
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // TODO: should always override the file ?
            if (file.exists()) {
                file.delete();
            }
            fos = getBuildContext().newFileOutputStream(file);
            subsystemManifest.write(fos);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("The file '"
                    + subsystemManifestFile + "' was not found.", e);
        } catch (IOException e) {
            throw new MojoFailureException(
                    "A failure occurred while trying to open the file '"
                            + subsystemManifestFile + "'.", e);
        } finally {
            if (fos != null)
                IOUtils.close(fos);
        }
    }
}
