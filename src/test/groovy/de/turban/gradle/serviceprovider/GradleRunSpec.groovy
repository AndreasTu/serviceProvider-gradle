package de.turban.gradle.serviceprovider


import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

abstract class GradleRunSpec extends Specification {

    BuildResult lastResult

    File testProjectDir

    void setup() {
        testProjectDir = createTempProjectDir()
        defaultFiles()
    }

    BuildResult gradleBuild() {
        return gradleRun("build")
    }

    BuildResult gradleRun(String... args) {
        def argsList = []
        argsList.addAll(args)
        argsList.addAll(["-s", "--warning-mode", "all"])

        lastResult = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(argsList)
            .withPluginClasspath()
            .withDebug(true)
            .build()
        return lastResult
    }

    File resolveFile(String path) {
        return new File(testProjectDir, path)
    }

    File resolveServiceDescriptorFile(String path) {
        return new File(testProjectDir, "build/tmp/generateServiceManifests/META-INF/services/" + path)
    }

    File resolveClassFile(String path) {
        return new File(testProjectDir, "build/classes/java/main/" + path + ".class")
    }

    void buildFile(String content) {
        def file = resolveFile("build.gradle")
        file.write(content)
    }

    void buildFileWithPlugin(String content) {
        buildFile("""
plugins{
    id 'de.turban.gradle.serviceprovider'
}
""" + content)
    }

    void javaFile(String name, String content) {
        def file = resolveFile("src/main/java/${name}.java")
        file.getParentFile().mkdirs()
        file.write(content)
    }

    void defaultFiles() {
        def file = resolveFile("settings.gradle")
        file.write("")
    }

    private File createTempProjectDir() {
        File folder = new File("./build/tmp/testRun/" + this.getClass().getSimpleName() + "/" + specificationContext.currentIteration.name)
        if (folder.exists()) {
            folder.deleteDir()
        }
        File folderAbs = folder.getCanonicalFile()
        folderAbs.mkdirs()

        return folderAbs
    }
}
