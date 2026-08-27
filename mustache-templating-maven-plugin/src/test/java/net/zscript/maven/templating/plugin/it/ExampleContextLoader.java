/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2024 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.plugin.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

import net.zscript.maven.templating.contextloader.LoadableEntities;
import net.zscript.maven.templating.contextloader.TemplatingPluginContextLoader;

/**
 * Test fixture context loader used by the Invoker integration tests.
 */
public class ExampleContextLoader implements TemplatingPluginContextLoader {

    private final Map<String, Object> extra = new HashMap<>();

    public ExampleContextLoader() {
        extra.put("key1", "value1");
        extra.put("key2", "value2");
        extra.put("exampleUpper", (Function<String, String>) String::toUpperCase);
    }

    @Override
    public List<LoadableEntities.LoadedEntityScopes> loadAndMap(LoadableEntities entities) {
        return entities.loadEntities(this::load);
    }

    private List<LoadableEntities.LoadedEntityScopes> load(LoadableEntities.LoadableEntity entity) {
        final String relativePathToSource = entity.getRelativePath();
        final Path relativePathToOutput = createDefaultOutputPath(
                relativePathToSource, entity.getFileTypeSuffix(), entity.getFileSystem());

        try (Reader reader = new BufferedReader(new InputStreamReader(
                entity.getFullPathAsUrl().openStream(), UTF_8))) {
            Properties properties = new Properties();
            properties.load(reader);

            return singletonList(entity.withScopes(Arrays.asList(extra, properties), relativePathToOutput));
        } catch (NullPointerException ex) {
            throw new UncheckedIOException(new IOException("Failed to read from: " + entity.getFullPath(), ex));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
