package net.zscript.model.templating.plugin.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

import static java.lang.System.lineSeparator;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

public class Test4MustachePartialTest {
    @Test
    public void shouldRenderClasspathTemplatesWithPartials() throws IOException {
        final String expected = "Test-4 (classpath, partials): Test mustache file: receipt is Classpath example for Joe Bloggs" + lineSeparator()
                + "Testing partials:" + lineSeparator()
                + "foo-4.mustache: - Ship-to: 31 Cat Road&#10;Suite 16&#10;" + lineSeparator() + lineSeparator();

        final URL contextUrl = getClass().getResource("/contexts-cp/example-2.yaml");
        System.out.println("contextUrl : " + contextUrl);
        final InputStream contextStream = contextUrl.openStream();
        assertThat(contextStream).isNotNull();

        final Yaml yamlMapper = new Yaml();

        final DefaultMustacheFactory mustacheFactory = new DefaultMustacheFactory("template-cp");
        final Mustache               mustache        = mustacheFactory.compile("test-4-cp-part.mustache");
        final StringWriter           writer          = new StringWriter();
        final Map<?, ?>              context         = yamlMapper.load(contextStream);
        mustache.execute(writer, context);

        assertThat(writer.toString()).isEqualTo(expected);
    }
}
