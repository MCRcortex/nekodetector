package me.cortex.jarscanner.scanner;

import me.cortex.jarscanner.scanner.summary.ScanSummary;

import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
public interface Scanner<T extends ScanSummary> {
    /**
     * Runs the scan for this scanner.
     */
    T runScan(Path path) throws IOException;
}
