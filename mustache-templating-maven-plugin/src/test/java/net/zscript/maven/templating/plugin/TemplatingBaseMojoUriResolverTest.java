package net.zscript.maven.templating.plugin;

import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static net.zscript.maven.templating.plugin.TemplatingBaseMojoTestSupport.createMustacheResolver;
import static net.zscript.maven.templating.plugin.TemplatingBaseMojoTestSupport.newMojo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.resolver.ClasspathResolver;
import org.junit.jupiter.api.Test;

class TemplatingBaseMojoUriResolverTest {
    @Test
    void shouldSelectFileUriResolverAndResolveTemplatesAndPartials() throws Exception {
        final Path templateDirectory = Files.createTempDirectory("mustache-templates");
        try {
            write(templateDirectory.resolve("main.mustache"), "Main: {{value}} / {{> partial.mustache}}\n");
            write(templateDirectory.resolve("partial.mustache"), "partial\n");

            final TemplatingMojo mojo = newMojo(templateDirectory, templateDirectory.toUri().toString(), "main.mustache");
            final MustacheResolver resolver = createMustacheResolver(mojo);
            final Mustache mustache = new DefaultMustacheFactory(resolver).compile("main.mustache");
            final StringWriter output = new StringWriter();
            mustache.execute(output, Collections.singletonMap("value", "value"));

            assertThat(output.toString()).isEqualTo("Main: value / partial\n\n");
        } finally {
            Files.deleteIfExists(templateDirectory.resolve("partial.mustache"));
            Files.deleteIfExists(templateDirectory.resolve("main.mustache"));
            Files.deleteIfExists(templateDirectory);
        }
    }

    @Test
    void shouldSelectClasspathResolverWithAndWithoutLeadingSlash() throws Exception {
        for (String templateDirectory : new String[] {"classpath:/template-cp", "classpath:template-cp"}) {
            final TemplatingMojo mojo = newMojo(Path.of("."), templateDirectory, "test-2-cp.mustache");
            final MustacheResolver resolver = createMustacheResolver(mojo);

            assertThat(resolver).isInstanceOf(ClasspathResolver.class);
            try (Reader reader = resolver.getReader("test-2-cp.mustache")) {
                assertThat(reader).isNotNull();
            }
        }
    }

    @Test
    void shouldResolveTemplatesAndPartialsFromOpaqueJarUri() throws Exception {
        final Path templatesJar = Files.createTempFile("mustache-templates", ".jar");
        try {
            try (OutputStream output = Files.newOutputStream(templatesJar); ZipOutputStream jar = new ZipOutputStream(output)) {
                addEntry(jar, "templates/main.mustache", "Main: {{value}} / {{> partial.mustache}}\n");
                addEntry(jar, "templates/partial.mustache", "partial\n");
            }

            final String templateDirectory = "jar:" + templatesJar.toUri() + "!/templates/";
            final TemplatingMojo mojo = newMojo(templatesJar.getParent(), templateDirectory, "main.mustache");
            final MustacheResolver resolver = createMustacheResolver(mojo);
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

    @Test
    void shouldRejectUriWhenMainTemplateCannotBeRead() throws Exception {
        final Path templateDirectory = Files.createTempDirectory("mustache-templates");
        try {
            final TemplatingMojo mojo = newMojo(templateDirectory, templateDirectory.toUri().toString(), "missing.mustache");

            assertThatThrownBy(() -> createMustacheResolver(mojo))
                    .isInstanceOf(TemplatingBaseMojo.TemplatingMojoFailureException.class)
                    .hasMessageContaining("Cannot locate template");
        } finally {
            Files.deleteIfExists(templateDirectory);
        }
    }

    @Test
    void shouldRejectMalformedTemplateDirectoryUri() throws Exception {
        final TemplatingMojo mojo = newMojo(Path.of("."), "file:/templates with spaces", "main.mustache");

        assertThatThrownBy(() -> createMustacheResolver(mojo))
                .isInstanceOf(TemplatingBaseMojo.TemplatingMojoFailureException.class)
                .hasMessageContaining("Bad template directory URI");
    }

    private static void write(Path path, String content) throws Exception {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private static void addEntry(ZipOutputStream jar, String name, String content) throws Exception {
        jar.putNextEntry(new ZipEntry(name));
        jar.write(content.getBytes(StandardCharsets.UTF_8));
        jar.closeEntry();
    }
}
