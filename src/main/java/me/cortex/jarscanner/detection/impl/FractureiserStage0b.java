package me.cortex.jarscanner.detection.impl;

import me.cortex.jarscanner.detection.Detection;
import me.cortex.jarscanner.detection.DetectionItem;
import me.cortex.jarscanner.detection.DetectionSink;
import me.cortex.jarscanner.util.InsnUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static java.lang.reflect.Modifier.isStatic;

/**
 * Detection for <a href="https://github.com/fractureiser-investigation/fractureiser/blob/main/docs/tech.md#stage-0-infected-mod-jars">Fractureiser stage 0.</a>
 */
public class FractureiserStage0b extends AbstractDetection implements Detection, Opcodes {
    // Method B, this is a hard detect, if it matches this it is 100% chance infected
    private static final AbstractInsnNode[] SIG = {
            new TypeInsnNode(NEW, "java/lang/String"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new TypeInsnNode(NEW, "java/lang/String"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getConstructor",
                    "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/net/URL", "<init>",
                    "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Constructor", "newInstance",
                    "([Ljava/lang/Object;)Ljava/lang/Object;"),
            new MethodInsnNode(INVOKESTATIC, "java/lang/Class", "forName",
                    "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"),
    };

    public FractureiserStage0b() {
        super("Fractureiser Stage 0 Variant B");
    }

    @Override
    public void scan(@Nonnull Path jarPath, @Nonnull ClassNode node, @Nonnull DetectionSink sink) {
        for (MethodNode method : node.methods) {
            // Stage0 injects static methods that are invoked from <clinit>
            if (isStatic(method.access) && method.instructions != null) {
                AbstractInsnNode[] insns = method.instructions.toArray();
                int sigOffset = 0;
                for (int i = 0; i < insns.length && sigOffset < SIG.length; i++) {
                    AbstractInsnNode insn = insns[i];

                    // Skip labels
                    if (insn.getOpcode() == -1)
                        continue;

                    // Check if opcode matches sig opcode, and insn operands match
                    if (insn.getOpcode() == SIG[sigOffset].getOpcode() &&
                            !InsnUtils.same(insn, SIG[sigOffset++])) {
                        break;
                    }
                }
                if (sigOffset == SIG.length) {
                    sink.addItem(new DetectionItem(jarPath, node.name, method.name + method.desc));
                    return;
                }
            }
        }
    }
}
