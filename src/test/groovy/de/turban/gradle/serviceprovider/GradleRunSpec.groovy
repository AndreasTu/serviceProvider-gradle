package de.turban.gradle.serviceprovider

import groovy.transform.CompileStatic
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.Specification

@CompileStatic
abstract class GradleRunSpec extends  Specification {

    BuildResult lastResult

    @Rule
    TestName name = new TestName()

    File testProjectDir

    @Before
    void beforeFolderPrepare(){
        testProjectDir = createTempProjectDir()
        defaultFiles()
    }


    BuildResult gradleBuild(String... args){
        return  gradleRun("build")
    }

    BuildResult gradleRun(String... args){
        def argsList = []
        argsList.addAll(args)
        argsList.addAll(["-s","--warning-mode","all"])

        lastResult = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(argsList)
            .withPluginClasspath()
            .withDebug(true)
            .build()
        return lastResult
    }

    File resolveFile(String path){
        return new File(testProjectDir, path)
    }

    File resolveServiceDescriptorFile(String path){
        return new File(testProjectDir, "build/tmp/generateServiceManifests/META-INF/services/"+ path)
    }

    File resolveClassFile(String path){
        return new File(testProjectDir, "build/classes/java/main/"+ path+".class")
    }

    void buildFile(String content){
        def file = resolveFile("build.gradle")
        file.write(content)
    }

    void buildFileWithPlugin(String content){
        buildFile("""
plugins{
    id 'de.turban.gradle.serviceprovider'
}
"""+content)
    }

    void javaFile(String name, String content){
        def file = resolveFile( "src/main/java/${name}.java")
        file.getParentFile().mkdirs()
        file.write(content)
    }

    void defaultFiles(){
        def file = resolveFile( "settings.gradle")
        file.write("")
    }

    private File createTempProjectDir(){
        File folder = new File("./build/tmp/testRun/"+ this.getClass().getSimpleName()+"/"+this.name.methodName)
        if( folder.exists()){
            folder.deleteDir()
        }
        File folderAbs = folder.getCanonicalFile()
        folderAbs.mkdirs()

        return folderAbs
    }
}
