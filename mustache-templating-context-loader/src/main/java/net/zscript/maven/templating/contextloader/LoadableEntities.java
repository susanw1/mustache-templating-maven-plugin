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
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class LoadableEntities {
    private final String       entityDescription;
    private final URI          rootPath;
    private final List<String> relativePaths;
    private final String       fileTypeSuffix;

    private final FileSystem fileSystem;

    public LoadableEntities(String entityDescription, URI rootPath, List<String> relativePaths, String fileTypeSuffix, FileSystem fileSystem) {
        this.entityDescription = entityDescription;
        if (!rootPath.getPath().endsWith("/")) {
            throw new IllegalArgumentException("Invalid directory URI - missing '/'? " + rootPath);
        }
        this.rootPath = rootPath;
        this.relativePaths = relativePaths;
        this.fileTypeSuffix = fileTypeSuffix;
        this.fileSystem = fileSystem;
    }

    public List<LoadedEntityContent> loadEntities(Function<LoadableEntity, List<LoadedEntityContent>> loader) {
        return relativePaths.stream()
                .map(LoadableEntity::new)
                .map(loader)
                .flatMap(Collection::stream)
                .collect(toList());
    }

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

        public String getDescription() {
            return entityDescription;
        }

        public String getFileTypeSuffix() {
            return fileTypeSuffix;
        }

        public FileSystem getFileSystem() {
            return fileSystem;
        }

        public URI getFullPath() {
            return rootPath.resolve(relativePath);
        }

        public URL getFullPathAsUrl() throws MalformedURLException {
            final URI fullPath = getFullPath();
            return ("classpath".equals(fullPath.getScheme())) ? getClass().getResource(fullPath.getPath()) : fullPath.toURL();
        }

        /**
         * Takes a (list of) content objects associated with this entity. In simple cases, the content may just be the singular  (parsed) file content, but it could also have other
         * objects to provide additional context to the templating.
         *
         * @param contents               list of content objects
         * @param relativeOutputFilename the location where the result of the template operation be written, relative to the user's specified output directory
         * @return the loaded entity
         */
        public LoadedEntityContent withContents(final List<Object> contents, final Path relativeOutputFilename) {
            return new LoadedEntityContent(relativePath, contents, relativeOutputFilename);
        }
    }

    public class LoadedEntityContent extends LoadableEntity {
        private final List<Object> contents;
        private final Path         relativeOutputPath;

        public LoadedEntityContent(String relativePath, List<Object> contents, Path relativeOutputPath) {
            super(relativePath);

            this.contents = contents;

            if (relativeOutputPath.isAbsolute()) {
                throw new IllegalArgumentException("relativeOutputPath is absolute: " + relativeOutputPath);
            }
            this.relativeOutputPath = relativeOutputPath;
        }

        public List<Object> getContents() {
            return contents;
        }

        public Path getRelativeOutputPath() {
            return relativeOutputPath;
        }
    }
}
