package me.cortex.jarscanner.scanner;

import me.cortex.jarscanner.scanner.summary.ScanSummary;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Outline of scoped scans.
 *
 * @param <T>
 * 		Scan result type.
 */
@FunctionalInterface
public interface Scanner<T extends ScanSummary> {
	/**
	 * Runs the scan for this scanner.
	 *
	 * @return Scan results.
	 */
	@Nonnull
	T runScan(@Nonnull Path path) throws IOException;
}
