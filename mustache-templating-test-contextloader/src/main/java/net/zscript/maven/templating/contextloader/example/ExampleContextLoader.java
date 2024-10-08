/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2024 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.contextloader.example;

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
 * This is an example ContextLoader implementation. It loads the specified files as a Properties file (remember those?) and adds a Helper object with some handy stuff.
 */
public class ExampleContextLoader implements TemplatingPluginContextLoader {

    /** Example map of extra items that can be accessed in the template */
    Map<String, Object> extra;

    public ExampleContextLoader() {
        extra = new HashMap<>();
        // We will be able to reference these in the template as {{key1}}, {{key2}}, and {{#exampleUpper}}text{{/exampleUpper}}
        extra.put("key1", "value1");
        extra.put("key2", "value2");
        extra.put("exampleUpper", (Function<String, String>) String::toUpperCase);
    }

    @Override
    public List<LoadableEntities.LoadedEntityScopes> loadAndMap(LoadableEntities entities) {
        return entities.loadEntities(this::load);
    }

    /**
     * This is executed once for each relative path in the plugin's configuration.
     *
     * @param entity a specific relative path to be loaded/processed
     * @return the resulting scopes representing possibly multiple template contexts
     */
    private List<LoadableEntities.LoadedEntityScopes> load(LoadableEntities.LoadableEntity entity) {
        final String relativePathToSource = entity.getRelativePath();

        final Path relativePathToOutput = createDefaultOutputPath(relativePathToSource, entity.getFileTypeSuffix(), entity.getFileSystem());
        try (Reader r = new BufferedReader(new InputStreamReader(entity.getFullPathAsUrl().openStream(), UTF_8))) {
            Properties props = new Properties();
            props.load(r);

            // This list of objects which get scanned right-to-left by the mustache template system
            final List<Object> scopes = Arrays.asList(extra, props);

            // in principle, this could return many LoadedEntityContent objects, and we'd apply the template to each of them.
            return singletonList(entity.withScopes(scopes, relativePathToOutput));
        } catch (NullPointerException ex) {
            throw new UncheckedIOException(new IOException("Failed to read from: " + entity.getFullPath(), ex));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
