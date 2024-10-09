/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.contextloader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

class YamlTemplatingPluginContextLoaderTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    final YamlTemplatingPluginContextLoader contextLoader = new YamlTemplatingPluginContextLoader();

    @Test
    public void shouldProduceListOfLoadedEntities() throws IOException {
        // Dir must exist before creating URI, otherwise no trailing '/', https://github.com/google/jimfs/issues/16
        final Path rootDirPath = Files.createDirectory(fs.getPath("/foo"));

        final URI          rootPathUri = rootDirPath.toUri();
        final List<String> relPaths    = singletonList("baz/a.yaml");
        final String       suffix      = "java";

        final LoadableEntities le = new LoadableEntities(rootPathUri, relPaths, suffix, fs);

        final Path yamlFile = fs.getPath("/foo", "baz", "a.yaml");
        Files.createDirectories(yamlFile.getParent());
        try (final BufferedWriter w = Files.newBufferedWriter(yamlFile)) {
            w.write("{a : w1, b: [x,y], c: w3}");
        }

        final List<LoadableEntities.LoadedEntityScopes> loadedEntities = contextLoader.loadAndMap(le);

        assertThat(loadedEntities).hasSize(1);
        final List<Object> contexts = loadedEntities.get(0).getScopes();
        final Map<?, ?>    context0 = (Map<?, ?>) contexts.get(0);
        assertThat(context0.get("a")).isEqualTo("w1");
        assertThat(context0.get("b")).isInstanceOf(List.class).isEqualTo(asList("x", "y"));
        assertThat(context0.get("c")).isEqualTo("w3");

        assertThat(loadedEntities.get(0).getRelativeOutputPath()).isEqualTo(fs.getPath("baz/a.java"));
    }

    @Test
    public void shouldFailWithNonexistentClasspathResource() throws IOException, URISyntaxException {
        final LoadableEntities le = new LoadableEntities(new URI("classpath:/"), singletonList("bar"), "java", fs);
        assertThatThrownBy(() -> contextLoader.loadAndMap(le)).isInstanceOf(UncheckedIOException.class).hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void shouldFailWithMissingFile() throws IOException, URISyntaxException {
        final LoadableEntities le = new LoadableEntities(new URI("file:/"), singletonList("bar"), "java", fs);
        assertThatThrownBy(() -> contextLoader.loadAndMap(le)).isInstanceOf(UncheckedIOException.class).hasCauseInstanceOf(IOException.class);
    }
}
