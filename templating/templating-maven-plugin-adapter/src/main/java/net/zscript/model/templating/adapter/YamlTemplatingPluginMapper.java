package net.zscript.model.templating.adapter;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class YamlTemplatingPluginMapper implements TemplatingPluginMapper {
    private final Yaml yamlMapper = new Yaml();

    @Override
    public List<LoadableEntities.LoadedEntityContent> loadAndMap(LoadableEntities entities) {
        return entities.loadEntities(this::load);
    }

    private List<LoadableEntities.LoadedEntityContent> load(LoadableEntities.LoadableEntity entity) {
        final String relativePathToSource = entity.getRelativePath();
        final Path   relativePathToOutput = findRelativePathToOutput(relativePathToSource, entity.getFileTypeSuffix());

        try (final Reader r = Files.newBufferedReader(Path.of(entity.getFullPath()))) {
            final Map<?, ?> value = yamlMapper.load(r);
            return List.of(entity.withContents(List.of(value), relativePathToOutput));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
