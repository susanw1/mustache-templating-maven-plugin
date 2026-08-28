/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.contextloader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

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

        try {
            final URL resourceUrl = entity.getFullPathAsUrl();
            if (resourceUrl == null) {
                throw new UncheckedIOException(new IOException("Context resource not found: " + entity.getFullPath()));
            }

            final Object value;
            try (Reader r = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
                value = yamlMapper.load(r);
            }

            if (!(value instanceof Map)) {
                final String valueType = value == null ? "null" : value.getClass().getName();
                throw new IllegalArgumentException("Context resource '" + entity.getFullPath()
                        + "' must contain a top-level mapping, but found " + valueType);
            }

            return singletonList(entity.withScopes(singletonList(value), relativePathToOutput));
        } catch (YAMLException ex) {
            throw new IllegalArgumentException("Malformed YAML in context resource '" + entity.getFullPath() + "'", ex);
        } catch (FileNotFoundException ex) {
            throw new UncheckedIOException(new IOException("Context resource not found: " + entity.getFullPath(), ex));
        } catch (IOException ex) {
            throw new UncheckedIOException(new IOException("Failed to read context resource '" + entity.getFullPath() + "'", ex));
        }
    }
}
