package net.zscript.model.transformer.plugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The entry class for the test maven plugin
 */
@Mojo(name = "test-transform", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class TransformerTestMojo extends TransformerBaseMojo {

    private static final String TEMPLATE_DEFAULT_DIR = "src/test/templates";
    private static final String CONTEXT_DEFAULT_DIR  = "src/test/contexts";
    private static final String OUTPUT_DEFAULT_DIR   = "generated-test-sources/java";

    /**
     * Specify output directory where the transformed output files are placed. This is added to the Maven Compile Source Root list if the {@code fileTypeSuffix} is "java".
     */
    @Parameter
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoExecutionException {
        String outputDirectoryPath = executeBase(TEMPLATE_DEFAULT_DIR, CONTEXT_DEFAULT_DIR, OUTPUT_DEFAULT_DIR);
        getLog().info("Test: generateSources: " + generateSources + ", fileTypeSuffix: " + fileTypeSuffix + ", outputDirectoryPath: " + outputDirectoryPath);
        if (outputDirectoryPath != null) {
            project.addTestCompileSourceRoot(outputDirectoryPath);
        }
    }
}
