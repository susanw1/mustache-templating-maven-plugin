/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2026 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.plugin;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

class TemplatingBaseMojoOutputPathTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

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
