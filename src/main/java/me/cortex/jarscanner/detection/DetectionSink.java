package me.cortex.jarscanner.detection;

import javax.annotation.Nonnull;

/**
 * Generic sink for any type that can consume detection items/problems.
 */
public interface DetectionSink {
	/**
	 * @param item
	 * 		Detection item to add.
	 */
	void addItem(@Nonnull DetectionItem item);

	/**
	 * @param problemCause
	 * 		Problem cause exception to add.
	 */
	void addProblem(@Nonnull Throwable problemCause);

	/**
	 * @param problem
	 * 		Problem to add.
	 */
	void addProblem(@Nonnull DetectionProblem problem);

	/**
	 * @param problemMessage
	 * 		Problem message to add.
	 */
	void addProblem(@Nonnull String problemMessage);
}
