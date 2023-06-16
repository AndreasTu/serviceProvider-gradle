package de.turban.gradle.serviceprovider.tasks

import de.turban.gradle.serviceprovider.ServiceProviderExtension
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.*

import javax.inject.Inject

@SuppressWarnings("unused")
@CacheableTask
@CompileStatic
class GenerateServiceProviderManifestTask extends DefaultTask {
    static final String TASK_NAME = 'generateServiceManifests'
    public static final String SERVICE_DIR = 'META-INF/services'
    public static final String CLASS_FILE_SUFFIX = ".class"
    public static final String NL = "\n"

    @Input
    MapProperty<String, String> serviceInterfaces

    @Classpath
    @SkipWhenEmpty
    FileCollection classes

    @OutputDirectory
    File destinationDir

    @Inject
    GenerateServiceProviderManifestTask(ServiceProviderExtension extension) {
        description = 'Generate META-INF/services for the Java ServiceLoader'
        group = 'Source Generation'

        SourceSetContainer sourceSets = project.extensions.getByType(SourceSetContainer)
        SourceSet main = sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)
        SourceSetOutput mainOutput = main.output
        classes = mainOutput.classesDirs
        destinationDir = getTemporaryDir()
        serviceInterfaces = extension.getServiceInterfaces()
    }

    @TaskAction
    void generate() {
        project.delete(destinationDir)

        ClassServiceImplFinder finder = new ClassServiceImplFinder(serviceInterfaces.get())
        processClasses(finder)
        writeFiles(finder)
    }

    private void processClasses(ClassServiceImplFinder finder) {
        classes.getAsFileTree().each { File cls ->
            if (cls.getName().endsWith(CLASS_FILE_SUFFIX)) {
                finder.read(cls);
            }
        }
    }

    private Map<String, List<String>> writeFiles(ClassServiceImplFinder finder) {
        File serviceDir = new File(destinationDir, SERVICE_DIR)
        if (!finder.serviceImpls.isEmpty()) {
            serviceDir.mkdirs()
        }
        finder.serviceImpls.each { k, v ->
            File file = new File(serviceDir, k)
            StringBuilder b = new StringBuilder()
            v.each {
                b.append(it)
                b.append(NL)
            }
            file.write(b.toString())
        }
    }
}
