/*
 * Mustache Templating Maven Plugin - generates files and source-code using Mustache templates.
 * Copyright (c) 2023 Zscript team (Susan Witts, Alicia Witts)
 * SPDX-License-Identifier: Apache-2.0
 */
package net.zscript.maven.templating.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.resolver.ClasspathResolver;
import com.github.mustachejava.resolver.FileSystemResolver;
import com.github.mustachejava.resolver.URIResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import net.zscript.maven.templating.contextloader.LoadableEntities;
import net.zscript.maven.templating.contextloader.LoadableEntities.LoadedEntityScopes;
import net.zscript.maven.templating.contextloader.TemplatingPluginContextLoader;

/**
 * Common superclass for the "Main" and "Test" mojos.
 */
abstract class TemplatingBaseMojo extends AbstractMojo {
    private static final String     FILE_TYPE_SUFFIX_DEFAULT = "java";
    private static final FileSystem FS                       = FileSystems.getDefault();

    /**
     * Defines the directory where the template file is found. The 'mainTemplate' will be searched relative to this directory, and Mustache 'partial' files will be too. Defaults to
     * 'src/main/templates' (or 'src/test/templates' if 'test-transform' goal is used). This param may be a directory URL instead, and it may use a 'classpath:/' scheme to read
     * from classpath resources.
     */
    @Parameter
    protected String templateDirectory;

    /**
     * A file name, possibly with a relative path, to specify the template file to be used. Described more fully in {@link #templateDirectory}.
     */
    @Parameter(required = true)
    protected String mainTemplate;

    /**
     * A fileset describing a set of context files (ie JSON/YAML files for the default transformer). Defaults to 'src/main/contexts' (or 'src/test/contexts' if 'test-transform'
     * goal is used). If the &lt;directory&gt; element is specified but does not correspond to an existing directory, then it will be attempted as a URL, also supporting the
     * "classpath:" scheme to read from classpath resources. Note, if a URL directory is specified in the fileset, then only specific &lt;include&gt; tags with relative paths are
     * supported with URLs - no wildcards, no excludes etc.
     */
    @Parameter
    protected FileSet contexts;

    /**
     * Specify output directory where the transformed output files are placed. This directory is also added to the Maven Compile Source Root list if the {@code fileTypeSuffix} is
     * "java", or if the 'generateSources' parameter is set.
     */
    @Parameter
    protected File outputDirectory;

    /**
     * The fully-qualified classname of a {@link TemplatingPluginContextLoader} to use for loading and mapping the files described by the {@code contexts}. Changing this allows you
     * to perform arbitrary transformations from any file-type you can read.
     * <p>
     * The class you specify here must be built in a separate Maven module from the one you want to use it, which must be added as a dependency of the plugin execution definition.
     * See the integration-test examples under {@code src/it} for examples.
     */
    @Parameter(defaultValue = "net.zscript.maven.templating.contextloader.YamlTemplatingPluginContextLoader")
    protected String contextLoaderClass;

    /**
     * If true, then an empty context fileset is considered an error.
     */
    @Parameter(defaultValue = "true")
    protected boolean failIfNoFiles;

    /**
     * The file suffix to add to output files. If "java" (the default), then the output directory is added to the compile/test scope.
     */
    @Parameter(defaultValue = FILE_TYPE_SUFFIX_DEFAULT)
    protected String fileTypeSuffix;

    /**
     * If 'true', then the output directory is added to the {@code compile} scope, which allows generated sources to be properly compiled into the project. If false, then the
     * {@code compile} scope is unchanged. If unset (default), sources will only be added if fileTypeSuffix is ".java".
     */
    @Parameter
    protected String generateSources;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    protected final List<String> templateRootDirs = new ArrayList<>();

    /**
     * Performs the actual execution for either of the subclass Mojos.
     *
     * @param contextDefaultDir the base directory for loading context files (differs for main vs test)
     * @param outputDefaultDir  the base directory for putting generated files (differs for main vs test)
     * @return a path to be added as a CompileSourceRoot directory (for compilation), or null if not required
     * @throws MojoExecutionException if anything fails
     */
    public String executeBase(String contextDefaultDir, String outputDefaultDir) throws MojoExecutionException {
        final MustacheResolver mustacheResolver = createMustacheResolver();
        final MustacheFactory  mustacheFactory  = new DefaultMustacheFactory(mustacheResolver);

        final FileSet          contextFileSet  = initFileSet(contexts, contextDefaultDir);
        final LoadableEntities contextEntities = extractContextFileList(contextFileSet);

        // read in context files as YAML and perform any field mapping as required. Read in templates ready to use Mustache.
        final List<LoadedEntityScopes> loadedMappedScopes = loadMappedContexts(contextEntities);
        getLog().info("outputDir: " + outputDirectory);

        if (outputDirectory == null) {
            outputDirectory = new File(project.getBuild().getDirectory(), outputDefaultDir);
        }
        getLog().info("outputDir: " + outputDirectory);

        final Path outputDirectoryPath = outputDirectory.toPath();
        createDirIfRequired(outputDirectoryPath);

        // This is the important bit: iterates the contexts and performs the actual Mustache templating.
        for (LoadedEntityScopes context : loadedMappedScopes) {
            try {
                final Path outputFileFullPath = outputDirectoryPath.resolve(context.getRelativeOutputPath());
                final Path outputParentDir    = outputFileFullPath.getParent();
                createDirIfRequired(outputParentDir);
                final Mustache mustache = mustacheFactory.compile(mainTemplate);
                getLog().info("Applying context " + context.getRelativePath() + " with template " + mainTemplate + " to " + outputFileFullPath);
                try (Writer out = Files.newBufferedWriter(outputFileFullPath)) {
                    mustache.execute(out, context.getScopes());
                }
            } catch (final IOException e) {
                throw new MojoExecutionException("Failed to generate output file: " + outputDirectoryPath, e);
            }
        }

        if (Boolean.parseBoolean(generateSources) || generateSources == null && fileTypeSuffix.equals(FILE_TYPE_SUFFIX_DEFAULT)) {
            return outputDirectoryPath.toString();
        }

        return null;
    }

    /**
     * This does the legwork with respect to handling the 'templateDirectory', 'mainTemplate', and the main (and possibly test) 'templateRootDirs'.  The templateRootDirs are only
     * tried if  templateDirectory appears to be a relative path.
     * <p>
     * If treating them as files doesn't work, it tries them as a URL. If there's a scheme called "classpath", then the URL path is tried as a Classpath, reading from the
     * default/configured resources root.
     *
     * @return a valid template resolver, never null
     */
    private MustacheResolver createMustacheResolver() {
        final String messagePrefix = "Main Template resolution for \"" + mainTemplate + "\": ";

        try {
            if (templateDirectory == null || templateDirectory.isEmpty()) {
                return createFileResolverFromDefaultRoots();
            }

            final Path configuredPath = FS.getPath(templateDirectory);
            if (configuredPath.isAbsolute()) {
                return requireFileResolver(createFileResolver(configuredPath));
            }

            final URI dirUri = new URI(templateDirectory);
            if (dirUri.getScheme() == null) {
                final Path projectTemplateDirectory = project.getBasedir().toPath().resolve(configuredPath).normalize();
                MustacheResolver mustacheResolver = createFileResolver(projectTemplateDirectory);
                if (mustacheResolver != null) {
                    return mustacheResolver;
                }
                return createFileResolverFromDefaultRoots(configuredPath);
            }

            if (dirUri.getScheme().equals("classpath")) {
                final String path         = dirUri.getPath();
                final String resourceRoot = path.startsWith("/") ? path.substring(1) : path;
                getLog().debug(messagePrefix + ": use ClasspathResolver with resourceRoot: " + resourceRoot);
                return new ClasspathResolver(resourceRoot);
            } else {
                getLog().debug(messagePrefix + ": use URI resolver with resourceRoot: " + dirUri);
                return createUriResolver(dirUri);
            }
        } catch (URISyntaxException e1) {
            throw new TemplatingMojoFailureException("Bad template directory URI: " + templateDirectory, e1);
        }
    }

    private MustacheResolver createFileResolverFromDefaultRoots() {
        for (String defaultDir : templateRootDirs) {
            MustacheResolver mustacheResolver = createFileResolver(project.getBasedir().toPath().resolve(defaultDir));
            if (mustacheResolver != null) {
                return mustacheResolver;
            }
        }
        throw new TemplatingMojoFailureException("Cannot locate template: " + mainTemplate);
    }

    private MustacheResolver createFileResolverFromDefaultRoots(Path relativeTemplateDirectory) {
        for (String defaultDir : templateRootDirs) {
            final Path templateDirectory = project.getBasedir().toPath().resolve(defaultDir).resolve(relativeTemplateDirectory).normalize();
            MustacheResolver mustacheResolver = createFileResolver(templateDirectory);
            if (mustacheResolver != null) {
                return mustacheResolver;
            }
        }
        throw new TemplatingMojoFailureException("Cannot locate template: " + mainTemplate);
    }

    private MustacheResolver requireFileResolver(MustacheResolver mustacheResolver) {
        if (mustacheResolver == null) {
            throw new TemplatingMojoFailureException("Cannot locate template: " + mainTemplate);
        }
        return mustacheResolver;
    }

    private MustacheResolver createFileResolver(Path templateDirectoryPath) {
        if (!Files.isDirectory(templateDirectoryPath)) {
            getLog().debug("  checked possible template dir (doesn't exist): " + templateDirectoryPath);
            return null;
        }
        final Path mainTemplateFullPath = templateDirectoryPath.resolve(mainTemplate);

        if (!Files.isRegularFile(mainTemplateFullPath)) {
            getLog().debug("  possible template dir exists: " + templateDirectoryPath);
            getLog().debug("  but template doesn't: " + mainTemplateFullPath);
            return null;
        }
        getLog().info("Template found in dir: " + templateDirectoryPath);
        return new FileSystemResolver(templateDirectoryPath.toFile());
    }

    private MustacheResolver createUriResolver(URI templateDirectoryUri) {
        final URI directoryUri = withTrailingSlash(templateDirectoryUri);
        final URIResolver uriResolver = new URIResolver();
        final MustacheResolver resolver = resourceName -> uriResolver.getReader(directoryUri.resolve(resourceName).toString());

        try (Reader reader = resolver.getReader(mainTemplate)) {
            if (reader == null) {
                throw new TemplatingMojoFailureException("Cannot locate template: " + directoryUri.resolve(mainTemplate));
            }
        } catch (IOException e) {
            throw new TemplatingMojoFailureException("Cannot read template: " + directoryUri.resolve(mainTemplate), e);
        }
        return resolver;
    }

    private URI withTrailingSlash(URI directoryUri) {
        final String path = directoryUri.getPath();
        if (path == null || path.endsWith("/")) {
            return directoryUri;
        }
        try {
            return new URI(directoryUri.getScheme(), directoryUri.getUserInfo(), directoryUri.getHost(), directoryUri.getPort(),
                    path + "/", directoryUri.getQuery(), directoryUri.getFragment());
        } catch (URISyntaxException e) {
            throw new TemplatingMojoFailureException("Bad template directory URI: " + directoryUri, e);
        }
    }

    private void createDirIfRequired(final Path outputDirectoryPath) throws MojoExecutionException {
        if (!Files.isDirectory(outputDirectoryPath)) {
            try {
                getLog().debug("Creating output directory: " + outputDirectoryPath);
                Files.createDirectories(outputDirectoryPath);
            } catch (final IOException e) {
                throw new MojoExecutionException("Cannot create output directory: " + outputDirectoryPath, e);
            }
        }
    }

    /**
     * Ensures we have a FileSet with a 'directory' configured, using the supplied default if not already set.
     *
     * @param fs         a fileset to update, or null (in which case a new empty FileSet is created)
     * @param defaultDir the directory to use, if FileSet doesn't already have one; this is resolved relative to the project's basedir
     * @return a FileSet with a configured directory
     */
    private FileSet initFileSet(final FileSet fs, final String defaultDir) {
        final FileSet fileSet = fs != null ? fs : new FileSet();

        if (fileSet.getDirectory() == null) {
            final Path dirToSet = project.getBasedir().toPath().resolve(defaultDir);
            fileSet.setDirectory(dirToSet.toString());
        }
        return fileSet;
    }

    private LoadableEntities extractContextFileList(final FileSet fileSet) throws MojoExecutionException {
        final String directoryString = fileSet.getDirectory();
        try {
            URI rootUri = new URI(directoryString);
            if (rootUri.getScheme() != null) {
                getLog().debug("Context: directory is valid URI, so assuming using limited 'includes' paths: " + directoryString);
                return new LoadableEntities(rootUri, fileSet.getIncludes(), fileTypeSuffix, FS);
            }
        } catch (URISyntaxException e) {
            getLog().debug("Context: directory isn't valid URI, so assuming local directory: " + directoryString);
        }

        return extractFileListAsLocalFiles(fileSet, FS.getPath(directoryString));
    }

    private LoadableEntities extractFileListAsLocalFiles(final FileSet fileSet, Path rootPath) throws MojoExecutionException {
        URI rootUri;
        if (!Files.isDirectory(rootPath)) {
            throw new MojoExecutionException("Context directory not found: " + rootPath);
        }
        if (!Files.isReadable(rootPath)) {
            throw new MojoExecutionException("Context directory not readable: " + rootPath);
        }
        rootUri = rootPath.toUri();

        getLog().debug("    Context: fileSet.getDirectory: " + rootPath + "; rootUri: " + rootUri);

        final FileSetManager fileSetManager = new FileSetManager();
        final List<String>   files          = stream(fileSetManager.getIncludedFiles(fileSet)).collect(Collectors.toList());

        if (failIfNoFiles && files.isEmpty()) {
            throw new MojoExecutionException("No matching Context files found in: " + rootPath);
        }

        getLog().debug("    #files = " + files.size());
        files.forEach(f -> getLog().debug("    " + f));

        return new LoadableEntities(rootUri, files, fileTypeSuffix, rootPath.getFileSystem());
    }

    private List<LoadedEntityScopes> loadMappedContexts(LoadableEntities contextEntities) throws MojoExecutionException {
        final TemplatingPluginContextLoader contextLoader;
        try {
            contextLoader = (TemplatingPluginContextLoader) Class.forName(contextLoaderClass).getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            throw new MojoExecutionException("Cannot load class '" + contextLoaderClass + "'", e);
        }
        return contextLoader.loadAndMap(contextEntities);
    }

    static class TemplatingMojoFailureException extends RuntimeException {
        TemplatingMojoFailureException(String msg, Exception e) {
            super(msg, e);
        }

        TemplatingMojoFailureException(String msg) {
            super(msg);
        }
    }
}
