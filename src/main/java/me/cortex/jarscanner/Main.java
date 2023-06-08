package me.cortex.jarscanner;

import me.cortex.jarscanner.detection.DetectionItem;
import me.cortex.jarscanner.detection.DetectionProblem;
import me.cortex.jarscanner.scanner.summary.JarScanSummary;
import me.cortex.jarscanner.scanner.summary.RootScanSummary;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Main entry point for the CLI
 *
 * @see RootScanCommand CLI command outline.
 */
public final class Main {
	private static final Logger logger = getLogger(Main.class);
	private static final DetectionItem NO_DET = new DetectionItem("", Paths.get("."), null, null) {
		@Override
		public String toString() {
			return "No detections";
		}
	};
	private static final DetectionProblem NO_PROB = new DetectionProblem(null, null) {
		@Override
		public String toString() {
			return "No problems";
		}
	};

	private Main() {
	}

	public static void main(String[] args) {
		// Delegate app args to picocli, which will populate the root-scanner parameters and options
		CommandLine cmd = new CommandLine(new RootScanCommand());
		int exitCode = cmd.execute(args);
		RootScanSummary summary = cmd.getExecutionResult();
		List<JarScanSummary> jarScanSummaries = summary.getJarScanSummaries();
		logger.info("Scan complete, scanned {} jars", jarScanSummaries.size());
		for (JarScanSummary jarScanSummary : jarScanSummaries) {
			logger.info("JAR: {}\n" +
							" - Detections:\n" +
							"    - {}\n" +
							" - Problems: \n" +
							"    - {}",
					jarScanSummary.getJarPath(),
					stringify(jarScanSummary.getItems().isEmpty() ? singletonList(NO_DET) : jarScanSummary.getItems()),
					stringify(jarScanSummary.getProblems().isEmpty() ? singletonList(NO_PROB) : jarScanSummary.getProblems()));
		}
		System.exit(exitCode);
	}

	private static String stringify(Collection<?> items) {
		return items.stream().map(Object::toString)
				.collect(Collectors.joining("\n    - "));
	}
}
