package net.zscript.maven.templating.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;

import com.github.mustachejava.MustacheResolver;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;

import net.zscript.maven.templating.contextloader.LoadableEntities;

final class TemplatingBaseMojoTestSupport {
    private TemplatingBaseMojoTestSupport() {
    }

    static TemplatingMojo newMojo(Path projectDirectory, String templateDirectory, String mainTemplate) {
        final TemplatingMojo mojo = new TemplatingMojo();
        final MavenProject project = new MavenProject();
        project.setFile(projectDirectory.resolve("pom.xml").toFile());
        mojo.project = project;
        mojo.templateDirectory = templateDirectory;
        mojo.mainTemplate = mainTemplate;
        return mojo;
    }

    static MustacheResolver createMustacheResolver(TemplatingMojo mojo) throws Exception {
        return (MustacheResolver) invoke(mojo, "createMustacheResolver");
    }

    static LoadableEntities extractContextFileList(TemplatingMojo mojo, FileSet fileSet) throws Exception {
        return (LoadableEntities) invoke(mojo, "extractContextFileList", FileSet.class, fileSet);
    }

    private static Object invoke(TemplatingMojo mojo, String methodName) throws Exception {
        final Method method = TemplatingBaseMojo.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        try {
            return method.invoke(mojo);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    private static Object invoke(TemplatingMojo mojo, String methodName, Class<?> argumentType, Object argument) throws Exception {
        final Method method = TemplatingBaseMojo.class.getDeclaredMethod(methodName, argumentType);
        method.setAccessible(true);
        try {
            return method.invoke(mojo, argument);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

}
