package net.zscript.model.templating.plugin.tests;

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
public class Test1BasicTemplatingTest {
    @Test
    public void shouldCreateFirstOutputFile() throws IOException {
        String      expectedContent = "Test-1: Test mustache file: receipt is Oz-Ware Purchase Invoice for Dorothy Gale" + lineSeparator();
        InputStream input           = getClass().getResourceAsStream("/templates-out/test1/exampleA-1.txt");
        assertThat(input).isNotNull();

        String content = IOUtils.toString(input, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(expectedContent);
    }

    @Test
    public void shouldCreateSecondOutputFile() throws IOException {
        String      expectedContent = "Test-1: Test mustache file: receipt is Something Else for Joe Bloggs" + lineSeparator();
        InputStream input           = getClass().getResourceAsStream("/templates-out/test1/exampleB-1.txt");
        assertThat(input).isNotNull();

        String content = IOUtils.toString(input, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(expectedContent);
    }
}
