/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The entry class for the <i>test</i> maven plugin. It differs from the {@link TemplatingMojo} templates:
 * <ul>
 *     <li>it pre-checks whether a specified template is in {@code src/test/templates} before checking the normal one</li>
 *     <li>contexts are searched in {@code src/test/contexts} by default</li>
 *     <li>if transformed output is to be considered source-code, then the output directory is added to the project's TestCompileSourceRoot list, not the normal one</li>
 * </ul>
 */
@Mojo(name = "test-transform", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class TemplatingTestMojo extends TemplatingBaseMojo {

    static final String TEMPLATE_DEFAULT_DIR = "src/test/templates";
    static final String CONTEXT_DEFAULT_DIR  = "src/test/contexts";
    static final String OUTPUT_DEFAULT_DIR   = "generated-test-sources/java";

    @Override
    public void execute() throws MojoExecutionException {
        templateRootDirs.add(TEMPLATE_DEFAULT_DIR);
        templateRootDirs.add(TemplatingMojo.TEMPLATE_DEFAULT_DIR);

        String outputDirectoryPath = executeBase(CONTEXT_DEFAULT_DIR, OUTPUT_DEFAULT_DIR);
        if (outputDirectoryPath != null) {
            project.addTestCompileSourceRoot(outputDirectoryPath);
        }
    }
}
