package me.cortex.jarscanner;

import picocli.CommandLine;

/**
 * Main entry point for the CLI
 *
 * @see RootScannerTask CLI command outline.
 */
public class Main {
	public static void main(String[] args) {
		// Delegate app args to picocli, which will populate the root-scanner parameters and options
		int exitCode = new CommandLine(new RootScannerTask()).execute(args);
		System.exit(exitCode);
	}
}
