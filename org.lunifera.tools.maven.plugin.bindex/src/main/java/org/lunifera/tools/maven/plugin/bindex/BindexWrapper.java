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

/*
 * Part of this code was borrowed from maven-bundle-plugin (https://github.com/apache/felix.git)
 * project that is released under Apache License Version 2.0
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.osgi.service.indexer.ResourceAnalyzer;
import org.osgi.service.indexer.ResourceIndexer;
import org.osgi.service.indexer.impl.KnownBundleAnalyzer;
import org.osgi.util.tracker.ServiceTracker;

import de.kalpatec.pojosr.framework.launch.BundleDescriptor;
import de.kalpatec.pojosr.framework.launch.ClasspathScanner;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistryFactory;

/**
 * Wrapper the PojoSr and BIndex.
 * 
 * @author cvgaviao
 *
 */
public class BindexWrapper {

    public final static Namespace NS = Namespace.getNamespace("repo",
            "http://www.osgi.org/xmlns/repository/v1.0.0");
    private final static String SEARCH_PATTERN = "//repo:resource[repo:capability[repo:attribute[@name='osgi.identity' and contains(@value,'%s')] and repo:attribute[@name='version' and @value='%s']]]";
    private final static String SEARCH_PATTERN_SNAPSHOT = "//repo:resource[repo:capability[repo:attribute[@name='osgi.identity' and contains(@value,'%s')] and repo:attribute[@name='version' and contains(@value,'%s')]]]";

    // private final static String SEARCH_PATTERN =
    // "//repo:resource[repo:capability[repo:attribute[@name='osgi.identity' and @value='%s'] and repo:attribute[@name='version' and @value='%s']]]";

    private static Properties loadPropertiesFile(File knownBundles)
            throws FileNotFoundException, IOException {
        Properties props = new Properties();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(knownBundles);
            props.load(stream);
        } finally {
            if (stream != null)
                stream.close();
        }
        return props;
    }

    /**
     * The ClassLoader to be used by PojoSr.
     */
    private ClassLoader classLoader;

    /**
     * values to be added to PojoSr bundle filter.
     */
    private List<String> extraBundles;

    /**
     * Used by the BIndex component.
     */
    private File knownBundlesExtraFile;

    /**
     * logger for this plugin.
     */
    private Log logger;

    /**
     * initialize information.
     * 
     * @param repositoryXmlURL
     *            path to the repository descriptor file
     * @param filterClauses
     * @param mavenRepositoryPath
     *            path to the local maven repository
     * @param config
     *            user information
     * @param logger
     *            plugin logger
     */
    public BindexWrapper(ClassLoader classLoader,
            String knownBundlesExtraFilePath, List<String> extraBundles,
            Log logger) {
        this.classLoader = classLoader;
        this.logger = logger;
        this.extraBundles = extraBundles;
        if (knownBundlesExtraFilePath != null) {
            knownBundlesExtraFile = new File(knownBundlesExtraFilePath);
        }
    }

    private Document generateNewDocumentWithNewElements(
            Set<Artifact> artifacts, List<Element> newElements, InputStream bis)
            throws JDOMException, IOException {

        SAXBuilder saxBuilder = new SAXBuilder();
        org.jdom2.Document document = saxBuilder.build(bis);
        XPathFactory xpathFactory = XPathFactory.instance();
        for (Artifact artifact : artifacts) {
            String symbolicName = artifact.getArtifactId();
            String version = artifact.getVersion();
            XPathExpression<Element> expr = xpathFactory.compile(
                    getArtifactSearchPattern(symbolicName, version),
                    Filters.element(NS), null, NS);
            List<Element> elements = expr.evaluate(document);
            for (Element element : elements) {
                if (element != null) {

                    logger.debug("Detaching from document: "
                            + element.getName());
                    element.detach();
                }
            }
        }
        document.getRootElement().getChildren().addAll(newElements);

        return document;
    }

    private void generateNewDocumentWithNewElements(Set<Artifact> artifacts,
            List<Element> newElements, Path repositoryXmlPath,
            boolean compressed, boolean pretty) throws MojoExecutionException {
        int bufferSize = 8 * 1024;
        if (compressed) {
            logger.debug("opening the gzip file at: " + repositoryXmlPath);
            Path extractedFile;
            Path backupGZipFile;

            try {
                // we need to backup the gz file before operate on it because it
                // will corrupt on any failure.
                backupGZipFile = Files
                        .copy(repositoryXmlPath, Files.createTempFile(
                                repositoryXmlPath.getParent(), "", ".backup"),
                                StandardCopyOption.REPLACE_EXISTING);
                backupGZipFile.toFile().deleteOnExit();
            } catch (IOException e1) {
                throw new MojoExecutionException(
                        "Error occurred when backing up the gzip file.", e1);
            }

            try {
                extractedFile = Files.createTempFile(
                        repositoryXmlPath.getParent(), "", ".index.xml");
                extractedFile.toFile().deleteOnExit();
            } catch (IOException e1) {
                throw new MojoExecutionException(
                        "Error occurred while creating the file to be extracted.",
                        e1);
            }

            // extract the xml from the gzip file
            try (InputStream gis = new GZIPInputStream(
                    Files.newInputStream(repositoryXmlPath), bufferSize);
                    OutputStream fos = Files.newOutputStream(extractedFile)) {
                byte[] buffer = new byte[bufferSize];
                int len;
                while ((len = gis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Error occurred when extracting xml from the gzip file.",
                        e);
            }

            try (InputStream extractedFileIS = Files
                    .newInputStream(extractedFile);
                    OutputStream newFileOS = new GZIPOutputStream(
                            Files.newOutputStream(repositoryXmlPath),
                            bufferSize) {
                        {
                            def.setLevel(Deflater.BEST_COMPRESSION);
                        }
                    }) {
                Document newDocument = generateNewDocumentWithNewElements(
                        artifacts, newElements, extractedFileIS);
                XMLOutputter xmlOutputter = new XMLOutputter(
                        (pretty ? Format.getPrettyFormat()
                                : Format.getRawFormat()));
                xmlOutputter.output(newDocument, newFileOS);
            } catch (Exception e) {
                logger.error(e);
                try {
                    Files.move(backupGZipFile, repositoryXmlPath);
                } catch (IOException e1) {
                    throw new MojoExecutionException(
                            "Error occurred while restoring the backup of the old index.xml.gz file",
                            e);
                }
                throw new MojoExecutionException(
                        "Error occurred while generating the new .gz file", e);
            }
        } else {
            Path backupFile;
            try {
                backupFile = Files.move(repositoryXmlPath, Files
                        .createTempFile(repositoryXmlPath.getParent(), "",
                                ".index"), StandardCopyOption.REPLACE_EXISTING);
                try (InputStream backupFileIS = Files.newInputStream(
                        backupFile, StandardOpenOption.DELETE_ON_CLOSE);
                        OutputStream newFileOS = Files
                                .newOutputStream(repositoryXmlPath)) {
                    Document newDocument = generateNewDocumentWithNewElements(
                            artifacts, newElements, backupFileIS);
                    XMLOutputter xmlOutputter = new XMLOutputter(
                            (pretty ? Format.getPrettyFormat()
                                    : Format.getRawFormat()));
                    xmlOutputter.output(newDocument, newFileOS);
                }
            } catch (IOException | JDOMException e) {
                throw new MojoExecutionException(
                        "Error occurred while generating the new index.xml file",
                        e);
            }
        }
    }

    /**
     * Generated a new repository index file using the provided set of files.
     * <p>
     * It will use the OSGi Alliance BIndex library in order to create/update
     * the repository index file.
     * 
     * @param filesToIndex
     * @param bindexConfig
     * 
     * @throws MojoExecutionException
     *             if the plugin failed
     */
    public int generateRepositoryIndex(Set<File> filesToIndex,
            Path repositoryXmlPath, Map<String, String> bindexConfig)
            throws MojoExecutionException {
        logger.debug(" (f) repositoryXml = " + repositoryXmlPath);
        logger.debug(" (f) files = " + filesToIndex);
        logger.debug(" (f) config = " + bindexConfig);

        if (repositoryXmlPath == null) {
            logger.error("The repository index informed must be a valid one.");
            return 0;
        }
        if (filesToIndex.isEmpty()) {
            logger.warn("No file was processed by Bindex.");
            return 0;
        }
        try {
            ResourceIndexer index = setupIndexerService();
            // Run
            try (OutputStream output = Files.newOutputStream(repositoryXmlPath)) {
                logger.info("Repository Indexer started to process "
                        + filesToIndex.size() + " files.");
                index.index(filesToIndex, output, bindexConfig);
                return filesToIndex.size();
            } catch (Exception e) {
                throw new MojoExecutionException(
                        "Repository Indexer was unable to generate the repository index file ("
                                + repositoryXmlPath + ").", e);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error executing PojoSR.", e);
        }
    }

    /**
     * 
     * @param artifact
     * @param bindexConfig
     * @return
     * @throws MojoExecutionException
     */
    public String generateResourceFragmentForArtifacts(Set<Artifact> artifacts,
            Map<String, String> bindexConfig) throws MojoExecutionException {
        logger.debug(" (f) config = " + bindexConfig);
        // Run
        StringWriter writer = new StringWriter();
        Set<File> filesToIndex = new HashSet<File>();

        try {
            for (Artifact artifact2 : artifacts) {
                filesToIndex.add(artifact2.getFile());
            }
            ResourceIndexer index = setupIndexerService();
            index.indexFragment(filesToIndex, writer, bindexConfig);
            return writer.toString();
        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Repository Indexer was unable to generate an update fragment for artifacts.",
                    e);
        }
    }

    protected String getArtifactSearchPattern(String artifactId, String version) {
        String formatted;
        int index = version.indexOf("-SNAPSHOT");
        if (index != -1) {
            version = version.substring(0, index);
            formatted = String.format(SEARCH_PATTERN_SNAPSHOT, artifactId,
                    version);
        } else {
            formatted = String.format(SEARCH_PATTERN, artifactId, version);
        }
        return formatted;
    }

    private List<Element> getElementsFromGeneratedXmlFragment(
            String generatedXMLFragment) throws MojoExecutionException {
        // transform the fragment string to be inserted/updated into a document
        // node
        org.jdom2.Document fragmentDoc = null;
        List<Element> newElements = new ArrayList<Element>();
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            StringBuilder fragmentBuilder = new StringBuilder(
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            fragmentBuilder
                    .append("<repo:repository increment=\"1410476149404\" name=\"fragment\" xmlns:repo=\"http://www.osgi.org/xmlns/repository/v1.0.0\">");
            fragmentBuilder.append(generatedXMLFragment);
            fragmentBuilder.append("</repo:repository>");
            fragmentDoc = saxBuilder.build(new StringReader(fragmentBuilder
                    .toString()));

            for (Element element : fragmentDoc.getRootElement().getChildren(
                    "resource", NS)) {
                newElements.add(element.clone().detach());
            }
            if (newElements.isEmpty()) {
                logger.error("Error while transforming xml framgment into a JDOM document.");
            }
        } catch (JDOMException | IOException e1) {
            throw new MojoExecutionException(
                    "Error while transforming xml framgment into a JDOM document.",
                    e1);
        }
        return newElements;
    }

    private ResourceIndexer setupIndexerService() throws Exception {
        KnownBundleAnalyzer knownBundleAnalyzer = null;

        // Configure PojoSR
        Map<String, Object> pojoSrConfig = new HashMap<String, Object>();

        // only allowed bundles must be part of BIndex execution
        StringBuilder filter = new StringBuilder("(|");
        for (String bsn : extraBundles) {
            filter.append("(Bundle-SymbolicName=");
            filter.append(bsn).append(")");
        }
        filter.append(")");

        ClasspathScanner scanner = new ClasspathScanner();
        List<BundleDescriptor> bundles = new ArrayList<>();
        bundles = scanner.scanForBundles(filter.toString(), classLoader);
        logger.debug("PojoSr loaded using the following classpath: " + bundles);
        pojoSrConfig
                .put(PojoServiceRegistryFactory.BUNDLE_DESCRIPTORS, bundles);

        // Start PojoSR Service Registry
        ServiceLoader<PojoServiceRegistryFactory> loader = ServiceLoader
                .load(PojoServiceRegistryFactory.class);

        PojoServiceRegistry registry = loader.iterator().next()
                .newPojoServiceRegistry(pojoSrConfig);

        // Look for indexer and run index generation
        ServiceTracker<ResourceIndexer, ResourceIndexer> tracker = new ServiceTracker<>(
                registry.getBundleContext(), ResourceIndexer.class.getName(),
                null);
        tracker.open();
        ResourceIndexer index = (ResourceIndexer) tracker.waitForService(1000);
        if (index == null)
            throw new MojoExecutionException(
                    "Timed out waiting for ResourceIndexer service.");

        if (knownBundleAnalyzer == null)
            knownBundleAnalyzer = new KnownBundleAnalyzer();

        if (knownBundlesExtraFile != null) {
            Properties props = loadPropertiesFile(knownBundlesExtraFile);
            knownBundleAnalyzer.setKnownBundlesExtra(props);
        }
        registry.getBundleContext().registerService(
                ResourceAnalyzer.class.getName(), knownBundleAnalyzer, null);
        return index;
    }

    public int updateIndexRepositoryForArtifactSet(Set<Artifact> artifacts,
            Path repositoryXmlPath, Map<String, String> bindexConfig)
            throws MojoExecutionException {
        boolean pretty = Boolean.parseBoolean(bindexConfig
                .get(ResourceIndexer.PRETTY));
        boolean compressed = Boolean.parseBoolean(bindexConfig
                .get(ResourceIndexer.COMPRESSED));
        String fragment = generateResourceFragmentForArtifacts(artifacts,
                bindexConfig);
        if (fragment == null) {
            logger.info("Indexing finish without a new repository, it was generated a empty fragment.");
            return -1;
        }
        List<Element> newElements = getElementsFromGeneratedXmlFragment(fragment);

        // will remove old resources when artifact has a snapshot version
        generateNewDocumentWithNewElements(artifacts, newElements,
                repositoryXmlPath, compressed, pretty);

        return newElements.size();
    }

}
