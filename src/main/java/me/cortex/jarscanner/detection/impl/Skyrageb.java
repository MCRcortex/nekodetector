package me.cortex.jarscanner.detection.impl;

import me.cortex.jarscanner.detection.DetectionSink;
import me.cortex.jarscanner.util.InsnUtils;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static java.lang.reflect.Modifier.isStatic;

public class Skyrageb extends AbstractDetection {

    // Based on KosmX's Skyrage detector: https://github.com/KosmX/jneedle/blob/main/src/dbGen/kotlin/dev/kosmx/needle/dbGen/db/Skyrage.kt
    private static final AbstractInsnNode[] SIG = {
            new MethodInsnNode(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;"),
            new TypeInsnNode(ANEWARRAY, "java/lang/String"),
            new TypeInsnNode(NEW, "java/lang/StringBuilder"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
            new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getEncoder", "()Ljava/util/Base64$Encoder;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Encoder", "encodeToString", "([B)Ljava/lang/String;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"),
            new TypeInsnNode(NEW, "java/lang/String"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/io/File", "getPath", "()Ljava/lang/String;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "([Ljava/lang/String;)Ljava/lang/Process;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Process", "waitFor", "()I"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/io/File", "delete", "()Z"),
    };

    public Skyrageb() {
        super("Skyrage Variant b");
    }

    @Override
    public void scan(@Nonnull Path jarPath, @Nonnull ClassNode node, @Nonnull DetectionSink sink) {

        // Basically for each class, loop through its methods
        // When inside the method, loop through each instruction until we reach the first one
        // to match the first instruction in SIG.

        // loop through every method in the class
        for (MethodNode method : node.methods) {

            // array of instructions
            AbstractInsnNode[] insns = method.instructions.toArray();

            // track which item in SIG we are currently looking for
            int sigOffset = 0;

            // loop through the instructions
            for (int i = 0; i < insns.length && sigOffset < SIG.length; i++) {
                // bytecode instruction
                AbstractInsnNode insn = insns[i];

                // Skip labels
                if (insn.getOpcode() == -1)
                    continue;

                // Check if opcode matches sig opcode, and insn operands match
                //
                if (insn.getOpcode() == SIG[sigOffset].getOpcode() &&
                        !InsnUtils.same(insn, SIG[sigOffset++])) {
                    sigOffset = 0;
                    continue;
                }
            }

            // if we've cycled through the entire SIG array, we've got a match
            if (sigOffset >= SIG.length) {
                sink.addItem(det(jarPath, node, method));
                return;
            }
        }
    }

}
