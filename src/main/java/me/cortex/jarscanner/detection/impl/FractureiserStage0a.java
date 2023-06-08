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
public class FractureiserStage0a implements Detection, Opcodes {
	// Method A, this is a near hard detect, if it matches this it is 95% chance infected
	private static final AbstractInsnNode[] SIG = new AbstractInsnNode[] {
			new MethodInsnNode(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;"),
			new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"),
			new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "INVOKEVIRTUAL",
					"(Ljava/lang/String;)Ljava/lang/String;"), // TODO:FIXME: this might not be in all of them
			new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"),
			new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
			new MethodInsnNode(INVOKEVIRTUAL, "java/io/File", "getPath", "()Ljava/lang/String;"),
			new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "([Ljava/lang/String;)Ljava/lang/Process;"),
	};

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
