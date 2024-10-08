package net.zscript.maven.templating.contextloader;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

class TemplatingPluginContextLoaderTest {
    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    @Test
    public void shouldConstructDefaultOutputPath() {
        TemplatingPluginContextLoader loader = entities -> null;

        Path result = loader.createDefaultOutputPath("this/that.x", "abc", fs);
        assertThat(result.toString()).isEqualTo("this/that.abc");
    }

    @Test
    public void shouldConstructDefaultOutputPathWhenNoSuffix() {
        TemplatingPluginContextLoader loader = entities -> null;

        Path result = loader.createDefaultOutputPath("this/that", "abc", fs);
        assertThat(result.toString()).isEqualTo("this/that.abc");
    }
}
