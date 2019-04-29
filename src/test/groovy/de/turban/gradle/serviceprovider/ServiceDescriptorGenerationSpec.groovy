package de.turban.gradle.serviceprovider

class ServiceDescriptorGenerationSpec extends GradleRunSpec {

    public static final String COMPARABLE = "java.lang.Comparable"
    public static final String MY_CLASS = "mypkg/MyClass"

    def generateOneIf(){
        setup:
        buildFileWithPlugin("""
services{
    serviceInterface("java.lang.Comparable")
}
""")
        javaFile(MY_CLASS,
            """
package mypkg;
class MyClass implements Comparable<Object> {
    public int compareTo(Object o){
        return -1;
    }
}
""")
        when:
        def r = gradleBuild()
        def desc =  resolveServiceDescriptorFile(COMPARABLE)
        def clsFile = resolveClassFile(MY_CLASS)

        then:
        r.getOutput() && desc.exists() && desc.getText().trim() == "mypkg.MyClass"
        clsFile.exists()
    }

    def "Indirect interface"(){
        setup:
        buildFileWithPlugin("""
services{
    serviceInterface("java.lang.Comparable", ["java.lang.Cloneable"])
}
""")
        javaFile(MY_CLASS,
            """
package mypkg;
class MyClass implements Cloneable {
    
}
""")
        when:
        def r = gradleBuild()
        def desc =  resolveServiceDescriptorFile(COMPARABLE)
        def clsFile = resolveClassFile(MY_CLASS)

        then:
        r.getOutput() && desc.exists() && desc.getText().trim() == "mypkg.MyClass"
        clsFile.exists()
    }

    def "Indirect baseClass"(){
        setup:
        buildFileWithPlugin("""
services{
    serviceInterface("java.lang.Comparable", ["java.util.ArrayList"])
}
""")
        javaFile(MY_CLASS,
            """
package mypkg;
class MyClass extends java.util.ArrayList<String> {
    
}
""")
        when:
        def r = gradleBuild()
        def desc =  resolveServiceDescriptorFile(COMPARABLE)
        def clsFile = resolveClassFile(MY_CLASS)

        then:
        r.getOutput() && desc.exists() && desc.getText().trim() == "mypkg.MyClass"
        clsFile.exists()
    }


    def "Abstract class does not generate an Impl"(){
        setup:
        buildFileWithPlugin("""
services{
    serviceInterface("java.lang.Comparable")
}
""")
        javaFile(MY_CLASS,
            """
package mypkg;
abstract class MyClass implements Comparable<Object> {
}
""")
        when:
        def r = gradleBuild()
        def desc =  resolveServiceDescriptorFile(COMPARABLE)
        def clsFile = resolveClassFile(MY_CLASS)

        then:
        r.getOutput() && !desc.exists()
        clsFile.exists()
    }

    def "Non matching interface generated nothing"(){
        setup:
        buildFileWithPlugin("""
services{
    serviceInterface("java.lang.Comparator")
}
""")
        javaFile(MY_CLASS,
            """
package mypkg;
class MyClass implements Comparable<Object> {
    public int compareTo(Object o){
        return -1;
    }
}
""")
        when:
        def r = gradleBuild()
        def desc =  resolveServiceDescriptorFile(COMPARABLE)
        def clsFile = resolveClassFile(MY_CLASS)

        then:
        r.getOutput() && !desc.exists()
        clsFile.exists()
    }
}
