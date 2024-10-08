/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.contextloader;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * This class captures the information from the plugin about how context input should be created, and how the output destination filenames should be constructed. It is created by
 * the plugin, and is supplied as input to the {@link TemplatingPluginContextLoader#loadAndMap(LoadableEntities)}.
 * <p>
 * The context input is likely to be loaded from (one or more) files, and ultimately comes from an ordered list of scopes which are used to populate the key fields in the Mustache
 * templates. Strictly, a scope doesn't have to be loaded from anywhere - it's just a collection of Objects which are probably derived from the relative paths.
 */
public class LoadableEntities {
    private final URI          rootPath;
    private final List<String> relativePaths;
    private final String       fileTypeSuffix;
    private final FileSystem   fileSystem;

    /**
     * This method is called by the plugin to describe the files that have been requested for loading the scopes for templating.
     *
     * @param rootPath       the directory URI (file, http(s) or classpath) to use for resolving the relative paths
     * @param relativePaths  the paths to the files to be tried by the {@link TemplatingPluginContextLoader contextLoader}
     * @param fileTypeSuffix the requested output filename suffix, helpful for defining the output filenames
     * @param fileSystem     the local FileSystem type, usually from {@link FileSystems#getDefault()} (mostly facilitating multiplatform testing)
     */
    public LoadableEntities(URI rootPath, List<String> relativePaths, String fileTypeSuffix, FileSystem fileSystem) {
        if (!rootPath.getPath().endsWith("/")) {
            throw new IllegalArgumentException("Invalid directory URI - missing '/'? " + rootPath);
        }
        this.rootPath = rootPath;
        this.relativePaths = relativePaths;
        this.fileTypeSuffix = fileTypeSuffix;
        this.fileSystem = fileSystem;
    }

    /**
     * This method wraps the relative paths as LoadableEntity objects (before loading) and performs the 'loader' function upon them, to produce (zero or more) LoadedEntityScopes -
     * the output of the Entity loading/transforming operation. Supplying this loader function is the core of writing a ContextLoader.
     *
     * @param loader a function for loading entities and supplying them (and their proposed output paths) in the form that Mustache can consume
     * @return the loaded entities, ready for Mustache
     */
    public List<LoadedEntityScopes> loadEntities(Function<LoadableEntity, List<LoadedEntityScopes>> loader) {
        return relativePaths.stream()
                .map(LoadableEntity::new)
                .map(loader)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    /**
     * A single loadable entity, representing one of the relative paths in the {@link LoadableEntities}.
     */
    public class LoadableEntity {
        private final String relativePath;

        public LoadableEntity(String relativePath) {
            if (fileSystem.getPath(relativePath).isAbsolute()) {
                throw new IllegalArgumentException("relativePath is absolute: " + relativePath);
            }
            this.relativePath = relativePath;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public URI getRootPath() {
            return rootPath;
        }

        public String getFileTypeSuffix() {
            return fileTypeSuffix;
        }

        /**
         * The filesystem type (eg Win, Unix) used by the plugin. This should be used for all filesystem operations, to facilitate testing - build tools do a lot of file/directory
         * handling, and it's easy to make them not work for certain OSs.
         *
         * @return the type of FileSystem currently in use
         */
        public FileSystem getFileSystem() {
            return fileSystem;
        }

        public URI getFullPath() {
            return rootPath.resolve(relativePath);
        }

        /**
         * Determines the usable URL from the rootPath and relativePath, including handling "classpath" scheme. Calling {@link URL#openStream()} should just work.
         *
         * @return a usable URL
         * @throws MalformedURLException if the resulting URL is malformed
         */
        public URL getFullPathAsUrl() throws MalformedURLException {
            final URI fullPath = getFullPath();
            return ("classpath".equals(fullPath.getScheme())) ? getClass().getResource(fullPath.getPath()) : fullPath.toURL();
        }

        /**
         * Takes a (list of) scope objects associated with this entity. In simple cases, a scope may just be the singular (parsed) file content, but it could also have other
         * objects to provide additional scopes to the templating.
         *
         * @param scopes                 list of scope objects, used in the templating process, searched right-to-left for references.
         * @param relativeOutputFilename the location where the result of the template operation should be written, relative to the plugin's specified output directory
         * @return the loaded entity
         */
        public LoadedEntityScopes withScopes(final List<Object> scopes, final Path relativeOutputFilename) {
            return new LoadedEntityScopes(relativePath, scopes, relativeOutputFilename);
        }
    }

    /**
     * A LoadableEntity that is augmented with post-load content(s) (to be used as templating scopes) and an output filename. It is created from a LoadableEntity by a loader
     * calling {@link LoadableEntity#withScopes(List, Path)}, to supply the extra information.
     */
    public class LoadedEntityScopes extends LoadableEntity {
        private final List<Object> scopes;
        private final Path         relativeOutputPath;

        private LoadedEntityScopes(String relativePath, List<Object> scopes, Path relativeOutputPath) {
            super(relativePath);

            this.scopes = scopes;

            if (relativeOutputPath.isAbsolute()) {
                throw new IllegalArgumentException("relativeOutputPath is absolute: " + relativeOutputPath);
            }
            this.relativeOutputPath = relativeOutputPath;
        }

        /**
         * @return the list of loaded scopes to be used as templating context (searched in right-to-left order)
         */
        public List<Object> getScopes() {
            return scopes;
        }

        /**
         * The location of the output file, to be created to hold the templated output. It will be created relative to the plugin's configured 'outputDirectory'.
         *
         * @return the relative path of the proposed output file
         */
        public Path getRelativeOutputPath() {
            return relativeOutputPath;
        }
    }
}
