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

public class SubsystemManifestConfiguration {

	/**
	 * The Require-Bundle header declares the required bundles for a Scoped
	 * Subsystem.
	 */
	@Parameter(alias = "Require-Bundle")
	private String requireBundle;

	/**
	 * The Require-Capability header declares the required capabilities for a
	 * Scoped Subsystem.
	 */
	@Parameter(alias = "Require-Capability")
	private String requireCapability;

	/**
	 * The Export-Package header declares the exported packages for a Scoped
	 * Subsystem.
	 */
	@Parameter(alias = "Export-Package")
	private String exportPackage;

	/**
	 * The Provide-Capability header declares the capabilities exported for a
	 * Scoped Subsystem.
	 */
	@Parameter(alias = "Provide-Capability")
	private String provideCapability;

	/**
	 * The Subsystem-Name header defines a short, human-readable name for this
	 * Subsystem which may be localized. This should be a short, human-readable
	 * name that can contain spaces.
	 */
	@Parameter(alias = "Subsystem-Name")
	private String subsystemName;

	/**
	 * The Subsystem-SymbolicName header specifies a non-localizable name for
	 * this Subsystem. The Sub- system symbolic name together with a version
	 * identify a Subsystem Definition though a Subsystem can be installed
	 * multiple times in a framework. The Subsystem symbolic name should be
	 * based on the reverse domain name convention.
	 */
	@Parameter(alias = "Subsystem-SymbolicName", required = false)
	private String subsystemSymbolicName;

	/**
	 * The Subsystem-Version header specifies the version of this Subsystem.
	 */
	@Parameter(alias = "Subsystem-Version", required = false)
	private String subsystemVersion;

	/**
	 * The Import-Package header declares the imported packages for a Scoped
	 * Subsystem.
	 */
	@Parameter(alias = "Import-Package")
	private String importPackage;

	/**
	 * The Preferred-Provider header declares a list bundles and Subsystems
	 * which are the providers of capabilities that are preferred when wiring
	 * the requirements of a Scoped Subsystem.
	 */
	@Parameter(alias = "Preferred-Provider")
	private String preferredProvider;

	/**
	 * The Subsystem-Content header lists requirements for resources that are
	 * considered to be the con- tents of this Subsystem.
	 */
	@Parameter(alias = "Subsystem-Content")
	private String subsystemContent;

	/**
	 * The Subsystem-ExportService header specifies the exported services for a
	 * Scoped Subsystem.
	 */
	@Parameter(alias = "Subsystem-ExportService")
	private String subsystemExportService;

	/**
	 * The Subsystem-Description header defines a human-readable description for
	 * this Subsystem, which can potentially be localized.
	 */
	@Parameter(alias = "Subsystem-Description")
	private String subsystemDescription;

	/**
	 * The Subsystem-ImportService header specifies the imported services for a
	 * Scoped Subsystem.
	 */
	@Parameter(alias = "Subsystem-ImportService")
	private String subsystemImportService;

	public String getExportPackage() {
		return exportPackage;
	}

	public String getImportPackage() {
		return importPackage;
	}

	public String getPreferredProvider() {
		return preferredProvider;
	}

	public String getProvideCapability() {
		return provideCapability;
	}

	public String getRequireBundle() {
		return requireBundle;
	}

	public String getRequireCapability() {
		return requireCapability;
	}

	public String getSubsystemContent() {
		return subsystemContent;
	}

	public String getSubsystemDescription() {
		return subsystemDescription;
	}

	public String getSubsystemExportService() {
		return subsystemExportService;
	}

	public String getSubsystemImportService() {
		return subsystemImportService;
	}

	public String getSubsystemName() {
		return subsystemName;
	}

	public String getSubsystemSymbolicName() {
		return subsystemSymbolicName;
	}

	public String getSubsystemVersion() {
		return subsystemVersion;
	}

	public void setExportPackage(String exportPackage) {
		this.exportPackage = exportPackage;
	}

	public void setImportPackage(String importPackage) {
		this.importPackage = importPackage;
	}

	public void setPreferredProviders(String preferredProviders) {
		this.preferredProvider = preferredProviders;
	}

	public void setProvideCapability(String provideCapability) {
		this.provideCapability = provideCapability;
	}

	public void setRequireBundle(String requireBundle) {
		this.requireBundle = requireBundle;
	}

	public void setRequireCapability(String requireCapability) {
		this.requireCapability = requireCapability;
	}

	public void setSubsystemContent(String subsystemContent) {
		this.subsystemContent = subsystemContent;
	}

	public void setSubsystemDescription(String subsystemDescription) {
		this.subsystemDescription = subsystemDescription;
	}

	public void setSubsystemExportService(String subsystemExportService) {
		this.subsystemExportService = subsystemExportService;
	}

	public void setSubsystemImportService(String subsystemImportService) {
		this.subsystemImportService = subsystemImportService;
	}

	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}

	public void setSubsystemSymbolicName(String subsystemSymbolicName) {
		this.subsystemSymbolicName = subsystemSymbolicName;
	}

	public void setSubsystemVersion(String subsystemVersion) {
		this.subsystemVersion = subsystemVersion;
	}

}