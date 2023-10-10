# mustache-templating-maven-plugin

[![Mustache Templating Maven Plugin Build](https://github.com/susanw1/mustache-templating-maven-plugin/actions/workflows/maven.yaml/badge.svg)](https://github.com/susanw1/mustache-templating-maven-plugin/actions/workflows/maven.yaml)
[![codecov](https://codecov.io/gh/susanw1/mustache-templating-maven-plugin/graph/badge.svg?token=U1CDGHGJFY)](https://codecov.io/gh/susanw1/mustache-templating-maven-plugin)

This is a Maven Plugin for running templated file generation during a Maven build. You specify a template in the configuration, and a set of YAML contexts to be used to populate
the template, and an output file is generated per context.

It has the following features:

* Templates and contexts are loadable from directories under _src_, from a _classpath:/_ URL, or from any other reachable URL.
* The context loader is pluggable, so if your contexts are more complex or require some processing, then you can add a custom ContextLoader to produce one or more custom objects to
  be templated. This also gives control over the output filename.

This plugin is an offshoot of the [Zscript](https://github.com/susanw1/zscript) project, where it is used to generate custom Java code from YAML message definitions.

# Usage

The plugin needs to be configured into the POM file in the `<build><plugins>` section as per this snippet (substituting the relevant release version in the `<version>` tag):

    <plugin>
        <groupId>net.zscript.maven-templates</groupId>
        <artifactId>mustache-templating-maven-plugin</artifactId>
        <version>0.1.0</version>
        <executions>
            <execution>
                <id>templating-example</id>
                <goals>
                    <goal>transform</goal>
                </goals>
                <configuration>
                    <outputDirectory>${project.build.directory}/classes/templates-out/test1</outputDirectory>
                    <mainTemplate>test-1.mustache</mainTemplate>
                    <fileTypeSuffix>txt</fileTypeSuffix>
                    <contexts>
                        <includes>**/e*-1.yaml</includes>
                    </contexts>
                </configuration>
            </execution>
        </executions>
    </plugin>

Key points:

* this performs the _transform_ goal in the _generate-sources_ phase (the default) - can be overridden by specifying a different `<phase>`.
* it identifies all files matching the fileset pattern `**/e*-1.yaml` under the default context directory _src/main/contexts_ and generates output files with matching paths under
  the specified output directory (eg _src/main/contexts/foo/example1.yaml_ becomes _target/classes/templates-out/test1/foo/example1.txt_).
* the template file used is found under _src/main/templates_ by default - and if you use Mustache 'partials' (inclusions), then `{{> mypartial.mustache}}` is expected to be
  relative to that same directory.

# More information

More examples may be found in the `pom.xml` in the [_mustache-templating-tests_](https://github.com/susanw1/mustache-templating-maven-plugin/tree/main/mustache-templating-tests)
project.

The plugin is self-documenting. Try:

    mvn help:describe -Dplugin=net.zscript.maven-templates:mustache-templating-maven-plugin:0.1.0 -Ddetail

# Related Links and Support

* Zscript: https://github.com/susanw1/zscript
* Mailing list: https://groups.google.com/g/zscript
* Slack: https://app.slack.com/client/T05NY2VRSE5/C05P94WKR16
