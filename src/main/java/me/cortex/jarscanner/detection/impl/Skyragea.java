package me.cortex.jarscanner.detection.impl;

import me.cortex.jarscanner.detection.DetectionSink;
import me.cortex.jarscanner.util.InsnUtils;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static java.lang.reflect.Modifier.isStatic;

public class Skyragea extends AbstractDetection {

    // Based on KosmX's Skyrage detector: https://github.com/KosmX/jneedle/blob/main/src/dbGen/kotlin/dev/kosmx/needle/dbGen/db/Skyrage.kt
    private static final AbstractInsnNode[] SIG = {
            new TypeInsnNode(NEW, "java/lang/String"),
            new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/io/File", "getPath", "()Ljava/lang/String;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "([Ljava/lang/String;)Ljava/lang/Process;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Process", "waitFor", "()I"),
            new MethodInsnNode(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/io/File", "delete", "()Z"),
            new MethodInsnNode(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B"),
            new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"),
    };

    public Skyragea() {
        super("Skyrage Variant a");
    }

    @Override
    public void scan(@Nonnull Path jarPath, @Nonnull ClassNode node, @Nonnull DetectionSink sink) {
        for (MethodNode method : node.methods) {
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
            if (sigOffset >= SIG.length) {
                sink.addItem(det(jarPath, node, method));
                return;
            }
        }
    }

}
