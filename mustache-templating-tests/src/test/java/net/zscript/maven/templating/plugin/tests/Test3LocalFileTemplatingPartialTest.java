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
 * These tests verify that actions performed by the Maven Plugin have succeeded. Make sure you've built the module (and its dependencies) first!
 */
public class Test3LocalFileTemplatingPartialTest {
    @Test
    public void shouldCreateFileUsingPartials() throws IOException {
        String expectedContent = "Test-3: Test mustache file: receipt is Oz-Ware Purchase Invoice for Dorothy Gale" + lineSeparator()
                + "Partial#1: Test-3a: Bill-to: 123 Tornado Alley&#10;Suite 16&#10;" + lineSeparator() + lineSeparator()
                + "Partial#2: Test-3b: - Ship-to: 123 Tornado Alley&#10;Suite 16&#10;" + lineSeparator() + lineSeparator();

        InputStream input = getClass().getResourceAsStream("/templates-out/test3/exampleA-1.txt");
        assertThat(input).isNotNull();

        String content = IOUtils.toString(input, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(expectedContent);
    }
}
