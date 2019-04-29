package de.turban.gradle.serviceprovider.tasks;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class ClassServiceImplFinder {
    private final Map<String, List<String>> serviceImpls;
    private final Map<String, String> serviceInterfaces;

    ClassServiceImplFinder(Map<String, String> serviceInterfaces){
        this.serviceInterfaces = buildIfMap(serviceInterfaces);
        this.serviceImpls = new LinkedHashMap<>();
    }

    private static Map<String, String> buildIfMap(Map<String, String> serviceInterfaces) {
        Map<String, String> m = new HashMap<>();
        for(Map.Entry<String,String> e: serviceInterfaces.entrySet()){
            String old = m.put(toInternalName(e.getKey()), toInternalName(e.getValue()));
            if( old!= null){
                throw new IllegalStateException("Duplicated class entry for " + e.getKey());
            }
        }
        return m;
    }

    private static String toInternalName(String clsName){
        return clsName.replace(".","/");
    }

    public Map<String, List<String>> getServiceImpls() {
        return serviceImpls;
    }

    public void read(File clsFile) throws IOException {
        try(InputStream in = Files.newInputStream(clsFile.toPath())){
            ClassReader reader = new ClassReader(in);
            IfClassVisitor v = new IfClassVisitor();
            reader.accept(v, ClassReader.SKIP_CODE| ClassReader.SKIP_DEBUG| ClassReader.SKIP_FRAMES);
        }
    }

    private class IfClassVisitor extends ClassVisitor {
        IfClassVisitor() {
            super(Opcodes.ASM7);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if((access & Opcodes.ACC_ABSTRACT) != 0){
                //Do not process Abstract
                return;
            }
            if((access & Opcodes.ACC_INTERFACE) != 0){
                //Do not process interfaces
                return;
            }
            if((access & Opcodes.ACC_ENUM) != 0){
                return;
            }
            if((access & Opcodes.ACC_MODULE) != 0){
                return;
            }
            if((access & Opcodes.ACC_PRIVATE) != 0){
                return;
            }

            checkIf(name, superName);
            for(String ifName : interfaces){
                checkIf(name, ifName);
            }
        }
    }

    private void checkIf(String srcName, String ifName){
        String realIfName = serviceInterfaces.get(ifName);
        if( realIfName != null){
            List<String> l = serviceImpls.computeIfAbsent(Type.getObjectType(realIfName).getClassName(), (k) -> new ArrayList<>());
            l.add(Type.getObjectType(srcName).getClassName());
        }
    }
}
