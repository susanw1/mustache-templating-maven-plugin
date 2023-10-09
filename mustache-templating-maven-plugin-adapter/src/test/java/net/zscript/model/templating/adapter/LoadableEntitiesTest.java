package net.zscript.model.templating.adapter;

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

import net.zscript.model.templating.adapter.LoadableEntities.LoadedEntityContent;

class LoadableEntitiesTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    @Test
    void shouldProductListOfLoadedEntities() throws URISyntaxException {
        final URI          rootPath = new URI("file:///foo/");
        final List<String> relPaths = Arrays.asList("bar", "baz/");
        final String       suffix   = "java";

        final LoadableEntities le = new LoadableEntities("desc", rootPath, relPaths, suffix, fs);

        final List<LoadedEntityContent> result = le.loadEntities(
                entity -> singletonList(entity.withContents(
                        singletonList("content+" + entity.getRelativePath()),
                        fs.getPath("a").resolve(entity.getRelativePath())))
        );

        assertThat(result)
                .hasSize(2)
                .extracting(LoadedEntityContent::getContents,
                        LoadedEntityContent::getRelativeOutputPath,
                        LoadedEntityContent::getDescription,
                        LoadedEntityContent::getRelativePath,
                        LoadedEntityContent::getRootPath,
                        LoadedEntityContent::getFullPath)
                .containsExactly(
                        tuple(singletonList("content+bar"), fs.getPath("a/bar"), "desc", "bar", new URI("file:///foo/"), new URI("file:///foo/bar")),
                        tuple(singletonList("content+baz/"), fs.getPath("a/baz/"), "desc", "baz/", new URI("file:///foo/"), new URI("file:///foo/baz/")));
    }

    @Test
    void shouldRejectAbsoluteEntityPath() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new LoadableEntities("desc", new URI("/foo/"), singletonList("/bar"), "java", fs)
                        .loadEntities(e -> emptyList()))
                .withMessageStartingWith("relativePath is absolute");
    }

    @Test
    void shouldRejectAbsoluteOutputPath() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new LoadableEntities("desc", new URI("/foo/"), singletonList("bar"), "java", fs)
                        .loadEntities(e -> singletonList(e.withContents(singletonList(""), fs.getPath("/baz")))))
                .withMessageStartingWith("relativeOutputPath is absolute");
    }
}