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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryUpdaterTest {

    @Test
    public void testSearchPatternFormatting() throws URISyntaxException {
        Log log = Mockito.mock(Log.class);
        BindexWrapper bindexWrapper = new BindexWrapper(getClass()
                .getClassLoader(), "", Arrays.asList(""), log);
        String formatted = bindexWrapper.getArtifactSearchPattern(
                "ant-launcher", "1.8.2-SNAPSHOT");
        assertThat(
                formatted,
                equalTo("//repo:resource[repo:capability[repo:attribute[@name='osgi.identity' and contains(@value,'ant-launcher')] and repo:attribute[@name='version' and contains(@value,'1.8.2')]]]"));
        String formatted2 = bindexWrapper.getArtifactSearchPattern("ant",
                "1.7.1");
        assertThat(
                formatted2,
                equalTo("//repo:resource[repo:capability[repo:attribute[@name='osgi.identity' and contains(@value,'ant')] and repo:attribute[@name='version' and @value='1.7.1']]]"));
    }

}
