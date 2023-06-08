package me.cortex.jarscanner.scanner;

import me.cortex.jarscanner.detection.Detection;
import me.cortex.jarscanner.scanner.summary.JarScanSummary;
import me.cortex.jarscanner.scanner.summary.RootScanSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Root scanner responsible for scanning contents of a {@link Path} for malicious jar files.
 */
public class RootScanner implements Scanner<RootScanSummary> {
    private static final Logger logger = LoggerFactory.getLogger(RootScanner.class);
    private final int threads;
    private final long timeoutMinutes;
    private final List<Detection> detections;

    /**
     * @param threads        Number of threads to use for scanning jar file contents.
     * @param timeoutMinutes Timeout before cancelling this root scan operation.
     * @param detections     List of detections to scan for.
     */
    public RootScanner(int threads, long timeoutMinutes, List<Detection> detections) {
        this.threads = threads;
        this.timeoutMinutes = timeoutMinutes;
        this.detections = Collections.unmodifiableList(detections);
    }

    /**
     * @return Summary of all scan results in all jar files within the directory.
     * @throws IOException When some irrecoverable IO issue occurred walking the file directories.
     */
    public RootScanSummary runScan(Path rootDirectory) throws IOException {
        List<JarScanSummary> jarScans = new ArrayList<>();
        ExecutorService scanPool = Executors.newFixedThreadPool(this.threads);
        JarScanner scanner = new JarScanner(this.detections);

        // Walk the file directories in the given path, recording all jars as a jar-scan task to complete later
        Files.walkFileTree(
                rootDirectory,
                Set.of(FileVisitOption.FOLLOW_LINKS),
                Integer.MAX_VALUE,
                new JarFileVisitor(scanner, scanPool, jarScans)
        );
        scanPool.shutdown();

        // Wait until completion or timeout occurs, get future values
        try {
            if (this.timeoutMinutes < 0)
                scanPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            else
                scanPool.awaitTermination(this.timeoutMinutes, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        // Summarize info from all jar-summaries
        return RootScanSummary.fromJarScans(jarScans);
    }

    private static class JarFileVisitor extends SimpleFileVisitor<Path> {
        private final JarScanner scanner;
        private final ExecutorService scanPool;
        private final List<JarScanSummary> jarScans;

        public JarFileVisitor(JarScanner scanner, ExecutorService scanPool, List<JarScanSummary> jarScans) {
            this.scanner = scanner;
            this.scanPool = scanPool;
            this.jarScans = jarScans;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            // Submit all jar files to the thread pool
            String pathNameInsensitive = file.toString().toLowerCase();
            if (pathNameInsensitive.endsWith(".jar")) {
                CompletableFuture.supplyAsync(() -> {
                            try {
                                return this.scanner.runScan(file);
                            } catch (IOException e) {
                                throw new RuntimeException(e); // fuck you checked exceptions
                            }
                        }, this.scanPool)
                        .whenComplete((jarScan, exception) -> {
                            if (jarScan != null) {
                                logger.debug("Completed scan for jar: {} - [Detections={}, Problems={}]",
                                        file, jarScan.getItems().size(), jarScan.getProblems().size());
//                                this.jarScans.add(jarScan);
                            } else if (exception != null) {
                                logger.warn("Error scanning jar: {}", file, exception);
                            }
                        });
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
