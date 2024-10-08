/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.contextloader;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

import net.zscript.maven.templating.contextloader.LoadableEntities.LoadedEntityScopes;

/**
 * This specifies a user-defined mapper that takes requested file/URI paths, and loads (or otherwise produces) a set of context objects to be given to the templating transform
 * process to populate the references in the template.
 */
public interface TemplatingPluginContextLoader {

    /**
     * Performs the required loading and mapping of requested file paths into actual context that can given to the Mustache template system. This method is responsible for the
     * following actions:
     * <ol>
     *     <li>reading the {@link net.zscript.maven.templating.contextloader.LoadableEntities.LoadableEntity} path information in order to decide what to load (eg to load some
     *     JSON files)</li>
     *     <li>loading that data as required</li>
     *     <li>performing any required transformation on the loaded data, possibly by using the path information to load other files (eg to perform file-inclusion, or rename inconvenient fields)</li>
     *     <li>creating any other helper objects that may be required during the templating process (note that Mustache is unable to do any logic, but helper objects can)</li>
     *     <li>determining the output filename (relative to the plugin's outputDirectory) for the resulting template transformation, including any suffix</li>
     * </ol>
     * There is no requirement for the number of LoadedEntityContext objects to equal the input files - any LoadedEntityContext will be templated and stored in its output location.
     *
     * @param entities the entities to map
     * @return a list of contexts ready for templating
     */
    List<LoadedEntityScopes> loadAndMap(LoadableEntities entities);

    /**
     * Handy utility method that removes an existing '.'-suffix, puts the new suffix on, and produces a Path to the result. It's probably what's needed for creating output file
     * paths.
     *
     * @param relativePathToSource the pathname to be edited
     * @param fileTypeSuffix       the new suffix
     * @param fs                   the FileSystem type  currently in use
     * @return a Path with the new suffix
     */
    default Path createDefaultOutputPath(String relativePathToSource, String fileTypeSuffix, FileSystem fs) {
        final int    dotIndex   = relativePathToSource.lastIndexOf('.');
        final String newUriPath = (dotIndex != -1 ? relativePathToSource.substring(0, dotIndex) : relativePathToSource) + "." + fileTypeSuffix;
        return fs.getPath(newUriPath);
    }
}
