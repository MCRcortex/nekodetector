package me.cortex.jarscanner.detection.impl;

import me.cortex.jarscanner.detection.DetectionItem;
import me.cortex.jarscanner.detection.DetectionSink;
import me.cortex.jarscanner.util.InsnUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static java.lang.reflect.Modifier.isStatic;

/**
 * Detection for <a href="https://github.com/fractureiser-investigation/fractureiser/blob/main/docs/tech.md#stage-0-infected-mod-jars">Fractureiser stage 0.</a>
 */
public class FractureiserStage0a extends AbstractDetection {
    // Method A, this is a near hard detect, if it matches this it is 95% chance infected
    private static final AbstractInsnNode[] SIG = {
            new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;"),
            new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"),
            new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "INVOKEVIRTUAL",
                    "(Ljava/lang/String;)Ljava/lang/String;"), // TODO:FIXME: this might not be in all of them
            new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"),
            new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/File", "getPath", "()Ljava/lang/String;"),
            new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime", "exec", "([Ljava/lang/String;)Ljava/lang/Process;"),
    };

    public FractureiserStage0a() {
        super("Fractureiser Stage 0 Variant A");
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
                if (sigOffset >= SIG.length) {
                    sink.addItem(det(jarPath, node, method));
                    return;
                }
            }
        }
    }
}
