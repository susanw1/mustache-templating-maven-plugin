package net.zscript.maven.templating.plugin;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Collections;

import static net.zscript.maven.templating.plugin.TemplatingBaseMojoTestSupport.extractContextFileList;
import static net.zscript.maven.templating.plugin.TemplatingBaseMojoTestSupport.newMojo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.zscript.maven.templating.contextloader.LoadableEntities;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplatingBaseMojoContextFileSetTest {
    @Test
    void shouldRejectEmptyFileUrlFilesetWhenFailIfNoFilesIsTrue(@TempDir Path directory) throws Exception {
        final TemplatingMojo mojo = newMojo(directory, null, "main.mustache");
        mojo.failIfNoFiles = true;

        assertThatThrownBy(() -> extractContextFileList(mojo, emptyFileSet(directory.toUri().toString())))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(MojoExecutionException.class);
    }

    @Test
    void shouldAllowEmptyFileUrlFilesetWhenFailIfNoFilesIsFalse(@TempDir Path directory) throws Exception {
        final TemplatingMojo mojo = newMojo(directory, null, "main.mustache");
        mojo.failIfNoFiles = false;

        final LoadableEntities entities = extractContextFileList(mojo, emptyFileSet(directory.toUri().toString()));

        assertThat(entities.loadEntities(entity -> Collections.emptyList())).isEmpty();
    }

    @Test
    void shouldRejectEmptyClasspathFilesetWhenFailIfNoFilesIsTrue(@TempDir Path directory) throws Exception {
        final TemplatingMojo mojo = newMojo(directory, null, "main.mustache");
        mojo.failIfNoFiles = true;

        assertThatThrownBy(() -> extractContextFileList(mojo, emptyFileSet("classpath:/contexts-cp/")))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(MojoExecutionException.class);
    }

    @Test
    void shouldAllowEmptyClasspathFilesetWhenFailIfNoFilesIsFalse(@TempDir Path directory) throws Exception {
        final TemplatingMojo mojo = newMojo(directory, null, "main.mustache");
        mojo.failIfNoFiles = false;

        final LoadableEntities entities = extractContextFileList(mojo, emptyFileSet("classpath:/contexts-cp/"));

        assertThat(entities.loadEntities(entity -> Collections.emptyList())).isEmpty();
    }

    private static FileSet emptyFileSet(String directory) {
        final FileSet fileSet = new FileSet();
        fileSet.setDirectory(directory);
        return fileSet;
    }
}
