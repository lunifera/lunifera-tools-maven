/**
 * Copyright (c) 2011 - 2014, Lunifera GmbH (Gross Enzersdorf)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Based on Xtend Maven Plugin
 * 
 * Contributors: 
 * 		Florian Pirchner - Initial implementation
 */
package org.lunifera.tooling.entity;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class EntityGrammarCompilerMojoIT {

	private static String ROOT = "/it/compile";

	private void deleteFileIfExist(String path) throws URISyntaxException {
		URL userCodeURL = getClass().getResource(ROOT + "/filesystemaccess-client/src/main/java/" + path);
		if (userCodeURL != null) {
			new File(userCodeURL.toURI()).delete();
		}
	}

	@Test
	public void projectWithMultipleSourceDirectories() throws Exception {
		verifyErrorFreeLog(ROOT + "/multisources");
	}

	@Test
	public void encoding() throws Exception {
		Verifier verifier = newVerifier(ROOT + "/encoding");

		String entityDir = verifier.getBasedir() + "/src/main/java";
		assertFileContainsUTF16(verifier, entityDir + "/test/EntityA.entitymodel", "Mühlheim-Kärlicher Bürger");

		verifier.setDebug(true);
		verifier.executeGoal("test");
		verifier.verifyErrorFreeLog();

		String gen = verifier.getBasedir() + "/src/main/generated-sources/entity/test/EntityA.java";
		assertFileContainsUTF16(verifier, gen, "Mühlheim-Kärlicher Bürger");
	}

	@Test
	public void pluginPrefix() throws Exception {
		verifyErrorFreeLog(ROOT + "/encoding", "entity:compile");
	}

	@Test
	public void aggregation() throws Exception {
		Verifier verifier = newVerifier(ROOT + "/aggregation");
		verifier.setDebug(true);
		verifier.executeGoal("test");
		verifier.setForkJvm(false);
		verifier.verifyErrorFreeLog();
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=409759
		String outputdir = verifier.getBasedir() + "/relativeoutput-module/";

		verifier.assertFilePresent(outputdir + "src/main/generated-sources/entity/org/test/multisource/EntityA.java");
		verifier.assertFilePresent(outputdir + "src/main/generated-sources/entity/org/test/multisource/EntityB.java");

		verifier.assertFilePresent(outputdir + "src/test/generated-sources/entity/org/test/multisource/EntityA.java");
		verifier.assertFilePresent(outputdir + "src/test/generated-sources/entity/org/test/multisource/EntityB.java");
	}

	@Test
	public void haltOnValidationErrors() throws Exception {
		Verifier verifier = newVerifier(ROOT + "/entityerrors");
		try {
			verifier.executeGoal("verify");
			Assert.fail("expected org.apache.maven.plugin.MojoExecutionException");
		} catch (Exception e) {
			verifier.verifyTextInLog("3: Superclass must be a class");
			verifier.verifyTextInLog("BUILD FAILURE");
		}
	}

	@Test
	public void continueOnWarnings() throws Exception {
		Verifier verifier = newVerifier(ROOT + "/entitywarnings");
		verifier.executeGoal("verify");
		verifier.verifyTextInLog("3: The import 'java.util.Collections' is never used.");
		verifier.verifyTextInLog("[INFO] BUILD SUCCESS");
	}

	private void verifyErrorFreeLog(String pathToTestProject) throws IOException, VerificationException {
		verifyErrorFreeLog(pathToTestProject, "verify");
	}

	private void verifyErrorFreeLog(String pathToTestProject, String goal) throws IOException, VerificationException {
		Verifier verifier = newVerifier(pathToTestProject);
		verifier.setDebug(true);
		verifier.executeGoal(goal);
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}

	private Verifier newVerifier(String pathToTestProject) throws IOException, VerificationException {
		File testDir = ResourceExtractor.simpleExtractResources(getClass(), pathToTestProject);
		Verifier verifier = new Verifier(testDir.getAbsolutePath());
		// verifier.setDebugJvm(true);
		// verifier.setForkJvm(false);
		return verifier;
	}

	public void assertFileContainsUTF16(Verifier verifier, String file, String contained) {
		verifier.assertFilePresent(file);
		try {
			String content = FileUtils.fileRead(new File(file), "UTF-16");
			if (!content.contains(contained)) {
				Assert.fail("Content of " + file + " does not contain " + contained + " but: " + content);
			}
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
	}
}
