package me.cortex.jarscanner.scanner.summary;

import javax.annotation.Nonnull;
import java.util.List;

public class RootScanSummary implements ScanSummary {
	private final List<JarScanSummary> jarScanSummaries;

	public RootScanSummary(@Nonnull List<JarScanSummary> jarScanSummaries) {
		this.jarScanSummaries = jarScanSummaries;
	}

	@Nonnull
	public List<JarScanSummary> getJarScanSummaries() {
		return jarScanSummaries;
	}
}
