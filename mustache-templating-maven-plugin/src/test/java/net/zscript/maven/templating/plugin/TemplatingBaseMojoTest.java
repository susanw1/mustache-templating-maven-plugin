package net.zscript.maven.templating.plugin;

import java.io.Reader;
import java.io.StringWriter;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheResolver;
import org.junit.jupiter.api.Test;

class TemplatingBaseMojoTest {
    @Test
    void shouldResolveTemplatesAndPartialsFromOpaqueJarUri() throws Exception {
        final Path templatesJar = Files.createTempFile("mustache-templates", ".jar");
        try {
            try (OutputStream output = Files.newOutputStream(templatesJar); ZipOutputStream jar = new ZipOutputStream(output)) {
                addEntry(jar, "templates/main.mustache", "Main: {{value}} / {{> partial.mustache}}\n");
                addEntry(jar, "templates/partial.mustache", "partial\n");
            }

            final TemplatingMojo mojo = new TemplatingMojo();
            mojo.mainTemplate = "main.mustache";
            final Method createUriResolver = TemplatingBaseMojo.class.getDeclaredMethod("createUriResolver", URI.class);
            createUriResolver.setAccessible(true);
            final MustacheResolver resolver = (MustacheResolver) createUriResolver.invoke(mojo,
                    URI.create("jar:" + templatesJar.toUri() + "!/templates/"));

            final Mustache mustache = new DefaultMustacheFactory(resolver).compile("main.mustache");
            final StringWriter output = new StringWriter();
            mustache.execute(output, Collections.singletonMap("value", "value"));

            assertThat(output.toString()).isEqualTo("Main: value / partial\n\n");
            try (Reader partial = resolver.getReader("partial.mustache")) {
                final StringWriter partialOutput = new StringWriter();
                partial.transferTo(partialOutput);
                assertThat(partialOutput.toString()).isEqualTo("partial\n");
            }
        } finally {
            Files.deleteIfExists(templatesJar);
        }
    }

    private static void addEntry(ZipOutputStream jar, String name, String content) throws Exception {
        jar.putNextEntry(new ZipEntry(name));
        jar.write(content.getBytes(StandardCharsets.UTF_8));
        jar.closeEntry();
    }
}
