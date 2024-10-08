/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.plugin.tests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static java.lang.System.lineSeparator;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * These tests verify that actions performed by the Maven Plugin have succeeded.
 * <p>
 * Make sure you've built the module (and its dependencies) first, from Maven! The files we're testing are created during build, not test execution
 */
public class Test5ContextLoaderTest {
    @Test
    public void shouldHaveCreatedSimpleTemplatedFileUsingContextLoader() throws IOException {
        String      expectedContent = "Test-5: Test mustache file: key1=value1; key2=value2; cheese=brie; fruit=apple; TEST FUNCTION" + lineSeparator();
        InputStream input           = getClass().getResourceAsStream("/templates-out/test5/test-5a.txt");
        assertThat(input).isNotNull();

        String content = IOUtils.toString(input, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(expectedContent);
    }

    @Test
    public void shouldHaveCreatedTemplatedFileUsingContextLoaderWithScopeOverride() throws IOException {
        String      expectedContent = "Test-5: Test mustache file: key1=value1; key2=yale; cheese=cheddar; fruit=banana; TEST FUNCTION" + lineSeparator();
        InputStream input           = getClass().getResourceAsStream("/templates-out/test5/test-5b.txt");
        assertThat(input).isNotNull();

        String content = IOUtils.toString(input, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(expectedContent);
    }
}
