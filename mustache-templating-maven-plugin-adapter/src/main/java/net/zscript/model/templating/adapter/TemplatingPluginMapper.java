package net.zscript.model.templating.adapter;

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
    List<LoadedEntityContent> loadAndMap(LoadableEntities entities);
}
