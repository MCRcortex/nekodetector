package me.cortex.jarscanner.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Various instruction utilities.
 */
public final class InsnUtils {
	private InsnUtils() {
	}

	public static boolean same(AbstractInsnNode a, AbstractInsnNode b) {
		if (a instanceof TypeInsnNode) {
			TypeInsnNode aa = (TypeInsnNode) a;
			return aa.desc.equals(((TypeInsnNode) b).desc);
		}
		if (a instanceof MethodInsnNode) {
			MethodInsnNode aa = (MethodInsnNode) a;
			MethodInsnNode bb = (MethodInsnNode) b;
			return aa.owner.equals(bb.owner) &&
					aa.desc.equals(bb.desc);
		}
		if (a instanceof InsnNode) {
			return true;
		}
		throw new IllegalArgumentException("TYPE NOT ADDED");
	}
}
