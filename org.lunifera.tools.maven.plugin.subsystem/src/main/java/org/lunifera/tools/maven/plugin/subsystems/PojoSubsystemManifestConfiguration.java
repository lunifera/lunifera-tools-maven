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

import org.apache.maven.plugins.annotations.Parameter;

/**
 * 
 * @author cvgaviao
 *
 */
public class PojoSubsystemManifestConfiguration {

	public static enum ProvisionPolicy {
		rejectDependencies,
		acceptDependencies
	}

    /**
     * The Export-Package header declares the exported packages for a Scoped
     * Subsystem.
     */
    @Parameter(alias = "Export-Package")
    private String exportPackage;

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
     * The Provide-Capability header declares the capabilities exported for a
     * Scoped Subsystem.
     */
    @Parameter(alias = "Provide-Capability")
    private String provideCapability;

    /**
     * This is used to set the provision-policy of the subsystem.<p>
     * There are two policies defined by the spec: 
     */
    @Parameter()
    private ProvisionPolicy provisionPolicy;

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
     * The Subsystem-Content header lists requirements for resources that are
     * considered to be the con- tents of this Subsystem.
     */
    @Parameter(alias = "Subsystem-Content")
    private String subsystemContent;

    /**
     * The Subsystem-Description header defines a human-readable description for
     * this Subsystem, which can potentially be localized.
     */
    @Parameter(alias = "Subsystem-Description")
    private String subsystemDescription;

    /**
     * The Subsystem-ExportService header specifies the exported services for a
     * Scoped Subsystem.
     */
    @Parameter(alias = "Subsystem-ExportService")
    private String subsystemExportService;

    /**
     * The Subsystem-ImportService header specifies the imported services for a
     * Scoped Subsystem.
     */
    @Parameter(alias = "Subsystem-ImportService")
    private String subsystemImportService;

    /**
     * The version of the Subsystem manifest spec version.
     */
    @Parameter(alias = "Subsystem-ManifestVersion", property = "1",
            readonly = true)
    private String subsystemManifestVersion;

    /**
     * The Subsystem-Name header defines a short, human-readable name for this
     * Subsystem which may be localized. This should be a short, human-readable
     * name that can contain spaces.
     */
    @Parameter(alias = "Subsystem-Name", property = "${project.name}")
    private String subsystemName;

    /**
     * The Subsystem-SymbolicName header specifies a non-localizable name for
     * this Subsystem. The Sub- system symbolic name together with a version
     * identify a Subsystem Definition though a Subsystem can be installed
     * multiple times in a framework. The Subsystem symbolic name should be
     * based on the reverse domain name convention.
     */
    @Parameter(alias = "Subsystem-SymbolicName",
            property = "${project.artifactId}")
    private String subsystemSymbolicName;

    /**
     * The Subsystem-Type header specifies the type for this Subsystem. Three
     * types of Subsystems must be supported: subsystem-application,
     * subsystem-composite and subsystem-feature.
     */
    @Parameter(alias = "Subsystem-Type", property = "${project.packaging}",
            readonly = true)
    private String subsystemType;

    /**
     * The Subsystem-Version header specifies the version of this Subsystem.
     */
    @Parameter(alias = "Subsystem-Version", required = false, property = "${project.version}")
    private String subsystemVersion;

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

    public ProvisionPolicy getProvisionPolicy() {
		return provisionPolicy;
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

    public void setProvisionPolicy(ProvisionPolicy provisionPolicy) {
		this.provisionPolicy = provisionPolicy;
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
