package me.cortex.jarscanner;

import me.cortex.jarscanner.detection.DetectionItem;
import me.cortex.jarscanner.detection.DetectionProblem;
import me.cortex.jarscanner.detection.DetectionSink;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Summary of items found per single jar file.
 */
public class JarScanSummary implements DetectionSink {
	private final List<DetectionItem> items = new ArrayList<>();
	private final List<DetectionProblem> problems = new ArrayList<>();
	private final Throwable failure;

	/**
	 * Constructs a summary to add items/problems to later.
	 *
	 * @see #addProblem(Throwable)
	 */
	public JarScanSummary() {
		this.failure = null;
	}

	/**
	 * Constructs a summary with a single failure.
	 *
	 * @param failure
	 * 		Root cause of the summary failure.
	 */
	public JarScanSummary(@Nonnull Throwable failure) {
		this.failure = failure;
	}

	/**
	 * @return Detection items of malicious indicators.
	 */
	@Nonnull
	public List<DetectionItem> getItems() {
		return items;
	}

	/**
	 * @return Problems where detections couldn't be made due to some reason or another.
	 */
	@Nonnull
	public List<DetectionProblem> getProblems() {
		return problems;
	}

	/**
	 * @return {@code true} if the scan hard-failed, and results could not be fetched.
	 * {@code false} indicates there should be content within {@link #getItems()} or {@link #getProblems()}.
	 */
	public boolean wasScanFailure() {
		return failure != null && items.isEmpty();
	}

	@Override
	public void addItem(@Nonnull DetectionItem item) {
		items.add(item);
	}

	@Override
	public void addProblem(@Nonnull Throwable problemCause) {
		problems.add(new DetectionProblem(problemCause, null));
	}

	@Override
	public void addProblem(@Nonnull DetectionProblem problem) {
		problems.add(problem);
	}

	@Override
	public void addProblem(@Nonnull String problemMessage) {
		problems.add(new DetectionProblem(null, problemMessage));
	}

	@Override
	public String toString() {
		return "JarScanSummary{" +
				"items=" + items +
				", problems=" + problems +
				", failure=" + failure +
				'}';
	}
}
