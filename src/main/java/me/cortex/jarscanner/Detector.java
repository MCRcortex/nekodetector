package me.cortex.jarscanner;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.objectweb.asm.Opcodes.*;

public class Detector {
    public static void scan(JarFile file, Path path) {
        try {
            var matches = file.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .map(entry -> {
                        try {
                            return scanClass(file.getInputStream(entry).readAllBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (matches.isEmpty())
                return;
            System.out.println("Matches: " + path);
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to scan: "+ path);
        }
    }

    private static final AbstractInsnNode[] HARDMATCH = new AbstractInsnNode[] {
            new TypeInsnNode(NEW, "java/lang/String"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new TypeInsnNode(NEW, "java/lang/String"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/net/URL", "<init>", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Constructor", "newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;"),
            new MethodInsnNode(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"),
    };
    private static boolean same(AbstractInsnNode a, AbstractInsnNode b) {
        if (a instanceof TypeInsnNode aa) {
            return aa.desc.equals(((TypeInsnNode)b).desc);
        }
        if (a instanceof MethodInsnNode aa) {
            return aa.owner.equals(((MethodInsnNode)b).owner) && aa.desc.equals(((MethodInsnNode)b).desc) && aa.desc.equals(((MethodInsnNode)b).desc);
        }
        if (a instanceof InsnNode aa) {
            return true;
        }
        throw new IllegalArgumentException("TYPE NOT ADDED");
    }

    public static ScanResult scanClass(byte[] clazz) {
        ClassReader reader = new ClassReader(clazz);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        for (var method : node.methods) {
            //Method 1, this is a hard detect, if it matches this it is 100% chance infected
            if (true) {
                boolean match = true;
                int j = 0;
                for (int i = 0; i < method.instructions.size() && j < HARDMATCH.length; i++) {
                    if (method.instructions.get(i).getOpcode() == -1) {
                        continue;
                    }
                    if (method.instructions.get(i).getOpcode() == HARDMATCH[j].getOpcode()) {
                        if (!same(method.instructions.get(i), HARDMATCH[j++])) {
                            match = false;
                            break;
                        }
                    }
                }
                if (j != HARDMATCH.length) {
                    match = false;
                }
                if (match) {
                    return new ScanResult(node.name, 1000);
                }
            }
        }
        return null;
    }
}
