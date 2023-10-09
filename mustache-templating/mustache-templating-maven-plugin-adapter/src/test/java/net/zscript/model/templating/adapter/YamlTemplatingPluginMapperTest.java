package net.zscript.model.templating.adapter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

class YamlTemplatingPluginMapperTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    @Test
    public void shouldProductListOfLoadedEntities() throws IOException {
        // Dir must exist before creating URI, otherwise no trailing '/', https://github.com/google/jimfs/issues/16
        final Path rootDirPath = Files.createDirectory(fs.getPath("/foo"));

        final URI          rootPathUri = rootDirPath.toUri();
        final List<String> relPaths    = List.of("baz/a.yaml");
        final String       suffix      = "java";

        final LoadableEntities           le     = new LoadableEntities("desc", rootPathUri, relPaths, suffix, fs);
        final YamlTemplatingPluginMapper mapper = new YamlTemplatingPluginMapper();

        final Path yamlFile = fs.getPath("/foo", "baz", "a.yaml");
        Files.createDirectories(yamlFile.getParent());
        try (final BufferedWriter w = Files.newBufferedWriter(yamlFile)) {
            w.write("{a : w1, b: [x,y], c: w3}");
        }

        final List<LoadableEntities.LoadedEntityContent> loadedEntities = mapper.loadAndMap(le);

        assertThat(loadedEntities).hasSize(1);
        final List<Object> content = loadedEntities.get(0).getContents();
        final Map<?, ?>    context = (Map<?, ?>) content.get(0);
        assertThat(context.get("a")).isEqualTo("w1");
        assertThat(context.get("b")).isInstanceOf(List.class).isEqualTo(List.of("x", "y"));
        assertThat(context.get("c")).isEqualTo("w3");

        assertThat(loadedEntities.get(0).getRelativeOutputPath()).isEqualTo(fs.getPath("baz/a.java"));
    }
}
