package net.zscript.model.transformer.plugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The entry class for the maven plugin
 */
@Mojo(name = "transform", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class TransformerMojo extends TransformerBaseMojo {

    private static final String TEMPLATE_DEFAULT_DIR = "src/main/templates";
    private static final String CONTEXT_DEFAULT_DIR  = "src/main/contexts";
    private static final String OUTPUT_DEFAULT_DIR   = "generated-sources/java";

    /**
     * Specify output directory where the transformed output files are placed. This is added to the Maven Compile Source Root list if the {@code fileTypeSuffix} is "java".
     */
    @Parameter
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoExecutionException {
        String outputDirectoryPath = executeBase(TEMPLATE_DEFAULT_DIR, CONTEXT_DEFAULT_DIR, OUTPUT_DEFAULT_DIR);
        getLog().info("Main: generateSources: " + generateSources + ", fileTypeSuffix: " + fileTypeSuffix + ", outputDirectoryPath: " + outputDirectoryPath);
        if (outputDirectoryPath != null) {
            project.addCompileSourceRoot(outputDirectoryPath);
        }
    }
}
