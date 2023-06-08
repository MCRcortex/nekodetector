package me.cortex.jarscanner.detection.impl;

import me.cortex.jarscanner.detection.Detection;
import me.cortex.jarscanner.detection.DetectionItem;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Base for detections.
 */
public abstract class AbstractDetection implements Detection, Opcodes {
	private final String name;

	/**
	 * @param name
	 * 		Detection implementation name.
	 */
	public AbstractDetection(@Nonnull String name) {
		this.name = name;
	}

	@Nonnull
	@Override
	public String getName() {
		return this.name;
	}

	@Nonnull
	protected DetectionItem det(@Nonnull Path path, @Nonnull ClassNode node, @Nonnull MethodNode method) {
		return new DetectionItem(getName(), path, node.name, method.name + method.desc);
	}
}
