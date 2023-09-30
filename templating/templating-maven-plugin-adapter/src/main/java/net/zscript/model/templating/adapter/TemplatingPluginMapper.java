package net.zscript.model.templating.adapter;

import java.nio.file.Path;
import java.util.List;

import net.zscript.model.templating.adapter.LoadableEntities.LoadedEntityContent;

/**
 * User-defined mapper to convert a requested file/URI content into the form expected by the transform template.
 */
public interface TemplatingPluginMapper {

    /**
     * Performs the required mapping.
     *
     * @param entities the entities to map
     * @return a list of mapped contexts to be transformed
     */
    List<LoadedEntityContent> loadAndMap(final LoadableEntities entities);

    /**
     * Utility method for converting a relative path with a dot-suffixed filename into the same path with a different suffix.
     *
     * @param relativePathToSource the path to convert
     * @param suffix               the suffix to add
     * @return the output path
     */
    default Path findRelativePathToOutput(final String relativePathToSource, String suffix) {
        final int    index      = relativePathToSource.lastIndexOf('.');
        final String newUriPath = (index != -1 ? relativePathToSource.substring(0, index) : relativePathToSource) + "." + suffix;
        return Path.of(newUriPath);
    }
}
