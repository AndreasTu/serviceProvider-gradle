package de.turban.gradle.serviceprovider

import de.turban.gradle.serviceprovider.tasks.GenerateServiceProviderManifestTask
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

@SuppressWarnings("unused")
@CompileStatic
class ServiceProviderPlugin implements  Plugin<Project> {

    @Override
    void apply(Project project) {
        ServiceProviderExtension extension = project.extensions.create(ServiceProviderExtension.EXT_NAME, ServiceProviderExtension, project)
        project.plugins.apply(JavaPlugin)

        def tasks = project.tasks
        TaskProvider<GenerateServiceProviderManifestTask> taskProvider = tasks.register(GenerateServiceProviderManifestTask.TASK_NAME, GenerateServiceProviderManifestTask, extension)
        tasks.named('jar', Jar).configure({
            it.dependsOn(taskProvider)
            it.from({taskProvider.get().getDestinationDir()})
        })
    }
}
