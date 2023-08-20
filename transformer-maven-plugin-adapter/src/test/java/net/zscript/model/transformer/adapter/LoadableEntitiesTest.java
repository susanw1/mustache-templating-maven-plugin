package net.zscript.model.transformer.adapter;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import net.zscript.model.transformer.adapter.LoadableEntities.LoadedEntityContent;

class LoadableEntitiesTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    @Test
    void shouldProductListOfLoadedEntities() throws URISyntaxException {
        URI          rootPath = new URI("file:///foo/");
        List<String> relPaths = List.of("bar", "baz/");
        String       suffix   = "java";

        LoadableEntities le = new LoadableEntities("desc", rootPath, relPaths, suffix);

        final List<LoadedEntityContent> result = le.loadEntities(entity -> {
            return singletonList(entity.withContents(
                    List.of("content+" + entity.getRelativePath()),
                    fs.getPath("a").resolve(entity.getRelativePath())));
        });

        assertThat(result)
                .hasSize(2)
                .extracting(LoadedEntityContent::getContents,
                        LoadedEntityContent::getRelativeOutputPath,
                        LoadedEntityContent::getDescription,
                        LoadedEntityContent::getRelativePath,
                        LoadedEntityContent::getRootPath,
                        LoadedEntityContent::getFullPath)
                .containsExactly(
                        tuple(List.of("content+bar"), fs.getPath("a/bar"), "desc", "bar", new URI("file:///foo/"), new URI("file:///foo/bar")),
                        tuple(List.of("content+baz/"), fs.getPath("a/baz/"), "desc", "baz/", new URI("file:///foo/"), new URI("file:///foo/baz/")));
    }

    @Test
    void shouldRejectAbsoluteEntityPath() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            new LoadableEntities("desc", new URI("/foo/"), List.of("/bar"), "java").loadEntities(e -> List.of());
        }).withMessageStartingWith("relativePath is absolute");
    }

    @Test
    void shouldRejectAbsoluteOutputPath() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            new LoadableEntities("desc", new URI("/foo/"), List.of("bar"), "java")
                    .loadEntities(e -> List.of(e.withContents(List.of(""), fs.getPath("/baz"))));
        }).withMessageStartingWith("relativeOutputPath is absolute");
    }
}
