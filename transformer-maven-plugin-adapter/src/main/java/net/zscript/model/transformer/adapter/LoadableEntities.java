package net.zscript.model.transformer.adapter;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class LoadableEntities {
    private final String       entityDescription;
    private final URI          rootPath;
    private final List<String> relativePaths;
    private final String       fileTypeSuffix;

    public LoadableEntities(String entityDescription, URI rootPath, List<String> relativePaths, String fileTypeSuffix) {
        this.entityDescription = entityDescription;
        if (!rootPath.getPath().endsWith("/")) {
            throw new IllegalArgumentException("Invalid directory URI - missing '/'? " + rootPath);
        }
        this.rootPath = rootPath;
        this.relativePaths = relativePaths;
        this.fileTypeSuffix = fileTypeSuffix;
    }

    public <T> List<LoadedEntityContent> loadEntities(Function<LoadableEntity, List<LoadedEntityContent>> loader) {
        return relativePaths.stream()
                .map(LoadableEntity::new)
                .map(loader::apply)
                .flatMap(list -> list.stream())
                .collect(toList());
    }

    public class LoadableEntity {
        private final String relativePath;

        public LoadableEntity(String relativePath) {
            if (Path.of(relativePath).isAbsolute()) {
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

        public URI getFullPath() {
            return rootPath.resolve(relativePath);
        }

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
