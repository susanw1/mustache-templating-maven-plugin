/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.contextloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

import org.yaml.snakeyaml.Yaml;

/**
 * This is the default ContextLoader implementation, which loads context content from some specified Json/YAML files, and presents their content for templating.
 */
public class YamlTemplatingPluginContextLoader implements TemplatingPluginContextLoader {
    private final Yaml yamlMapper = new Yaml();

    @Override
    public List<LoadableEntities.LoadedEntityScopes> loadAndMap(LoadableEntities entities) {
        return entities.loadEntities(this::load);
    }

    private List<LoadableEntities.LoadedEntityScopes> load(LoadableEntities.LoadableEntity entity) {
        final String relativePathToSource = entity.getRelativePath();

        // Figure out the (relative) output filename
        final int dotIndex = relativePathToSource.lastIndexOf('.');
        final String newUriPath = (dotIndex != -1 ? relativePathToSource.substring(0, dotIndex) : relativePathToSource)
                + "." + entity.getFileTypeSuffix();
        final Path relativePathToOutput = entity.getFileSystem().getPath(newUriPath);

        try (Reader r = new BufferedReader(new InputStreamReader(entity.getFullPathAsUrl().openStream(), UTF_8))) {
            final Map<?, ?> value = yamlMapper.load(r);
            return singletonList(entity.withScopes(singletonList(value), relativePathToOutput));
        } catch (NullPointerException ex) {
            throw new UncheckedIOException(new IOException("Failed to read from: " + entity.getFullPath(), ex));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
