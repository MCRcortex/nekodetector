package me.cortex.jarscanner.detection.impl;

import me.cortex.jarscanner.detection.DetectionItem;
import me.cortex.jarscanner.detection.DetectionSink;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static java.lang.reflect.Modifier.isStatic;

/**
 * Detection for <a href="https://github.com/fractureiser-investigation/fractureiser/blob/main/docs/tech.md#stage-0-infected-mod-jars">Fractureiser stage 0.</a>
 */
public class FractureiserStage0c extends AbstractDetection {
    // Method c, this is a hard detect, if it matches this it is 100% chance infected
    // Looks for a byte array with the IP. This is a likely match.
    private static final AbstractInsnNode[] SIG = {
            new IntInsnNode(Opcodes.BIPUSH, 56),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new InsnNode(Opcodes.ICONST_1),
            new IntInsnNode(Opcodes.BIPUSH, 53),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new InsnNode(Opcodes.ICONST_2),
            new IntInsnNode(Opcodes.BIPUSH, 46),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new InsnNode(Opcodes.ICONST_3),
            new IntInsnNode(Opcodes.BIPUSH, 50),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new InsnNode(Opcodes.ICONST_4),
            new IntInsnNode(Opcodes.BIPUSH, 49),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new InsnNode(Opcodes.ICONST_5),
            new IntInsnNode(Opcodes.BIPUSH, 55),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new IntInsnNode(Opcodes.BIPUSH, 6),
            new IntInsnNode(Opcodes.BIPUSH, 46),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new IntInsnNode(Opcodes.BIPUSH, 7),
            new IntInsnNode(Opcodes.BIPUSH, 49),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new IntInsnNode(Opcodes.BIPUSH, 8),
            new IntInsnNode(Opcodes.BIPUSH, 52),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new IntInsnNode(Opcodes.BIPUSH, 9),
            new IntInsnNode(Opcodes.BIPUSH, 52),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new IntInsnNode(Opcodes.BIPUSH, 10),
            new IntInsnNode(Opcodes.BIPUSH, 46),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new IntInsnNode(Opcodes.BIPUSH, 11),
            new IntInsnNode(Opcodes.BIPUSH, 49),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new IntInsnNode(Opcodes.BIPUSH, 12),
            new IntInsnNode(Opcodes.BIPUSH, 51),
            new InsnNode(Opcodes.BASTORE),
            new InsnNode(Opcodes.DUP),
            new IntInsnNode(Opcodes.BIPUSH, 13),
            new IntInsnNode(Opcodes.BIPUSH, 48)
    };

    public FractureiserStage0c() {
        super("Fractureiser Stage 0 Variant C");
    }

    @Override
    public void scan(@Nonnull Path jarPath, @Nonnull ClassNode node, @Nonnull DetectionSink sink) {
        for (MethodNode method : node.methods) {
            // Stage0 injects static methods that are invoked from <clinit>
            if (isStatic(method.access) && method.instructions != null) {
                boolean match = false;
                int pos = 0;
                for (int i = 0; i < method.instructions.size(); i++) {
                    if (pos == SIG.length) {
                        break;
                    }
                    AbstractInsnNode insn = method.instructions.get(i);
                    if (insn.getOpcode() == -1) {
                        continue;
                    }
                    if (insn.getOpcode() == SIG[pos].getOpcode()) {
                        // the opcode matches
                        if (SIG[pos].getType() == AbstractInsnNode.INT_INSN) {
                            // check if operand matches
                            IntInsnNode iInsn = (IntInsnNode) insn;
                            IntInsnNode sigInsn = (IntInsnNode) SIG[pos];
                            if (iInsn.operand == sigInsn.operand) {
                                // operands match
                                match = true;
                                pos++;
                            }
                        } else {
                            // this is a regular InsnNode; just match
                            match = true;
                            pos++;
                        }
                    } else {
                        match = false;
                        pos = 0;
                    }
                }

                if (match) {
                    sink.addItem(det(jarPath, node, method));
                    return;
                }
            }
        }
    }
}
