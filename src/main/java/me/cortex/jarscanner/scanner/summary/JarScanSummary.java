package me.cortex.jarscanner.scanner.summary;

import me.cortex.jarscanner.detection.DetectionItem;
import me.cortex.jarscanner.detection.DetectionProblem;
import me.cortex.jarscanner.detection.DetectionSink;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Summary of items found per single jar file.
 */
public class JarScanSummary implements DetectionSink, ScanSummary {
	private final List<DetectionItem> items = new ArrayList<>();
	private final List<DetectionProblem> problems = new ArrayList<>();
	private final Throwable failure;
    private final Path jarPath;

    /**
	 * Constructs a summary to add items/problems to later.
	 *
	 * @param jarPath
	 * 		Path to the jar scanned.
	 *
	 * @see #addProblem(Throwable)
	 */
	public JarScanSummary(@Nonnull Path jarPath) {
		this.failure = null;
        this.jarPath = jarPath;
	}

	/**
	 * Constructs a summary with a single failure.
	 *
	 * @param jarPath
	 * 		Path to the jar scanned.
	 * @param failure
	 * 		Root cause of the summary failure.
	 */
	public JarScanSummary(@Nonnull Path jarPath, @Nonnull Throwable failure) {
		this.failure = failure;
        this.jarPath = jarPath;
	}

    /**
     * @return Path to the jar scanned.
     */
    @Nonnull
    public Path getJarPath() {
        return jarPath;
    }

    /**
	 * @return Detection items of malicious indicators.
	 */
	@Nonnull
	public List<DetectionItem> getItems() {
		return this.items;
	}

	/**
	 * @return Problems where detections couldn't be made due to some reason or another.
	 */
	@Nonnull
	public List<DetectionProblem> getProblems() {
		return this.problems;
	}

	/**
	 * @return {@code true} if the scan hard-failed, and results could not be fetched.
	 * {@code false} indicates there should be content within {@link #getItems()} or {@link #getProblems()}.
	 */
	public boolean wasScanFailure() {
		return this.failure != null && this.items.isEmpty();
	}

	@Override
	public void addItem(@Nonnull DetectionItem item) {
		this.items.add(item);
	}

	@Override
	public void addProblem(@Nonnull Throwable problemCause) {
		this.problems.add(new DetectionProblem(problemCause, null));
	}

	@Override
	public void addProblem(@Nonnull DetectionProblem problem) {
		this.problems.add(problem);
	}

	@Override
	public void addProblem(@Nonnull String problemMessage) {
		this.problems.add(new DetectionProblem(null, problemMessage));
	}

	@Override
	public String toString() {
		return "JarScanSummary{" +
				"items=" + this.items +
				", problems=" + this.problems +
				", failure=" + this.failure +
				'}';
	}
}
