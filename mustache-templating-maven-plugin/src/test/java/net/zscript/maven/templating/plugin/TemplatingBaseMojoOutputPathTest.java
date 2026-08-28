/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2026 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.plugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.zscript.maven.templating.contextloader.YamlTemplatingPluginContextLoader;

class TemplatingBaseMojoOutputPathTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    @Test
    void shouldGenerateNestedOutputFile(@TempDir Path projectDirectory) throws Exception {
        Files.createDirectories(projectDirectory.resolve("src/main/templates"));
        Files.write(projectDirectory.resolve("src/main/templates/main.mustache"),
                "Generated: {{value}}".getBytes(StandardCharsets.UTF_8));

        final Path contextsDirectory = Files.createDirectories(projectDirectory.resolve("src/main/contexts/nested"));
        Files.write(contextsDirectory.resolve("example.yaml"), "value: valid\n".getBytes(StandardCharsets.UTF_8));

        final TemplatingMojo mojo = TemplatingBaseMojoTestSupport.newMojo(projectDirectory, null, "main.mustache");
        mojo.templateRootDirs.add("src/main/templates");
        mojo.contextLoaderClass = YamlTemplatingPluginContextLoader.class.getName();
        mojo.fileTypeSuffix = "txt";
        mojo.outputDirectory = projectDirectory.resolve("target/generated-text").toFile();

        final FileSet contextFileSet = new FileSet();
        contextFileSet.setDirectory(projectDirectory.resolve("src/main/contexts").toString());
        contextFileSet.setIncludes(Collections.singletonList("nested/example.yaml"));
        mojo.contexts = contextFileSet;

        assertThat(mojo.executeBase("src/main/contexts", "generated-sources")).isNull();
        assertThat(Files.readAllBytes(projectDirectory.resolve("target/generated-text/nested/example.txt")))
                .isEqualTo("Generated: valid".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldAllowLegitimateNestedOutputPath() throws Exception {
        final Path outputDirectory = Files.createDirectory(fs.getPath("/output"));

        assertThat(TemplatingBaseMojo.resolveOutputFile(outputDirectory, fs.getPath("nested/example.txt")))
                .isEqualTo(fs.getPath("/output/nested/example.txt"));
    }

    @Test
    void shouldRejectOutputPathThatTraversesOutsideOutputDirectory() throws Exception {
        final Path outputDirectory = Files.createDirectory(fs.getPath("/output"));

        assertThatThrownBy(() -> TemplatingBaseMojo.resolveOutputFile(outputDirectory, fs.getPath("../outside.txt")))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("escapes outputDirectory");
    }

    @Test
    void shouldRejectOutputPathThatTraversesSymlinkedDirectory() throws Exception {
        final Path outputDirectory = Files.createDirectory(fs.getPath("/output"));
        final Path outsideDirectory = Files.createDirectory(fs.getPath("/outside"));
        Files.createSymbolicLink(outputDirectory.resolve("nested"), outsideDirectory);

        assertThatThrownBy(() -> TemplatingBaseMojo.resolveOutputFile(outputDirectory, fs.getPath("nested/example.txt")))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("contains a symbolic link");
    }
}
