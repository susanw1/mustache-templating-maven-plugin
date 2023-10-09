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
public class Test2ClasspathTest {
    @Test
    public void shouldCreateOutputFile() throws IOException {
        String      expectedContent = "Test-2 (classpath): Test mustache file: receipt is Classpath example for Joe Bloggs" + lineSeparator();
        InputStream input           = getClass().getResourceAsStream("/templates-out/test2/example-2.txt");
        String      content         = IOUtils.toString(input, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo(expectedContent);
    }
}
