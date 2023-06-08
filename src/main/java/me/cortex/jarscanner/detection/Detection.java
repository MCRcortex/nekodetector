package me.cortex.jarscanner.detection;

import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Outline for detections.
 * <p>
 * Implementors simply operate on the given node, and pass values they detect into the sink via sink operations:
 * <ul>
 * <li>{@link DetectionSink#addItem(DetectionItem)}</li>
 * <li>{@link DetectionSink#addProblem(String)}</li>
 * <li>{@link DetectionSink#addProblem(Throwable)}</li>
 * <li>{@link DetectionSink#addProblem(DetectionProblem)}</li>
 * </ul>
 */
public interface Detection {
    /**
     * @return Name of the detection pattern.
     */
    @Nonnull
    String getName();

    /**
     * @param jarPath Jar containing the class being scanned.
     * @param node    Class node to scan in.
     * @param sink    Sink to feed detection items into.
     */
    void scan(@Nonnull Path jarPath, @Nonnull ClassNode node, @Nonnull DetectionSink sink);
}
