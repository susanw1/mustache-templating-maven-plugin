package net.zscript.model.templating.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The entry class for the maven plugin
 */
@Mojo(name = "transform", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class TemplatingMojo extends TemplatingBaseMojo {

    static final String TEMPLATE_DEFAULT_DIR = "src/main/templates";
    static final String CONTEXT_DEFAULT_DIR  = "src/main/contexts";
    static final String OUTPUT_DEFAULT_DIR   = "generated-sources/java";

    /**
     * Specify output directory where the transformed output files are placed. This is added to the Maven Compile Source Root list if the {@code fileTypeSuffix} is "java".
     */

    @Override
    public void execute() throws MojoExecutionException {
        templateRootDirs.add(TEMPLATE_DEFAULT_DIR);
        String outputDirectoryPath = executeBase(CONTEXT_DEFAULT_DIR, OUTPUT_DEFAULT_DIR);
        getLog().info("Main: generateSources: " + generateSources + ", fileTypeSuffix: " + fileTypeSuffix + ", outputDirectoryPath: " + outputDirectoryPath);
        if (outputDirectoryPath != null) {
            project.addCompileSourceRoot(outputDirectoryPath);
        }
    }
}
