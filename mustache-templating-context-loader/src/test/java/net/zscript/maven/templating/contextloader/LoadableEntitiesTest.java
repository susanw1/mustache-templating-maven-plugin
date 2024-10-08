/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.contextloader;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

class LoadableEntitiesTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    @Test
    void shouldProductListOfLoadedEntities() throws URISyntaxException {
        final URI          rootPath = new URI("file:///foo/");
        final List<String> relPaths = Arrays.asList("bar", "baz/");
        final String       suffix   = "java";

        final LoadableEntities le = new LoadableEntities(rootPath, relPaths, suffix, fs);

        final List<LoadableEntities.LoadedEntityScopes> result = le.loadEntities(
                entity -> singletonList(entity.withScopes(
                        singletonList("content+" + entity.getRelativePath()),
                        fs.getPath("a").resolve(entity.getRelativePath())))
        );

        assertThat(result)
                .hasSize(2)
                .extracting(LoadableEntities.LoadedEntityScopes::getScopes,
                        LoadableEntities.LoadedEntityScopes::getRelativeOutputPath,
                        LoadableEntities.LoadedEntityScopes::getRelativePath,
                        LoadableEntities.LoadedEntityScopes::getRootPath,
                        LoadableEntities.LoadedEntityScopes::getFullPath)
                .containsExactly(
                        tuple(singletonList("content+bar"), fs.getPath("a/bar"), "bar", new URI("file:///foo/"), new URI("file:///foo/bar")),
                        tuple(singletonList("content+baz/"), fs.getPath("a/baz/"), "baz/", new URI("file:///foo/"), new URI("file:///foo/baz/")));
    }

    @Test
    void shouldRejectAbsoluteEntityPath() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new LoadableEntities(new URI("/foo/"), singletonList("/bar"), "java", fs)
                        .loadEntities(e -> emptyList()))
                .withMessageStartingWith("relativePath is absolute");
    }

    @Test
    void shouldRejectAbsoluteOutputPath() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new LoadableEntities(new URI("/foo/"), singletonList("bar"), "java", fs)
                        .loadEntities(e -> singletonList(e.withScopes(singletonList(""), fs.getPath("/baz")))))
                .withMessageStartingWith("relativeOutputPath is absolute");
    }

    @Test
    void shouldRejectNonDirectoryRootPath() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new LoadableEntities(new URI("/foo"), singletonList("bar"), "java", fs))
                .withMessageStartingWith("Invalid directory URI");
    }
}
