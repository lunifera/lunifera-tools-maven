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

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.osgi.service.indexer.ResourceIndexer;
import org.osgi.service.indexer.impl.KnownBundleAnalyzer;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * 
 * @author cvgaviao
 *
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    public static class FilesToIndexVisitor extends SimpleFileVisitor<Path> {

        private final List<PathMatcher> excludeMatchers;
        private final Set<File> filesFound;
        private final List<PathMatcher> includeMatchers;
        private Log logger;
        private int numMatches = 0;

        FilesToIndexVisitor(List<PathMatcher> includePatterns,
                List<PathMatcher> excludePatterns, Set<File> filesFound,
                Log logger) {
            this.filesFound = filesFound;
            this.logger = logger;
            this.includeMatchers = includePatterns;
            this.excludeMatchers = excludePatterns;
        }

        /**
         * returns the number of matches.
         * 
         * @return
         */
        public int count() {
            return numMatches;
        }

        private boolean isExcluded(Path path) {
            if (!excludeMatchers.isEmpty()) {
                for (PathMatcher pathMatcher : excludeMatchers) {
                    if (pathMatcher.matches(path)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isIncluded(Path path) {
            if (!includeMatchers.isEmpty()) {
                for (PathMatcher pathMatcher : includeMatchers) {
                    if (!pathMatcher.matches(path)) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            return true;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) {
            if (dir.startsWith(".")) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            if (isExcluded(dir)) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (isExcluded(file)) {
                return FileVisitResult.CONTINUE;
            }
            if (isIncluded(file)) {
                numMatches++;
                filesFound.add(file.toFile());
                logger.debug("found file: " + file.toString());
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.println(exc);
            return CONTINUE;
        }
    }

    protected static final String REPO_XML = "index.xml";

    protected static final String REPO_XML_GZ = "index.xml.gz";

    private Map<String, String> bindexConfig;

    @Component
    private BuildContext buildContext;

    /**
     * The actual root directory calculated from user parameters.
     */
    private Path calculatedRootDirPath;

    /**
     * Indicates to BIndex whether to generate a compressed file or not.
     * <p>
     * This property should be used with <b>pretty</b> property as stated in
     * table below:
     * 
     * <pre>
     * pretty   compressed         out-pretty     out-compressed
     *   null         null        Indent.NONE               true*
     *   null        false        Indent.NONE              false
     *   null         true        Indent.NONE               true
     *  false         null      Indent.PRETTY              false*
     *  false        false        Indent.NONE              false
     *  false         true        Indent.NONE               true
     *   true         null      Indent.PRETTY              false*
     *   true        false      Indent.PRETTY              false
     *   true         true      Indent.PRETTY               true
     *   
     *   * = original behaviour, before compressed was introduced
     * </pre>
     */
    @Parameter(required = true, property = "compressed", defaultValue = "true")
    private boolean compressed;

    /**
     * A list of path patterns pointing to files and directories that must be
     * included in indexer processing.
     * <p>
     * Example: {@literal "/*.jar"}
     * <p>
     * The pattern must follow the rules specified in
     * {@link FileSystem#getPathMatcher(String)}.
     */
    @Parameter(property = "excludeFilePatterns")
    private List<String> excludeFilePatterns = new ArrayList<String>();

    /**
     * A set of bundles that was added to classpath and must be used by PojoSR
     * in its execution.
     * <p>
     * This is a way to filter what PojoSR are using, avoiding it get maven or
     * eclipse classes. Example:
     * 
     * <pre>
     * {@code
     * <extraBundles>
     *    <extraBundle>org.lunifera.runtime.utils.bindex.subsystems</extraBundle>
     * </extraBundles>
     * }
     * </pre>
     */
    @Parameter(property = "extraBundles")
    private List<String> extraBundles = new ArrayList<String>();

    /**
     * A list of paths patterns pointing to files and directories that must be
     * included in indexer processing.
     * <p>
     * The pattern must follow the rules specified in
     * {@link FileSystem#getPathMatcher(String)}.
     * <p>
     * {@literal http
     * ://docs.oracle.com/javase/tutorial/essential/io/find.html}
     * 
     */
    @Parameter(property = "includeFilePatterns")
    private List<String> includeFilePatterns = new ArrayList<String>();

    /**
     * Allows BIndex to override the <b>increment</b> attribute of an existent
     * generated index repository.
     */
    @Parameter(required = true, property = "incrementOverride",
            defaultValue = "false")
    private boolean incrementOverride;

    /**
     * The URL of the index repository file that will be generated.
     * <p>
     * When it is not informed a default one will be created using the
     * {@link #rootDir} as its base directory.
     */
    @Parameter(property = "indexRepositoryPath")
    private String indexRepositoryPath = null;

    /**
     * Defines extra know bundles to be used by the {@link KnownBundleAnalyzer}.
     */
    @Parameter(property = "knownBundlesExtraFile")
    private String knownBundlesExtraFile = null;

    /**
     * 
     */
    @Parameter(required = true, property = "plugin", readonly = true)
    // for Maven 3 only
    private PluginDescriptor pluginDescriptor;

    /**
     * Used to indicate to BIndex to generate a formated uncompressed XML.
     */
    @Parameter(required = true, property = "pretty", defaultValue = "true")
    private boolean pretty;

    /**
     * The Maven project.
     */
    @Parameter(required = true, property = "project", readonly = true)
    private MavenProject project;

    /**
     * The name to be set in the generated index repository.
     * <p>
     * ex: <code>My Repository</code>
     */
    @Parameter(property = "repositoryName", defaultValue = "OSGi Repository")
    private String repositoryName;

    /**
     * A custom template used by BIndex to interpret the URL of resources that
     * will be part of generated index repository.
     * <p>
     * See details in {@link ResourceIndexer#URL_TEMPLATE}.
     */
    @Parameter(property = "resourceUrlTemplate")
    private String resourceUrlTemplate;

    /**
     * The base directory where the artifacts to be indexed will be searched.
     * <p>
     * BIndex will use it to relativize they path against to it.
     * <p>
     * When this property is not set BIndex will use the maven
     * {@link #localRepository}.
     */
    @Parameter(required = false, property = "rootDir")
    private String rootDir;

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
    @Parameter(required = true, property = "session", readonly = true)
    private MavenSession session;

    /**
     * The calculated rootURL;
     */
    // private URL rootURL;

    /**
     * Set this to <code>true</code> to skip the generation of the Subsystem
     * Manifest file.
     */
    @Parameter(defaultValue = "false", property = "skip", required = true)
    private boolean skip;

    /**
     * Whether BIndex will display about its processing on console.
     * <p>
     * An LogService implementation must be in the classpath in order this to
     * work.
     */
    @Parameter(required = true, property = "verbose", defaultValue = "false")
    private boolean verbose;

    public AbstractMojo() {
    }

    private List<PathMatcher> buildExcludeFileMatchers() {
        List<PathMatcher> excludeMatchers = new ArrayList<PathMatcher>();
        for (String exclude : getExcludeFilePatterns()) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
                    "glob:" + exclude);
            excludeMatchers.add(matcher);
        }
        return excludeMatchers;
    }

    private List<PathMatcher> buildIncludeFileMatchers() {
        List<PathMatcher> includeMatchers = new ArrayList<PathMatcher>();
        for (String include : getIncludeFilePatterns()) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
                    "glob:" + include);
            includeMatchers.add(matcher);
        }
        return includeMatchers;
    }

    protected abstract Path calculateIndexFilePath()
            throws MojoExecutionException;

    protected abstract Path calculateRootDirPath(String rootUrlStr)
            throws MojoExecutionException;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        BindexWrapper bindexWrapper;
        Path indexRepositoryPath;

        if (!runtimeInformation.isMavenVersion("[3.2,)")) {
            throw new UnsupportedOperationException(
                    "Lunifera requires Maven 3.2 or higher.");
        }
        if (skip) {
            getLog().info(
                    "Skiping " + shortDescription() + " for project "
                            + getProject().getId());
            return;
        }

//        getExtraBundles().add("osgi.core");
        getExtraBundles().add("org.lunifera.runtime.utils.bindex");
        getExtraBundles().add("de.kalpatec.pojosr.framework");
        getExtraBundles().add("org.apache.felix.configadmin");
        getExtraBundles().add("org.apache.felix.scr");
        getExtraBundles().add("org.apache.felix.log");

        getIncludeFilePatterns().add("**/*.jar");
        getIncludeFilePatterns().add("**/*.esa");
        getExcludeFilePatterns().add("**/.meta");
        getExcludeFilePatterns().add("**/.cache");
        getExcludeFilePatterns().add("**/.locks");
        getExcludeFilePatterns().add("**/*-javadoc*");
        extractBindexParameters(getBindexConfig());

        indexRepositoryPath = calculateIndexFilePath();

        try {
            int result = 0;
            bindexWrapper = new BindexWrapper(getClassLoader(),
                    getKnownBundlesExtraFile(), extraBundles(), getLog());

            synchronized (bindexWrapper) // protect against concurrent
            // in-process updates
            {
                result = executeRepositoryIndexer(bindexWrapper,
                        indexRepositoryPath, buildIncludeFileMatchers(),
                        buildExcludeFileMatchers(), getBindexConfig());
            }
            getLog().info(
                    "OSGi Repository indexing finished. It was processed "
                            + result + " files.");

        } finally {
            bindexWrapper = null;
            calculatedRootDirPath = null;
            indexRepositoryPath = null;
            bindexConfig = null;
        }
    }

    protected abstract int executeRepositoryIndexer(
            BindexWrapper bindexWrapper, Path indexRepositoryPath,
            List<PathMatcher> includeMatchers,
            List<PathMatcher> excludeMatchers, Map<String, String> bindexConfig)
            throws MojoExecutionException;

    protected List<String> extraBundles() {
        return extraBundles;
    }

    protected void extractBindexParameters(Map<String, String> bindexConfig)
            throws MojoExecutionException {

        bindexConfig.put(ResourceIndexer.ROOT_URL,
                calculateRootDirPath(getRootDir()).toString());

        if (isVerbose() == true) {
            bindexConfig.put(ResourceIndexer.VERBOSE, Boolean.TRUE.toString());
        }
        if (isCompressed() == true) {
            getBindexConfig().put(ResourceIndexer.COMPRESSED,
                    Boolean.TRUE.toString());
        } else {
            getBindexConfig().put(ResourceIndexer.COMPRESSED,
                    Boolean.FALSE.toString());
        }
        if (getRepositoryName() != null && !getRepositoryName().isEmpty()) {
            bindexConfig.put(ResourceIndexer.REPOSITORY_NAME,
                    getRepositoryName());
        }
        if (getResourceUrlTemplate() != null
                && !getResourceUrlTemplate().isEmpty()) {
            bindexConfig.put(ResourceIndexer.URL_TEMPLATE,
                    getResourceUrlTemplate());
        }
        if (isPretty() == true) {
            bindexConfig.put(ResourceIndexer.PRETTY, Boolean.TRUE.toString());
        }
        if (isIncrementOverride() == true) {
            bindexConfig.put("-repository.increment.override", "");
        }
        if (!getProject().getLicenses().isEmpty()) {
            License license = getProject().getLicenses().get(0);
            bindexConfig.put(ResourceIndexer.LICENSE_URL, license.getUrl());
        }
    }

    protected Map<String, String> getBindexConfig() {
        if (bindexConfig == null) {
            bindexConfig = new HashMap<>(10);
        }
        return bindexConfig;
    }

    protected BuildContext getBuildContext() {
        return buildContext;
    }

    protected Path getCalculatedRootDirPath() throws MojoExecutionException {
        if (calculatedRootDirPath == null) {
            calculatedRootDirPath = calculateRootDirPath(getRootDir());
        }
        return calculatedRootDirPath;
    }

    protected ClassLoader getClassLoader() throws MojoExecutionException {
        return this.getClass().getClassLoader();
    }

    protected List<String> getExcludeFilePatterns() {
        return excludeFilePatterns;
    }

    protected List<String> getExtraBundles() {
        return extraBundles;
    }

    protected List<String> getIncludeFilePatterns() {
        return includeFilePatterns;
    }

    protected String getIndexRepositoryPath() {
        return indexRepositoryPath;
    }

    protected String getKnownBundlesExtraFile() {
        return knownBundlesExtraFile;
    }

    /**
     * Used to get the classpath of the plugin to pass it to PojoSr.
     * 
     * @return
     */
    protected PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }

    /**
     * Used to get the classpath of the current user project.
     */
    protected MavenProject getProject() {
        return project;
    }

    protected String getRepositoryName() {
        return repositoryName;
    }

    protected String getResourceUrlTemplate() {
        return resourceUrlTemplate;
    }

    protected String getRootDir() {
        return rootDir;
    }

    protected MavenSession getSession() {
        return session;
    }

    protected boolean isCompressed() {
        return compressed;
    }

    protected boolean isIncrementOverride() {
        return incrementOverride;
    }

    protected boolean isPretty() {
        return pretty;
    }

    protected boolean isSkip() {
        return skip;
    }

    protected boolean isVerbose() {
        return verbose;
    }

    protected void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    protected void setIncrementOverride(boolean incrementOverride) {
        this.incrementOverride = incrementOverride;
    }

    protected void setIndexRepositoryURLStr(String indexRepositoryURLStr) {
        this.indexRepositoryPath = indexRepositoryURLStr;
    }

    protected void setKnownBundlesExtraFile(String knownBundlesExtraFile) {
        this.knownBundlesExtraFile = knownBundlesExtraFile;
    }

    protected void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    protected void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    protected void setResourceUrlTemplate(String resourceUrlTemplate) {
        this.resourceUrlTemplate = resourceUrlTemplate;
    }

    protected void setRootURLStr(String rootURLStr) {
        this.rootDir = rootURLStr;
    }

    protected void setSkip(boolean skip) {
        this.skip = skip;
    }

    protected void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    protected abstract String shortDescription();
}
