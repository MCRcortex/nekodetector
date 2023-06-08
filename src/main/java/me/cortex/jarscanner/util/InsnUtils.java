package me.cortex.jarscanner.util;

import me.coley.cafedude.classfile.instruction.LookupSwitchInstruction;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Various instruction utilities.
 */
public final class InsnUtils {
	private static final Logger logger = LoggerFactory.getLogger(InsnUtils.class);

	private InsnUtils() {
	}

	/**
	 * @param a
	 * 		Insn to compare, {@code null} for wildcard matching.
	 * @param b
	 * 		Insn to compare, {@code null} for wildcard matching.
	 *
	 * @return {@code true} when matching operands.
	 */
	public static boolean same(@Nullable AbstractInsnNode a, @Nullable AbstractInsnNode b) {
		// use null for wildcard matching
		if (a == null || b == null)
			return true;

		// opcodes must be the same
		if (a.getOpcode() != b.getOpcode())
			return false;

		// check operands
		if (a instanceof InsnNode)
			return true;
		if (a instanceof FrameNode) {
			logger.warn("Frames should not be present, visiting frames in the ClassReader is a waste of CPU cycles. " +
					"Use SKIP_FRAMES as the read flags instead");
			return true;
		}
		if (a instanceof TypeInsnNode) {
			TypeInsnNode aa = (TypeInsnNode) a;
			TypeInsnNode bb = (TypeInsnNode) b;
			return aa.desc.equals(bb.desc);
		}
		if (a instanceof FieldInsnNode) {
			FieldInsnNode aa = (FieldInsnNode) a;
			FieldInsnNode bb = (FieldInsnNode) b;
			return aa.owner.equals(bb.owner) &&
					aa.name.equals(bb.name) &&
					aa.desc.equals(bb.desc);
		}
		if (a instanceof MethodInsnNode) {
			MethodInsnNode aa = (MethodInsnNode) a;
			MethodInsnNode bb = (MethodInsnNode) b;
			return aa.owner.equals(bb.owner) &&
					aa.name.equals(bb.name) &&
					aa.desc.equals(bb.desc);
		}
		if (a instanceof IincInsnNode) {
			IincInsnNode aa = (IincInsnNode) a;
			IincInsnNode bb = (IincInsnNode) b;
			return aa.incr == bb.incr &&
					aa.var == bb.var;
		}
		if (a instanceof IntInsnNode) {
			IntInsnNode aa = (IntInsnNode) a;
			IntInsnNode bb = (IntInsnNode) b;
			return aa.operand == bb.operand;
		}
		if (a instanceof JumpInsnNode) {
			JumpInsnNode aa = (JumpInsnNode) a;
			JumpInsnNode bb = (JumpInsnNode) b;
			if (aa.label == null || bb.label == null)
				return true; // assume null intends wildcard matching
			logger.warn("Including jumps in signatures with defined labels is an anti-pattern. " +
					"Use 'null' for labels to indicate a wildcard match.");
			return aa.label.getLabel().getOffset() == bb.label.getLabel().getOffset();
		}
		if (a instanceof LabelNode) {
			LabelNode aa = (LabelNode) a;
			LabelNode bb = (LabelNode) b;
			logger.warn("Including labels in signatures is an anti-pattern. " +
					"Use 'null' to indicate a wildcard match.");
			return aa.getLabel().getOffset() == bb.getLabel().getOffset();
		}
		if (a instanceof LdcInsnNode) {
			LdcInsnNode aa = (LdcInsnNode) a;
			LdcInsnNode bb = (LdcInsnNode) b;
			return Objects.equals(aa.cst, bb.cst);
		}
		if (a instanceof VarInsnNode) {
			VarInsnNode aa = (VarInsnNode) a;
			VarInsnNode bb = (VarInsnNode) b;
			return aa.var == bb.var;
		}
		if (a instanceof TableSwitchInsnNode) {
			TableSwitchInsnNode aa = (TableSwitchInsnNode) a;
			TableSwitchInsnNode bb = (TableSwitchInsnNode) b;
			return aa.min == bb.min && aa.max == bb.max;
		}
		if (a instanceof LookupSwitchInsnNode) {
			LookupSwitchInsnNode aa = (LookupSwitchInsnNode) a;
			LookupSwitchInsnNode bb = (LookupSwitchInsnNode) b;
			return Objects.equals(aa.keys, bb.keys);
		}
		if (a instanceof InvokeDynamicInsnNode) {
			InvokeDynamicInsnNode aa = (InvokeDynamicInsnNode) a;
			InvokeDynamicInsnNode bb = (InvokeDynamicInsnNode) b;
			// do not check indy name (can be arbitrary junk)
			// do not check args, the reference bsm/desc is all we want to look at
			return Objects.equals(aa.desc, bb.desc) &&
					Objects.equals(aa.bsm, bb.bsm);
		}
		throw new IllegalArgumentException("Unsupported op: " + a.getOpcode());
	}
}
