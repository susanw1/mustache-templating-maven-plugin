package net.zscript.maven.templating.plugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.zscript.maven.templating.plugin.TemplatingBaseMojoTestSupport.createMustacheResolver;
import static net.zscript.maven.templating.plugin.TemplatingBaseMojoTestSupport.newMojo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.resolver.FileSystemResolver;
import org.junit.jupiter.api.Test;

class TemplatingBaseMojoFileResolverTest {
    @Test
    void shouldUseTheFirstDefaultRootContainingTheMainTemplate() throws Exception {
        final Path projectDirectory = Files.createTempDirectory("mustache-project");
        try {
            final Path templateDirectory = projectDirectory.resolve("src/main/templates");
            writeTemplate(templateDirectory.resolve("main.mustache"));
            final TemplatingMojo mojo = newMojo(projectDirectory, "", "main.mustache");
            mojo.templateRootDirs.add("missing-templates");
            mojo.templateRootDirs.add("src/main/templates");

            assertThat(createMustacheResolver(mojo)).isInstanceOf(FileSystemResolver.class);
        } finally {
            deleteTree(projectDirectory);
        }
    }

    @Test
    void shouldUseAnAbsoluteTemplateDirectory() throws Exception {
        final Path projectDirectory = Files.createTempDirectory("mustache-project");
        try {
            final Path templateDirectory = projectDirectory.resolve("absolute-templates");
            writeTemplate(templateDirectory.resolve("main.mustache"));
            final TemplatingMojo mojo = newMojo(projectDirectory, templateDirectory.toString(), "main.mustache");

            assertThat(createMustacheResolver(mojo)).isInstanceOf(FileSystemResolver.class);
        } finally {
            deleteTree(projectDirectory);
        }
    }

    @Test
    void shouldUseAProjectRelativeTemplateDirectory() throws Exception {
        final Path projectDirectory = Files.createTempDirectory("mustache-project");
        try {
            writeTemplate(projectDirectory.resolve("custom-templates/main.mustache"));
            final TemplatingMojo mojo = newMojo(projectDirectory, "custom-templates", "main.mustache");

            assertThat(createMustacheResolver(mojo)).isInstanceOf(FileSystemResolver.class);
        } finally {
            deleteTree(projectDirectory);
        }
    }

    @Test
    void shouldRejectAnEmptyTemplateDirectoryWhenNoDefaultRootContainsTheMainTemplate() throws Exception {
        final Path projectDirectory = Files.createTempDirectory("mustache-project");
        try {
            final TemplatingMojo mojo = newMojo(projectDirectory, "", "main.mustache");
            mojo.templateRootDirs.add("missing-templates");

            assertThatThrownBy(() -> createMustacheResolver(mojo))
                    .isInstanceOf(TemplatingBaseMojo.TemplatingMojoFailureException.class)
                    .hasMessageContaining("Cannot locate template");
        } finally {
            deleteTree(projectDirectory);
        }
    }

    @Test
    void shouldFallBackToAConfiguredRootForAProjectRelativeTemplateDirectory() throws Exception {
        final Path projectDirectory = Files.createTempDirectory("mustache-project");
        try {
            writeTemplate(projectDirectory.resolve("src/main/templates/custom/templates/main.mustache"));
            final TemplatingMojo mojo = newMojo(projectDirectory, "custom/templates", "main.mustache");
            mojo.templateRootDirs.add("src/main/templates");

            assertThat(createMustacheResolver(mojo)).isInstanceOf(FileSystemResolver.class);
        } finally {
            deleteTree(projectDirectory);
        }
    }

    @Test
    void shouldRejectAnAbsoluteDirectoryWithoutTheMainTemplate() throws Exception {
        final Path projectDirectory = Files.createTempDirectory("mustache-project");
        try {
            final Path templateDirectory = Files.createDirectory(projectDirectory.resolve("templates"));
            final TemplatingMojo mojo = newMojo(projectDirectory, templateDirectory.toString(), "main.mustache");

            assertThatThrownBy(() -> createMustacheResolver(mojo))
                    .isInstanceOf(TemplatingBaseMojo.TemplatingMojoFailureException.class)
                    .hasMessageContaining("Cannot locate template");
        } finally {
            deleteTree(projectDirectory);
        }
    }

    @Test
    void shouldRejectAMissingAbsoluteTemplateDirectory() throws Exception {
        final Path projectDirectory = Files.createTempDirectory("mustache-project");
        try {
            final TemplatingMojo mojo = newMojo(projectDirectory, projectDirectory.resolve("missing").toString(), "main.mustache");

            assertThatThrownBy(() -> createMustacheResolver(mojo))
                    .isInstanceOf(TemplatingBaseMojo.TemplatingMojoFailureException.class)
                    .hasMessageContaining("Cannot locate template");
        } finally {
            deleteTree(projectDirectory);
        }
    }

    @Test
    void shouldRejectAWindowsDrivePathWhenItCannotBeResolved() throws Exception {
        final Path projectDirectory = Files.createTempDirectory("mustache-project");
        try {
            final TemplatingMojo mojo = newMojo(projectDirectory, "C:\\templates", "main.mustache");

            assertThatThrownBy(() -> createMustacheResolver(mojo))
                    .isInstanceOf(TemplatingBaseMojo.TemplatingMojoFailureException.class)
                    .hasMessageContaining("Cannot locate template");
        } finally {
            deleteTree(projectDirectory);
        }
    }

    private static void writeTemplate(Path path) throws Exception {
        Files.createDirectories(path.getParent());
        Files.write(path, "template\n".getBytes(StandardCharsets.UTF_8));
    }

    private static void deleteTree(Path directory) throws Exception {
        try (java.util.stream.Stream<Path> paths = Files.walk(directory)) {
            paths.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
