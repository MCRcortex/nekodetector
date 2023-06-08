package me.cortex.jarscanner;

import me.cortex.jarscanner.detection.Detection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Root scanner responsible for scanning contents of a {@link Path} for malicious jar files.
 */
public class RootScanner {
	private static final Logger logger = LoggerFactory.getLogger(RootScanner.class);
	private final Path rootDirectory;
	private final int threads;
	private final long timeoutMinutes;
	private final List<Detection> detectionsToScanFor;

	/**
	 * @param rootDirectory
	 * 		Directory to scan within <i>(Recursively)</i> for jar files.
	 * @param threads
	 * 		Number of threads to use for scanning jar file contents.
	 * @param timeoutMinutes
	 * 		Timeout before cancelling this root scan operation.
	 * @param detectionsToScanFor
	 * 		List of detections to scan for.
	 */
	public RootScanner(Path rootDirectory, int threads, long timeoutMinutes, List<Detection> detectionsToScanFor) {
		this.rootDirectory = rootDirectory;
		this.threads = threads;
		this.timeoutMinutes = timeoutMinutes;
		this.detectionsToScanFor = detectionsToScanFor;
	}

	/**
	 * @return Summary of all scan results in all jar files within the directory.
	 *
	 * @throws IOException
	 * 		When some irrecoverable IO issue occurred walking the file directories.
	 */
	public RootScanSummary run() throws IOException {
		List<JarScanSummary> jarScans = new ArrayList<>();
		ExecutorService scanPool = newFixedThreadPool(threads);

		// Walk the file directories in the given path, recording all jars as a jar-scan task to complete later
		Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				// Submit all jar files to the thread pool
				String pathNameInsensitive = file.toString().toLowerCase();
				if (pathNameInsensitive.endsWith(".jar")) {
					supplyAsync(() -> new JarScanTask(file, detectionsToScanFor).call(), scanPool)
							.whenComplete((jarScan, exception) -> {
								if (jarScan != null) {
									logger.debug("Completed scan for jar: {} - [Detections={}, Problems={}]",
											file, jarScan.getItems().size(), jarScan.getProblems().size());
									jarScans.add(jarScan);
								} else if (exception != null) {
									logger.warn("Error scanning jar: {}", file, exception);
								}
							});
				}
				return FileVisitResult.CONTINUE;
			}
		});
		scanPool.shutdown();

		// Wait until completion or timeout occurs, get future values
		try {
			if (timeoutMinutes < 0)
				scanPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			else
				scanPool.awaitTermination(timeoutMinutes, TimeUnit.MINUTES);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}

		// Summarize info from all jar-summaries
		return RootScanSummary.fromJarScans(jarScans);
	}
}
