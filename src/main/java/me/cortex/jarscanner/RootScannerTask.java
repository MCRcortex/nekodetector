package me.cortex.jarscanner;

import me.cortex.jarscanner.detection.Detection;
import me.cortex.jarscanner.detection.DetectionManager;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * CLI wrapper for {@link RootScanner}
 *
 * @see Main Invokes this command.
 */
@CommandLine.Command(/*name = "scan",*/ mixinStandardHelpOptions = true, version = "1.0.0",
        description = "Scans the specified directory for malicious signatures.")
public class RootScannerTask implements Callable<RootScanSummary> {
    private static final Logger logger = getLogger(RootScannerTask.class);

    @CommandLine.Parameters(index = "0", description = "The directory to scan.")
    private File file;

    @CommandLine.Option(names = {"-t", "--threads"}, description = "Number of threads to scan")
    private int threads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);

    @CommandLine.Option(names = {"-x", "--timeout"}, description = "Timeout in minutes before the scan aborts")
    private long timeoutMinutes = -1;

    @Override
    public RootScanSummary call() throws IOException {
        // Scan for everything
        List<Detection> detectionsToScanFor = DetectionManager.getInstance().getDetectionImplementations();

        logger.info("Running detectors: {}", detectionsToScanFor.stream().map(Detection::getName).collect(Collectors.joining(", ")));

        // Run new scanner
        return new RootScanner(this.file.toPath(), this.threads, this.timeoutMinutes, detectionsToScanFor).run();
    }
}