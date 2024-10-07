/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.contextloader;

import java.util.List;

import net.zscript.maven.templating.contextloader.LoadableEntities.LoadedEntityContent;

/**
 * This specifies a user-defined mapper that takes requested file/URI paths, and loads (or otherwise produces) a set of context objects to be given to the templating transform
 * process to populate the references in the template.
 */
public interface TemplatingPluginContextLoader {

    /**
     * Performs the required loading and mapping of requested file paths into actual content that can given to the Mustache template system. This method is responsible for the
     * following actions:
     * <ol>
     *     <li>reading the {@link net.zscript.maven.templating.contextloader.LoadableEntities.LoadableEntity} path information in order to decide what to load (eg to load some
     *     JSON files)</li>
     *     <li>loading that data as required</li>
     *     <li>performing any required transformation on the loaded data, possibly by using the path information to load other files (eg to perform file-inclusion, or rename inconvenient fields)</li>
     *     <li>creating any other helper objects that may be required during the templating process (note that Mustache is unable to do any logic, but helper objects can)</li>
     *     <li>determining the output filename (relative to the plugin's outputDirectory) for the resulting template transformation, including any suffix</li>
     * </ol>
     * There is no requirement for the number of LoadedEntityContent objects to equal the input files - any LoadedEntityContent will be templated and stored in its output location.
     *
     * @param entities the entities to map
     * @return a list of mapped contexts to be transformed
     */
    List<LoadedEntityContent> loadAndMap(LoadableEntities entities);
}
