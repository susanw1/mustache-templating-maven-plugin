# mustache-templating-maven-plugin

[![Mustache Templating Maven Plugin Build](https://github.com/susanw1/mustache-templating-maven-plugin/actions/workflows/maven.yaml/badge.svg)](https://github.com/susanw1/mustache-templating-maven-plugin/actions/workflows/maven.yaml)
[![codecov](https://codecov.io/gh/susanw1/mustache-templating-maven-plugin/graph/badge.svg?token=U1CDGHGJFY)](https://codecov.io/gh/susanw1/mustache-templating-maven-plugin)
[![CodeFactor](https://www.codefactor.io/repository/github/susanw1/mustache-templating-maven-plugin/badge)](https://www.codefactor.io/repository/github/susanw1/mustache-templating-maven-plugin)
[![javadoc](https://javadoc.io/badge2/net.zscript.maven-templates/mustache-templating-maven-plugin/javadoc.svg)](https://javadoc.io/doc/net.zscript.maven-templates/mustache-templating-maven-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/net.zscript.maven-templates/mustache-templating.svg?label=Maven%20Central)](https://search.maven.org/artifact/net.zscript.maven-templates/mustache-templating)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](http://www.apache.org/licenses/LICENSE-2.0)

This is a Maven Plugin for running templated file generation during a Maven build. You specify a template in the configuration, and a set of contexts to be used to populate
the template, and an output file is generated per context. In its simplest form, a context is just a JSON or YAML file, perhaps just containing a few settings which get
substituted into the template.

It has the following features:

* Templates and contexts are loadable either from directories under _src_, from a _classpath:/_ URL, or from any other reachable URL.
* The context loader is pluggable, so if your contexts are more complex or require some processing, then you can add a custom ContextLoader to produce one or more custom objects to
  be templated. This also gives control over the output filename.

This plugin is an offshoot of the [Zscript](https://github.com/susanw1/zscript) project, where it is used to generate custom Java code from YAML message definitions.

Other plugins let you generate a lot of files from a lot of templates, given a context. This one is the other way around: given a lot of contexts, run them through a template. In
Zscript, we have lots of YAML module definitions, and we want a source-file generated for each one. Whilst it would be easy to make it able to process many templates, it
becomes complex to specify the output filenames in a flexible way - and as Zscript is using them to generate code which demands significant tie-in work, it's viable simply to
have multiple execution blocks in the maven plugin configuration.

# Usage

The plugin needs to be configured into the POM file in the `<build><plugins>` section as per this snippet (substituting the relevant release version in the `<version>` tag):

    <plugin>
        <groupId>net.zscript.maven-templates</groupId>
        <artifactId>mustache-templating-maven-plugin</artifactId>
        <version>1.0.0</version>
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
* it identifies all files matching the fileset pattern (eg `**/e*-1.yaml` in the example) under the default context directory _src/main/contexts_ and generates output files with
  matching paths under
  the specified output directory (eg _src/main/contexts/foo/example1.yaml_ becomes _target/classes/templates-out/test1/foo/example1.txt_).
* the template file used is found under _src/main/templates_ by default - and if you use Mustache 'partials' (inclusions), then `{{> mypartial.mustache}}` is expected to be
  relative to that same directory.

## Context Loading and Mapping

By default, the plugin assumes each of your contexts is a single JSON/YAML file. If you use some other format, or if your contexts need processing before the template is
applied, then you can create a _context loader_. This is a Java class which is given the context files' paths from the POM, and returns the Objects which are presented to the
template.

This allows:

* loading context from arbitrary file formats
* resolution of filename/URL inclusion (eg #include)
* multiple new "sub" contexts produced from a single master file

To make context loading/mapping work, you need to:

* Create a _new, separate Maven module_ to put this context-loader class in, and add *net.zscript.maven-templates:mustache-templating-context-loader* as a dependency. You have to
  do this, because this module
  will be part of your build, not part of your application, and needs to be built and packaged in advance of using it. It can't be in the same module that uses it.
* In this new module, create a subclass
  of [`net.zscript.maven.templating.contextloader.TemplatingPluginContextLoader`](https://github.com/susanw1/mustache-templating-maven-plugin/tree/main/mustache-templating-context-loader/src/main/java/net/zscript/maven/templating/contextloader),
  and implement its `loadAndMap` method.
* In the module where you want to apply the template, add an execution as above, but
    1. add the transformer module as a *plugin* dependency (not a normal one), and
    1. specify your transformer class in the execution's configuration block, like this:

           <contextLoaderClass>com.example.mycontextloader.MyTemplatingContextLoader</contextLoaderClass>

# More information

More examples may be found in the `pom.xml` in the [_mustache-templating-tests_](https://github.com/susanw1/mustache-templating-maven-plugin/tree/main/mustache-templating-tests)
project.

The plugin is self-documenting. Try this (with the latest version):

    mvn help:describe -Dplugin=net.zscript.maven-templates:mustache-templating-maven-plugin:1.0.0 -Ddetail

# Related Links and Support

* Zscript: https://github.com/susanw1/zscript
* Mailing list: https://groups.google.com/g/zscript
* Slack: https://app.slack.com/client/T05NY2VRSE5/C05P94WKR16
