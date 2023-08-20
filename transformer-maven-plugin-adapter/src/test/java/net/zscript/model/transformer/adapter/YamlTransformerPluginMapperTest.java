package net.zscript.model.transformer.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import net.zscript.model.transformer.adapter.LoadableEntities.LoadedEntityContent;

class YamlTransformerPluginMapperTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    @Disabled
    @Test
    public void shouldProductListOfLoadedEntities() throws IOException, URISyntaxException {
        URI          rootPath = new URI("/foo");
        List<String> relPaths = List.of("baz/a.yaml");
        String       suffix   = "java";

        LoadableEntities            le     = new LoadableEntities("desc", rootPath, relPaths, suffix);
        YamlTransformerPluginMapper mapper = new YamlTransformerPluginMapper();

        final Path yamlFile = fs.getPath("/foo", "baz", "a.yaml");
        Files.createDirectories(yamlFile.getParent());
        try (final BufferedWriter w = Files.newBufferedWriter(yamlFile)) {
            w.write("{a : w1, b: [x,y], c: w3}");
        }

        List<LoadedEntityContent> loadedEntities = mapper.loadAndMap(le);

        assertThat(loadedEntities).hasSize(1);
        final List<Object> content = loadedEntities.get(0).getContents();
        final Map<?, ?>    context = (Map<?, ?>) content.get(0);
        assertThat(context.get("a")).isEqualTo("w1");
        assertThat(context.get("b")).isInstanceOf(List.class).isEqualTo(List.of("x", "y"));
        assertThat(context.get("c")).isEqualTo("w3");

        assertThat(loadedEntities.get(0).getRelativeOutputPath()).isEqualTo(fs.getPath("baz/a.java"));
    }
}
