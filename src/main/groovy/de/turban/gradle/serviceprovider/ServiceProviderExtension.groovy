package de.turban.gradle.serviceprovider

import org.gradle.api.Project
import org.gradle.api.provider.MapProperty

import javax.inject.Inject

class ServiceProviderExtension {
    static final String EXT_NAME = "services";

    private final Project project

    final MapProperty<String, String> serviceInterfaces

    private Map<String, String> serviceInterfacesInternal

    @Inject
    ServiceProviderExtension(Project project) {
        this.project = project
        this.serviceInterfacesInternal = [:]
        serviceInterfaces = project.objects.mapProperty(String.class, String.class)
        serviceInterfaces.set(serviceInterfacesInternal)
    }

    @SuppressWarnings("unused")
    void serviceInterface(String ifName, List<String> implClasses = null) {
        def implClsLoc = []
        if (implClasses != null) {
            implClsLoc.addAll(implClasses)
        }
        implClsLoc.add(ifName)
        for (String implName : implClsLoc) {
            serviceInterfacesInternal.put(implName, ifName)
        }
    }
}
