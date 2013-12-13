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

import org.apache.maven.plugins.annotations.Parameter;

public class DeploymentManifestConfiguration extends AbstractSubsystemMojo {

	/**
	 * The Deployment-ManifestVersion header defines that the deployment
	 * manifest follows the rules of a Subsystems Specification. It is 1 (the
	 * default) for this version of the specification. Future versions of the
	 * Subsystems Specification can define higher numbers for this header.
	 */
	private String deploymentManifestVersion = "1";
	/**
	 * The Subsystem-SymbolicName header specifies a non-localizable name for
	 * the Subsystem that the deployment manifest is for. The Subsystem symbolic
	 * name together with a version must identify a unique Subsystem though it
	 * can be installed multiple times in a framework.
	 */
	@Parameter(alias = "Subsystem-SymbolicName", required = true)
	private String subsystemSymbolicName;

	/**
	 * The Subsystem-Version header specifies the version of this Subsystem that
	 * the deployment manifest is for.
	 */
	@Parameter(alias = "Subsystem-Version", required = true)
	private String subsystemVersion;
	/**
	 * The Deployed-Content header lists requirements for the exact resources
	 * that are considered to be the contents of this Subsystem. This header
	 * identifies the exact versions of the resources listed in the
	 * Subsystem-Content header.
	 */
	@Parameter(alias = "Deployed-Content")
	private String deployedContent;
	/**
	 * The Provision-Resource header lists requirements for the exact resources
	 * to be installed in order to satisfy requirements from the
	 * Deployed-Content resources that are not satisfied by the capabilities of
	 * the target runtime.
	 */
	@Parameter(alias = "Provision-Resource")
	private String provisionResource;

	/**
	 * The Import-Package header lists package requirements for capabilities
	 * that are to be imported into a Scoped Subsystem.
	 */
	@Parameter(alias = "Import-Package")
	private String importPackage;

	/**
	 * The Export-Package header lists package capabilities that are to be
	 * exported out of a Scoped Sub- system.
	 */
	@Parameter(alias = "Export-Package")
	private String exportPackage;

	/**
	 * The Require-Bundle header lists bundle requirements for bundle
	 * capabilities that are to be imported into a Scoped Subsystem.
	 */
	@Parameter(alias = "Require-Bundle")
	private String requireBundle;

	/**
	 * The Provide-Capability header declares the capabilities exported for a
	 * Scoped Subsystem.
	 */
	@Parameter(alias = "Provide-Capability")
	private String provideCapability;

	/**
	 * The Require-Capability header declares the required capabilities for a
	 * Scoped Subsystem.
	 */
	@Parameter(alias = "Require-Capability")
	private String requireCapability;

	/**
	 * The Subsystem-ExportService header lists service requirements that are
	 * matched against service capabilities provided by the Deployed-Content
	 * resources. Any matching capabilities are exported out of the Scoped
	 * Subsystem.
	 */
	@Parameter(alias = "Subsystem-ExportService")
	private String subsystemExportService;

	/**
	 * The Subsystem-ImportService header lists service requirements for service
	 * capabilities that are to be imported into a Scoped Subsystem.
	 */
	@Parameter(alias = "Subsystem-ImportService")
	private String subsystemImportService;

	public String getDeploymentManifestVersion() {
		return deploymentManifestVersion;
	}

	public void setDeploymentManifestVersion(String deploymentManifestVersion) {
		this.deploymentManifestVersion = deploymentManifestVersion;
	}

	public String getSubsystemSymbolicName() {
		return subsystemSymbolicName;
	}

	public void setSubsystemSymbolicName(String subsystemSymbolicName) {
		this.subsystemSymbolicName = subsystemSymbolicName;
	}

	public String getSubsystemVersion() {
		return subsystemVersion;
	}

	public void setSubsystemVersion(String subsystemVersion) {
		this.subsystemVersion = subsystemVersion;
	}

	public String getDeployedContent() {
		return deployedContent;
	}

	public void setDeployedContent(String deployedContent) {
		this.deployedContent = deployedContent;
	}

	public String getProvisionResource() {
		return provisionResource;
	}

	public void setProvisionResource(String provisionResource) {
		this.provisionResource = provisionResource;
	}

	public String getImportPackage() {
		return importPackage;
	}

	public void setImportPackage(String importPackage) {
		this.importPackage = importPackage;
	}

	public String getExportPackage() {
		return exportPackage;
	}

	public void setExportPackage(String exportPackage) {
		this.exportPackage = exportPackage;
	}

	public String getRequireBundle() {
		return requireBundle;
	}

	public void setRequireBundle(String requireBundle) {
		this.requireBundle = requireBundle;
	}

	public String getProvideCapability() {
		return provideCapability;
	}

	public void setProvideCapability(String provideCapability) {
		this.provideCapability = provideCapability;
	}

	public String getRequireCapability() {
		return requireCapability;
	}

	public void setRequireCapability(String requireCapability) {
		this.requireCapability = requireCapability;
	}

	public String getSubsystemExportService() {
		return subsystemExportService;
	}

	public void setSubsystemExportService(String subsystemExportService) {
		this.subsystemExportService = subsystemExportService;
	}

	public String getSubsystemImportService() {
		return subsystemImportService;
	}

	public void setSubsystemImportService(String subsystemImportService) {
		this.subsystemImportService = subsystemImportService;
	}

}
