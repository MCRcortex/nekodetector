package me.cortex.jarscanner;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.*;

public class Detector {
    public static void scan(JarFile file, Path path, Function<String, String> output) {
        try {
            var matches = file.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .anyMatch(entry -> {
                        try {
                            return scanClass(file.getInputStream(entry).readAllBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!matches)
                return;
            output.apply("Matches: " + path);
        } catch (Exception e) {
            e.printStackTrace();
            output.apply("Failed to scan: "+ path);
        }
    }

    private static final AbstractInsnNode[] SIG1 = new AbstractInsnNode[] {
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

    private static final AbstractInsnNode[] SIG2 = new AbstractInsnNode[] {
            new MethodInsnNode(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;"),
            new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "INVOKEVIRTUAL", "(Ljava/lang/String;)Ljava/lang/String;"),//TODO:FIXME: this might not be in all of them
            new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/io/File", "getPath", "()Ljava/lang/String;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "([Ljava/lang/String;)Ljava/lang/Process;"),
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

    public static boolean scanClass(byte[] clazz) {
        ClassReader reader = new ClassReader(clazz);
        ClassNode node = new ClassNode();
        try {
            reader.accept(node, 0);
        } catch (Exception e) {
            return false;//Yes this is very hacky but should never happen with valid clasees
        }
        for (var method : node.methods) {
            {
                //Method 1, this is a hard detect, if it matches this it is 100% chance infected
                boolean match = true;
                int j = 0;
                for (int i = 0; i < method.instructions.size() && j < SIG1.length; i++) {
                    if (method.instructions.get(i).getOpcode() == -1) {
                        continue;
                    }
                    if (method.instructions.get(i).getOpcode() == SIG1[j].getOpcode()) {
                        if (!same(method.instructions.get(i), SIG1[j++])) {
                            match = false;
                            break;
                        }
                    }
                }
                if (j != SIG1.length) {
                    match = false;
                }
                if (match) {
                    return true;
                }
            }

            {
                //Method 2, this is a near hard detect, if it matches this it is 95% chance infected
                boolean match = false;
                outer:
                for (int q = 0; q < method.instructions.size(); q++) {
                    int j = 0;
                    for (int i = q; i < method.instructions.size() && j < SIG2.length; i++) {
                        if (method.instructions.get(i).getOpcode() != SIG2[j].getOpcode()) {
                            continue;
                        }

                        if (method.instructions.get(i).getOpcode() == SIG2[j].getOpcode()) {
                            if (!same(method.instructions.get(i), SIG2[j++])) {
                                continue outer;
                            }
                        }
                    }
                    if (j == SIG2.length) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    return true;
                }
            }
        }
        return false;
    }
}
