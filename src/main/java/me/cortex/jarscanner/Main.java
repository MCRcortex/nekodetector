package me.cortex.jarscanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.jar.JarFile;

/**
 * Main class for Nekodetector, which scans for malicious code signatures from the Nekoclient malware.
 * <p>ORIGINAL SOURCE: https://github.com/MCRcortex/nekodetector</p>
 *
 * @author MCRcortex (https://github.com/MCRcortex)
 * @author Huskydog9988 (https://github.com/Huskydog9988)
 * @author mica-alex (https://github.com/mica-alex)
 */
public class Main {

    /**
     * The executor service used to scan jars in parallel.
     */
    private static ExecutorService executorService;

    /**
     * Main method. Checks arguments and scans the specified directory for malicious code signatures. Outputs the
     * results of the scan to the console.
     *
     * @param args the command line arguments (number of threads, directory to scan, and whether to emit walk errors)
     */
    public static void main(String[] args) {
        // Check arguments
        if (!checkArgs(args)) {
            return;
        }

        // Parse arguments
        int nThreads = Integer.parseInt(args[0]);
        Path dirToCheck = new File(args[1]).toPath();
        boolean emitWalkErrors = false;
        if (args.length > 2) {
            emitWalkErrors = Boolean.parseBoolean(args[2]);
        }

        // Create log output function
        Function<String, String> logOutput = outputString -> {
            System.out.println(outputString);
            return outputString;
        };

        // Output scan start
        logOutput.apply("Starting scan...");

        // Run scan
        Results results = null;
        try {
            results = run(nThreads, dirToCheck, emitWalkErrors, logOutput);
        } catch (IOException e) {
            logOutput.apply("An error occurred while scanning the directory: " + dirToCheck);
            e.printStackTrace();
        } catch (InterruptedException e) {
            logOutput.apply("An error occurred while waiting for the scan to complete.");
            e.printStackTrace();
        }

        // Output scan completion and results
        outputRunResults(results, logOutput);
    }

    /**
     * Output the results of a scan to the specified log output function.
     *
     * @param results   the resulting from {@link #run(int, Path, boolean, Function)}.
     * @param logOutput the function to use for logging output
     */
    public static void outputRunResults(Results results, Function<String, String> logOutput) {
        if (results == null) {
            logOutput.apply("Scan failed. Unable to display results.");
        } else {
            List<String> stage1Detections = results.getStage1Detections();
            List<String> stage2Detections = results.getStage2Detections();
            if (stage1Detections.isEmpty() && stage2Detections.isEmpty()) {
                logOutput.apply(Constants.ANSI_GREEN + "Scan complete. No infected jars found." + Constants.ANSI_RESET);
            } else {
                logOutput.apply(Constants.ANSI_RED + "Scan complete. Infections found!" + Constants.ANSI_RESET);
                if (!stage1Detections.isEmpty()) {
                    logOutput.apply(Constants.ANSI_RED + "Stage 1 Infections (" + stage1Detections.size() + "):" + Constants.ANSI_RESET);
                    for (int i = 0; i < stage1Detections.size(); i++) {
                        String stage1Infection = stage1Detections.get(i);
                        int stage1InfectionNumber = i + 1;
                        logOutput.apply(Constants.ANSI_RED + "[" + stage1InfectionNumber + "] " + Constants.ANSI_WHITE + stage1Infection + Constants.ANSI_RESET);
                    }
                }
                if (!stage2Detections.isEmpty()) {
                    logOutput.apply(Constants.ANSI_RED + "Stage 2 Infections (" + stage2Detections.size() + "):" + Constants.ANSI_RESET);
                    for (int i = 0; i < stage2Detections.size(); i++) {
                        String stage2Infection = stage2Detections.get(i);
                        int stage2InfectionNumber = i + 1;
                        logOutput.apply(Constants.ANSI_RED + "[" + stage2InfectionNumber + "] " + Constants.ANSI_WHITE + stage2Infection + Constants.ANSI_RESET);
                    }
                }
            }
        }
    }

    /**
     * Runs a check and scans a folder for jars with malicious code signatures.
     * An object containing lists of infected files found during scan stages is returned.
     *
     * @param nThreads       the number of threads to use for scanning
     * @param dirToCheck     the directory to scan
     * @param emitWalkErrors whether to emit errors when walking the directory tree
     * @param logOutput      the function to use for logging output
     * @return a scan results object
     * @throws IllegalArgumentException if the specified directory does not exist or is not a directory, or if the
     *                                  number of threads is less than 1.
     * @throws IOException              if an I/O error occurs while walking the directory tree
     */
    public static Results run(int nThreads, Path dirToCheck, boolean emitWalkErrors, Function<String, String> logOutput) throws IOException, InterruptedException {
        // Output scan start
        long startTime = System.currentTimeMillis();
        logOutput.apply(Constants.ANSI_GREEN + "Starting All Scans - " + Constants.ANSI_RESET
                + "This may take a while depending on the size of the directories and JAR files.");

        // Check that specified directory is valid, exists, and is a directory
        File dirToCheckFile = dirToCheck.toFile();
        if (!dirToCheckFile.exists()) {
            throw new IllegalArgumentException("Specified directory does not exist: " + dirToCheck);
        }
        if (!dirToCheckFile.isDirectory()) {
            throw new IllegalArgumentException("Specified directory is not a directory: " + dirToCheck);
        }

        // Check number of threads is valid
        if (nThreads < 1) {
            throw new IllegalArgumentException("Number of threads must be at least 1");
        }

        // Create executor service with number of threads
        executorService = Executors.newFixedThreadPool(nThreads);

        // Scan all jars in path
        long stage1StartTime = System.currentTimeMillis();
        logOutput.apply(Constants.ANSI_GREEN + "Running Stage 1 Scan..." + Constants.ANSI_RESET);
        final List<String> stage1InfectionsList = new ArrayList<>();
        Files.walkFileTree(dirToCheck, new FileVisitor<Path>() {
            /**
             * Invoked for a directory before entries in the directory are visited.
             * @param dir  a reference to the directory
             * @param attrs the directory's basic attributes
             *
             * @return {@link FileVisitResult#CONTINUE}.
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            /**
             * Invoked for a file in a directory.
             * @param file a reference to the file
             * @param attrs the file's basic attributes
             *
             * @return {@link FileVisitResult#CONTINUE}
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                // Check if file is a scannable Jar file
                boolean isScannable = file.toString().toLowerCase().endsWith(Constants.JAR_FILE_EXTENSION);

                // If file is scannable, submit it to the executor service for scanning
                if (isScannable) {
                    executorService.submit(() -> {
                        try (JarFile scannableJarFile = new JarFile(file.toFile())) {
                            boolean infectionDetected = Detector.scan(scannableJarFile, file, logOutput);
                            if (infectionDetected) {
                                synchronized (stage1InfectionsList) {
                                    stage1InfectionsList.add(file.toString());
                                }
                            }
                        } catch (Exception e) {
                            if (emitWalkErrors) {
                                logOutput.apply("Failed to scan Jar file: " + file);
                                e.printStackTrace();
                            }
                        }
                    });
                }
                return FileVisitResult.CONTINUE;
            }

            /**
             * Invoked for a file that could not be visited.
             * @param file a reference to the file
             * @param exc the I/O exception that prevented the file from being visited
             *
             * @return {@link FileVisitResult#CONTINUE}
             */
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                if (emitWalkErrors) {
                    logOutput.apply("Failed to access file: " + file);
                }
                return FileVisitResult.CONTINUE;
            }

            /**
             * Invoked for a directory after entries in the directory, and all of their
             * descendants, have been visited. This method is also invoked when iteration
             * of the directory completes prematurely (by a {@link #visitFile visitFile}
             * failure, or by throwing an exception).
             *
             * @param dir a reference to the directory
             * @param exc {@code null} if the iteration of the directory completes without
             *          an error; otherwise the I/O exception that caused the iteration
             *          of the directory to complete prematurely
             *
             * @return {@link FileVisitResult#CONTINUE}
             */
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (exc != null && emitWalkErrors) {
                    logOutput.apply("Failed to access directory: " + dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        // Shutdown executor service and wait for all tasks to complete
        executorService.shutdown();
        boolean timedOut = !executorService.awaitTermination(100000, TimeUnit.DAYS);
        if (timedOut) {
            logOutput.apply("Timed out while waiting for Jar scanning to complete.");
        }
        long stage1EndTime = System.currentTimeMillis();
        long stage1Time = stage1EndTime - stage1StartTime;
        logOutput.apply(Constants.ANSI_GREEN + "Stage 1 Scan Complete - " + Constants.ANSI_RESET + "Took  " + stage1Time + "ms.");

        // Run stage 2 scan
        long stage2StartTime = System.currentTimeMillis();
        logOutput.apply(Constants.ANSI_GREEN + "Running Stage 2 Scan..." + Constants.ANSI_RESET);
        List<String> stage2InfectionsList = Detector.checkForStage2();
        long stage2EndTime = System.currentTimeMillis();
        long stage2Time = stage2EndTime - stage2StartTime;
        logOutput.apply(Constants.ANSI_GREEN + "Stage 2 Scan Complete - " + Constants.ANSI_RESET + "Took  " + stage2Time + "ms.");

        // Output scan end
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logOutput.apply(
                Constants.ANSI_GREEN + "All Scans Complete - " + Constants.ANSI_RESET + "Total " + totalTime + "ms.");


        // Build results and return
        return new Results(stage1InfectionsList, stage2InfectionsList);
    }

    /**
     * Cancels the current scan, if one is running, by shutting down the executor service.
     */
    public static void cancelScanIfRunning() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    /**
     * Checks the arguments passed to the program for validity.
     *
     * @param args the arguments passed to the program main method
     * @return {@code true} if the arguments are valid, {@code false} otherwise
     */
    private static boolean checkArgs(String[] args) {
        // Output usage information if no arguments are passed
        if (args.length == 0) {
            Gui.main(args);
            return false;
        }

        // Check if the number of threads is an integer
        int nThreads;
        try {
            nThreads = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.err.println("Invalid thread count, please use an integer.");
            return false;
        }

        // Check if the number of threads is positive
        if (nThreads <= 0) {
            System.err.println("Invalid thread count, must be greater than 0.");
            return false;
        }

        // Check if the directory to scan is valid
        File dirToCheck = null;
        try {
            dirToCheck = new File(args[1]);
        } catch (Exception e) {
            System.err.println("Invalid path, unable to load.");
            return false;
        }

        // Check if the directory to scan exists
        if (!dirToCheck.exists()) {
            System.err.println("Invalid path, directory does not exist.");
            return false;
        }

        // Check if the directory to scan is a directory
        if (!dirToCheck.isDirectory()) {
            System.err.println("Invalid path, not a directory.");
            return false;
        }

        // Return true if all checks pass
        return true;
    }
}
