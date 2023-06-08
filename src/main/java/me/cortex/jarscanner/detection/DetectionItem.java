package me.cortex.jarscanner.detection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Outline of a single detection/signature match.
 */
public class DetectionItem {
	private final Path jarPath;
	private final String className;
	private final String methodName;

	/**
	 * @param jarPath
	 * 		Jar containing the detection.
	 * @param className
	 * 		Class name containing the detection.
	 * @param methodName
	 * 		Method in the class containing the detection.
	 */
	public DetectionItem(@Nonnull Path jarPath, @Nullable String className, @Nullable String methodName) {
		this.jarPath = jarPath;
		this.className = className;
		this.methodName = methodName;
	}

	/**
	 * @return Jar containing the detection.
	 */
	@Nonnull
	public Path getJarPath() {
		return jarPath;
	}

	/**
	 * @return Class name containing the detection.
	 */
	@Nullable
	public String getClassName() {
		return className;
	}

	/**
	 * @return Method in the class containing the detection.
	 */
	@Nullable
	public String getMethodName() {
		return methodName;
	}

	@Override
	public String toString() {
		return "DetectionItem{" +
				"jarPath=" + jarPath +
				", className='" + className + '\'' +
				", methodName='" + methodName + '\'' +
				'}';
	}
}
