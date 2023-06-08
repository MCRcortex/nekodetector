package me.cortex.jarscanner;

import me.cortex.jarscanner.detection.Detection;
import me.cortex.jarscanner.detection.DetectionManager;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * CLI wrapper for {@link RootScanner}
 *
 * @see Main Invokes this command.
 */
@CommandLine.Command(name = "scan", mixinStandardHelpOptions = true, version = "1.0.0",
		description = "Scans the specified directory for malicious signatures.")
public class RootScannerTask implements Callable<RootScanSummary> {
	@CommandLine.Parameters(index = "0", description = "The directory to scan.")
	private File file;

	@CommandLine.Option(names = {"-t", "--threads"}, description = "Number of threads to scan")
	private int threads = Runtime.getRuntime().availableProcessors() - 1;

	@CommandLine.Option(names = {"-x", "--timeout"}, description = "Timeout in minutes before the scan aborts")
	private long timeoutMinutes = -1;

	@Override
	public RootScanSummary call() throws Exception {
		// Scan for everything
		List<Detection> detectionsToScanFor = DetectionManager.getInstance().getDetectionImplementations();

		// Run new scanner
		return new RootScanner(file.toPath(), threads, timeoutMinutes, detectionsToScanFor).run();
	}
}
