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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.aries.subsystem.core.archive.DeployedContentHeader;
import org.apache.aries.subsystem.core.archive.DeploymentManifest;
import org.apache.aries.subsystem.core.archive.DeploymentManifest.Builder;
import org.apache.aries.subsystem.core.archive.ExportPackageHeader;
import org.apache.aries.subsystem.core.archive.GenericHeader;
import org.apache.aries.subsystem.core.archive.Header;
import org.apache.aries.subsystem.core.archive.ImportPackageHeader;
import org.apache.aries.subsystem.core.archive.PreferredProviderHeader;
import org.apache.aries.subsystem.core.archive.ProvideCapabilityHeader;
import org.apache.aries.subsystem.core.archive.ProvisionResourceHeader;
import org.apache.aries.subsystem.core.archive.RequireBundleHeader;
import org.apache.aries.subsystem.core.archive.RequireCapabilityHeader;
import org.apache.aries.subsystem.core.archive.SubsystemContentHeader;
import org.apache.aries.subsystem.core.archive.SubsystemContentHeader.Clause;
import org.apache.aries.subsystem.core.archive.SubsystemExportServiceHeader;
import org.apache.aries.subsystem.core.archive.SubsystemImportServiceHeader;
import org.apache.aries.subsystem.core.archive.SubsystemManifest;
import org.apache.aries.subsystem.core.archive.SubsystemSymbolicNameHeader;
import org.apache.aries.subsystem.core.archive.SubsystemTypeHeader;
import org.apache.aries.subsystem.core.archive.SubsystemVersionHeader;
import org.apache.aries.util.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.osgi.DefaultMaven2OsgiConverter;
import org.apache.maven.shared.osgi.Maven2OsgiConverter;

public abstract class AbstractSubsystemMojoManifest extends AbstractSubsystemMojo {

	protected static final String DEPLOYMENT_MANIFEST_NAME = "DEPLOYMENT.MF";
	protected static final String SUBSYSTEM_MANIFEST_NAME = "SUBSYSTEM.MF";

	/**
	 * Subsystem manifest generation instructions.
	 */
	@Parameter(alias = "Subsystem-Manifest")
	private SubsystemManifestConfiguration subsystemManifestConfiguration;

	/**
	 * Deployment manifest generation instructions.
	 */
	@Parameter(alias = "Deployment-Manifest")
	private DeploymentManifestConfiguration deploymentManifestConfiguration;

	/**
	 * Directory where the subsystem and deployment manifest files should be
	 * written.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}/OSGI-INF/", required = true, readonly = true)
	private File manifestGenerationLocation;

	/**
	 * Set this to <code>true</code> to enable the generation of the Subsystem
	 * Deployment Manifest file.
	 */
	@Parameter(defaultValue = "false", property = "generateDeploymentManifestFile", required = true)
	private boolean generateDeploymentManifestFile;

	/**
	 * Set this to <code>true</code> to disable the generation of Subsystem
	 * Manifest File and enable the use of the file defined by
	 * <code>providedSubsystemManifestFile</code>.
	 */
	@Parameter(defaultValue = "false", property = "useProvidedSubsystemManifestFile", required = true)
	private boolean useProvidedSubsystemManifestFile;

	/**
	 * The path to an existent SUBSYSTEM.MF file to use. It will be used if
	 * <code>useProvidedSubsystemManifestFile</code> was set to
	 * <code>true</code>.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}/OSGI-INF/SUBSYSTEM.MF")
	private File providedSubsystemManifestFile;

	/**
	 * Set this to <code>true</code> to disable the generation of Deployment
	 * Manifest File and enable the use of the file defined by
	 * <code>providedDeploymentManifestFile</code>.
	 */
	@Parameter(defaultValue = "false", property = "useProvidedDeploymentManifestFile", required = true)
	private boolean useProvidedDeploymentManifestFile;

	/**
	 * The path to an existent DEPLOYMENT.MF file to use. It will be used if
	 * <code>useProvidedDeploymentManifestFile</code> was set to
	 * <code>true</code> .
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}/OSGI-INF/DEPLOYMENT.MF", readonly = true)
	private File providedDeploymentManifestFile;

	private Maven2OsgiConverter maven2OsgiConverter = new DefaultMaven2OsgiConverter();

	public AbstractSubsystemMojoManifest() {
	}

	protected DeploymentManifest buildDeploymentManifest(
			SubsystemManifest subsystemManifest) {
		if (getDeploymentManifestConfiguration() == null)
			return null;

		Builder deploymentManifestBuilder = new DeploymentManifest.Builder();

		String deployedContent = getDeploymentManifestConfiguration()
				.getDeployedContent();
		if (deployedContent != null && !deployedContent.isEmpty())
			deploymentManifestBuilder.header(new DeployedContentHeader(
					deployedContent));

		String provisionResource = getDeploymentManifestConfiguration()
				.getDeployedContent();
		if (provisionResource != null && !provisionResource.isEmpty())
			deploymentManifestBuilder.header(new ProvisionResourceHeader(
					provisionResource));

		String importPackage = getDeploymentManifestConfiguration()
				.getImportPackage();
		if (importPackage != null && !importPackage.isEmpty())
			deploymentManifestBuilder.header(new ImportPackageHeader(
					importPackage));

		String exportPackage = getDeploymentManifestConfiguration()
				.getExportPackage();
		if (exportPackage != null && !exportPackage.isEmpty())
			deploymentManifestBuilder.header(new ExportPackageHeader(
					exportPackage));

		String requireBundle = getDeploymentManifestConfiguration()
				.getRequireBundle();
		if (requireBundle != null && !requireBundle.isEmpty())
			deploymentManifestBuilder.header(new RequireBundleHeader(
					requireBundle));

		String provideCapability = getDeploymentManifestConfiguration()
				.getProvideCapability();
		if (provideCapability != null && !provideCapability.isEmpty())
			deploymentManifestBuilder.header(new ProvideCapabilityHeader(
					provideCapability));

		String requireCapability = getDeploymentManifestConfiguration()
				.getRequireCapability();
		if (requireCapability != null && !requireCapability.isEmpty())
			deploymentManifestBuilder.header(new RequireCapabilityHeader(
					requireCapability));

		String subsystemExportService = getDeploymentManifestConfiguration()
				.getSubsystemExportService();
		if (subsystemExportService != null && !subsystemExportService.isEmpty())
			deploymentManifestBuilder.header(new SubsystemExportServiceHeader(
					subsystemExportService));

		String subsystemImportService = getDeploymentManifestConfiguration()
				.getSubsystemImportService();
		if (subsystemImportService != null && !subsystemImportService.isEmpty())
			deploymentManifestBuilder.header(new SubsystemImportServiceHeader(
					subsystemImportService));

		deploymentManifestBuilder.manifest(subsystemManifest);
		return deploymentManifestBuilder.build();
	}

	protected SubsystemManifest buildSubsystemManifest() {
		if (getSubsystemManifestConfiguration() == null)
			return null;

		org.apache.aries.subsystem.core.archive.SubsystemManifest.Builder subsystemManifestBuilder = new SubsystemManifest.Builder();
		String importPackage = getSubsystemManifestConfiguration()
				.getImportPackage();
		if (importPackage != null && !importPackage.isEmpty())
			subsystemManifestBuilder.header(new ImportPackageHeader(
					importPackage));

		String exportPackage = getSubsystemManifestConfiguration()
				.getExportPackage();
		if (exportPackage != null && !exportPackage.isEmpty())
			subsystemManifestBuilder.header(new ExportPackageHeader(
					exportPackage));

		String preferredProvider = getSubsystemManifestConfiguration()
				.getPreferredProvider();
		if (preferredProvider != null && !preferredProvider.isEmpty())
			subsystemManifestBuilder.header(new PreferredProviderHeader(
					preferredProvider));

		String provideCapability = getSubsystemManifestConfiguration()
				.getProvideCapability();
		if (provideCapability != null && !provideCapability.isEmpty())
			subsystemManifestBuilder.header(new ProvideCapabilityHeader(
					provideCapability));

		String requireBundle = getSubsystemManifestConfiguration()
				.getRequireBundle();
		if (requireBundle != null && !requireBundle.isEmpty())
			subsystemManifestBuilder.header(new RequireBundleHeader(
					requireBundle));

		String requireCapability = getSubsystemManifestConfiguration()
				.getRequireCapability();
		if (requireCapability != null && !requireCapability.isEmpty())
			subsystemManifestBuilder.header(new RequireCapabilityHeader(
					requireCapability));

		String subsystemType = getSubsystemType().getValue();
		subsystemManifestBuilder.header(new SubsystemTypeHeader(subsystemType));

		String subsystemExportService = getSubsystemManifestConfiguration()
				.getSubsystemExportService();
		if (subsystemExportService != null && !subsystemExportService.isEmpty())
			subsystemManifestBuilder.header(new SubsystemExportServiceHeader(
					subsystemExportService));

		String subsystemImportService = getSubsystemManifestConfiguration()
				.getSubsystemImportService();
		if (subsystemImportService != null && !subsystemImportService.isEmpty())
			subsystemManifestBuilder.header(new SubsystemImportServiceHeader(
					subsystemImportService));

		String subsystemDescription = getSubsystemManifestConfiguration()
				.getSubsystemDescription();
		if (subsystemDescription != null && !subsystemDescription.isEmpty())
			subsystemManifestBuilder.header(new GenericHeader(
					"Subsystem-Description", subsystemDescription));
		
		String version = getSubsystemManifestConfiguration()
				.getSubsystemVersion();
		if (version != null && !version.isEmpty()) {
			subsystemManifestBuilder.version(version);
		} else {
			subsystemManifestBuilder.header(calculateVersionFromProject());
		}

		String symbolicName = getSubsystemManifestConfiguration()
				.getSubsystemSymbolicName();
		if (symbolicName != null && !symbolicName.isEmpty()) {
			subsystemManifestBuilder.header(new SubsystemSymbolicNameHeader(
					symbolicName));
		} else {
			subsystemManifestBuilder.header(calculateSymbolicNameFromProject());
		}

		String subsystemContent = getSubsystemManifestConfiguration()
				.getSubsystemContent();
		if (subsystemContent != null && !subsystemContent.isEmpty()) {
			subsystemManifestBuilder.header(new SubsystemContentHeader(
					subsystemContent));
		} else {
			Header<?> contentHeader = calculateContentFromProjectDependencies();
			if (contentHeader != null)
				subsystemManifestBuilder
						.header(calculateContentFromProjectDependencies());
		}

		return subsystemManifestBuilder.build();
	}

	protected abstract SubsystemType getSubsystemType();

	private Header<?> calculateVersionFromProject() {
		SubsystemVersionHeader versionHeader = null;
		versionHeader = new SubsystemVersionHeader(
				maven2OsgiConverter.getVersion(getProject().getVersion()));
		return versionHeader;
	}

	private Header<?> calculateSymbolicNameFromProject() {
		SubsystemSymbolicNameHeader symbolicNameHeader = null;
		symbolicNameHeader = new SubsystemSymbolicNameHeader(getProject()
				.getArtifactId());
		return symbolicNameHeader;
	}

	/**
	 * This method will filter the dependencies defined in the project pom and
	 * select those that has scope Runtime or Compile, type Subsystem or Bundle.
	 * By default all content will be mandatory unless the artifact is set as
	 * optional.
	 * 
	 * @return the Subsystem-Content header
	 */
	private Header<?> calculateContentFromProjectDependencies() {
		SubsystemContentHeader subsystemContentHeader = null;
		Set<Artifact> artifacts = getProject().getDependencyArtifacts();
		Set<Clause> clauses = new HashSet<SubsystemContentHeader.Clause>();

		// TODO I need to find a way to set start-order:=int in the clause
		for (Artifact artifact : artifacts) {
			String type = artifact.getType();
			String scope = artifact.getScope();
			if ((scope.equalsIgnoreCase("Runtime") || scope
					.equalsIgnoreCase("Compile"))
					&& (type.equalsIgnoreCase("bundle") || type
							.startsWith("subsystem"))) {
				StringBuilder clauseStr = new StringBuilder();
				clauseStr.append(
						maven2OsgiConverter.getBundleSymbolicName(artifact))
						.append(";");
				clauseStr.append("version=\"")
						.append(maven2OsgiConverter.getVersion(artifact))
						.append("\";");
				if (artifact.isOptional()) {
					clauseStr.append("resolution:=optional;");
				}
				Clause clause = new Clause(clauseStr.toString());
				clauses.add(clause);
			}
		}
		if (!clauses.isEmpty()) {
			subsystemContentHeader = new SubsystemContentHeader(clauses);
		}
		return subsystemContentHeader;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		File source = getProject().getFile();
		if (!getBuildContext().hasDelta(source)) {
			return;
		}

		SubsystemManifest subsystemManifest = null;
		DeploymentManifest deploymentManifest = null;

		// verify whether developer wants to use a predefined manifest file
		if (isUseProvidedSubsystemManifestFile()) {
			try {
				subsystemManifest = new SubsystemManifest(
						getProvidedSubsystemManifestFile());
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException("The file '"
						+ getProvidedSubsystemManifestFile()
						+ "' was not found.", e);
			} catch (IOException e) {
				throw new MojoFailureException(
						"A failure occurred while trying to open the file '"
								+ getProvidedSubsystemManifestFile() + "'.", e);
			}
		} else {
			subsystemManifest = buildSubsystemManifest();
		}

		if (subsystemManifest != null) {
			try {
				writeSubsystemManifestFile(subsystemManifest);
			} catch (Exception e) {
				throw new MojoFailureException(
						"A failure occurred while trying to write the Subsystem manifest file.",
						e);
			}
		}

		if (isGenerateDeploymentManifestFile()) {
			if (isUseProvidedDeploymentManifestFile()) {
				try {
					deploymentManifest = new DeploymentManifest(
							getProvidedDeploymentManifestFile());
				} catch (IOException e) {
					throw new MojoFailureException(
							"A failure occurred while trying to open the file '"
									+ getProvidedDeploymentManifestFile()
									+ "'.", e);
				}
			} else {
				deploymentManifest = buildDeploymentManifest(subsystemManifest);
			}
			if (deploymentManifest != null) {
				try {
					writeDeploymentManifestFile(deploymentManifest);
				} catch (Exception e) {
					throw new MojoFailureException(
							"A failure occurred while trying to write the Deployment manifest file.",
							e);
				}
			}

		}
	}

	protected DeploymentManifestConfiguration getDeploymentManifestConfiguration() {
		return deploymentManifestConfiguration;
	}

	protected SubsystemManifestConfiguration getSubsystemManifestConfiguration() {
		return subsystemManifestConfiguration;
	}

	protected File getProvidedDeploymentManifestFile() {
		return providedDeploymentManifestFile;
	}

	protected File getProvidedSubsystemManifestFile() {
		return providedSubsystemManifestFile;
	}

	protected File getManifestGenerationLocation() {
		return manifestGenerationLocation;
	}

	protected boolean isGenerateDeploymentManifestFile() {
		return generateDeploymentManifestFile;
	}

	protected boolean isUseProvidedDeploymentManifestFile() {
		return useProvidedDeploymentManifestFile;
	}

	protected boolean isUseProvidedSubsystemManifestFile() {
		return useProvidedSubsystemManifestFile;
	}

	synchronized void writeDeploymentManifestFile(
			DeploymentManifest deploymentManifest) throws IOException,
			URISyntaxException {
		File file = getManifestGenerationLocation();
		if (!file.exists())
			file.mkdirs();
		OutputStream fos = getBuildContext().newFileOutputStream(
				new File(file, DEPLOYMENT_MANIFEST_NAME));
		try {
			deploymentManifest.write(fos);
		} finally {
			IOUtils.close(fos);
		}
	}

	synchronized void writeSubsystemManifestFile(
			SubsystemManifest subsystemManifest) throws URISyntaxException,
			IOException {
		File file = getManifestGenerationLocation();
		if (!file.exists())
			file.mkdirs();
		OutputStream fos = getBuildContext().newFileOutputStream(
				new File(file, SUBSYSTEM_MANIFEST_NAME));
		try {
			subsystemManifest.write(fos);
		} finally {
			IOUtils.close(fos);
		}
	}
}
