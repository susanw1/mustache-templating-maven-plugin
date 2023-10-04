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
        final String expected = "Test-4 (classpath, partials): Test mustache file: receipt is Classpath example on Thu Mar 16 00:00:00 GMT 2023" + lineSeparator()
                + "Testing partials:" + lineSeparator()
                + "foo-4.mustache: - Ship-to: 31 Cat Road&#10;Suite 16&#10;" + lineSeparator() + lineSeparator();

        //        final String templateResource = "/template-cp/test-4-cp-part.mustache";
        //                final URL    templateUrl      = getClass().getResource(templateResource);

        //        final String templatePathStr
        //                = "jar:file:/home/users/bill/.m2/repository/net/zscript/templating-maven-plugin-tests/0.0.1-SNAPSHOT/templating-maven-plugin-tests-0.0.1-SNAPSHOT.jar"
        //                + "!/template-cp/test-4-cp-part.mustache";
        //        final String fooPathStr
        //                = "jar:file:/home/users/bill/.m2/repository/net/zscript/templating-maven-plugin-tests/0.0.1-SNAPSHOT/templating-maven-plugin-tests-0.0.1-SNAPSHOT.jar"
        //                + "!/template-cp/foo-4.mustache";

        //        final URL templateUrl = new URL(templatePathStr);
        final URL contextUrl = getClass().getResource("/contexts-cp/example-2.yaml");
        //        System.out.println("templateUrl: " + templateUrl);
        System.out.println("contextUrl : " + contextUrl);

        //        MustacheResolver resolver = new ClasspathResolver("template-cp");

        //        final InputStream templateStream = templateUrl.openStream();
        //        assertThat(templateStream).isNotNull();
        final InputStream contextStream = contextUrl.openStream();
        assertThat(contextStream).isNotNull();

        final Yaml yamlMapper = new Yaml();

        final DefaultMustacheFactory mustacheFactory = new DefaultMustacheFactory("template-cp");
        try {
            final Mustache     mustache = mustacheFactory.compile("test-4-cp-part.mustache");
            final StringWriter writer   = new StringWriter();
            final Map<?, ?>    context  = yamlMapper.load(contextStream);
            mustache.execute(writer, context);

            assertThat(writer.toString()).isEqualTo(expected);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            throw ex;
        }
    }
}
