package net.zscript.model.templating.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.yaml.snakeyaml.Yaml;

public class YamlTemplatingPluginMapper implements TemplatingPluginMapper {
    private final Yaml yamlMapper = new Yaml();

    @Override
    public List<LoadableEntities.LoadedEntityContent> loadAndMap(LoadableEntities entities) {
        return entities.loadEntities(this::load);
    }

    private List<LoadableEntities.LoadedEntityContent> load(LoadableEntities.LoadableEntity entity) {
        final String relativePathToSource = entity.getRelativePath();

        final int dotIndex = relativePathToSource.lastIndexOf('.');
        final String newUriPath = (dotIndex != -1 ? relativePathToSource.substring(0, dotIndex) : relativePathToSource)
                + "." + entity.getFileTypeSuffix();
        final Path relativePathToOutput = entity.getFileSystem().getPath(newUriPath);

        try (Reader r = new BufferedReader(new InputStreamReader(entity.getFullPathAsUrl().openStream(), UTF_8))) {
            final Map<?, ?> value = yamlMapper.load(r);
            return List.of(entity.withContents(List.of(value), relativePathToOutput));
        } catch (NullPointerException ex) {
            throw new UncheckedIOException(new IOException("Failed to read from: " + entity.getFullPath(), ex));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
