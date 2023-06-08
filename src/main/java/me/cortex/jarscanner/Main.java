package me.cortex.jarscanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.jar.JarFile;

public class Main {
    public static AtomicInteger matches = new AtomicInteger(0);

    private static ExecutorService executorService;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) throws Exception {
        System.out.println(ANSI_GREEN + "Starting Scan -" + ANSI_RESET
                + " this may take a while depending on the size of the directories and JAR files.");
        // Detector.scan(new JarFile("FloatingDamage.jar"), new
        // File("floatingdamage.jar").toPath());
        // if (true) return;

        // check args
        if (!checkArgs(args)) {
            return;
        }

        run(Integer.parseInt(args[0]), new File(args[1]).toPath(), args.length > 2 && Boolean.parseBoolean(args[2]),
                s -> {
                    System.out.println(s);
                    return s;
                });

    }

    public static void run(int threadCount, Path path, boolean emitWalkErrors, Function<String, String> output) {
        long start = System.currentTimeMillis();

        executorService = Executors.newFixedThreadPool(threadCount);
        Detector.checkForStage2(s -> {
            System.out.println(s);
            return s;
        });
        boolean finalEmitWalkErrors = emitWalkErrors;

        // scan all jars in path
        try {
            Files.walkFileTree(path, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.toString().endsWith(".jar")) {
                        return FileVisitResult.CONTINUE;
                    }
                    // System.out.println("Looking at file " + file);
                    JarFile jf;
                    try {
                        jf = new JarFile(file.toFile());
                    } catch (Exception e) {
                        if (finalEmitWalkErrors) {
                            output.apply("Failed to access jar: " + file);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    Future<?> handler = executorService.submit(() -> Detector.scan(jf, file, output));
                    try {
                        handler.get(Duration.ofSeconds(600).toMillis(), TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        handler.cancel(true);
                        System.out.println("Timedout scanning jar: " + file.toString());
                        System.out.println("Error: " + e.toString());
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(100000, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(
                ANSI_GREEN + "Scan Complete - " + ANSI_RESET + Main.matches + " matches found. - " + ANSI_RESET
                        + " took " + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * Checks the arguments passed to the program
     * 
     * @param args
     * @return
     */
    private static boolean checkArgs(String[] args) {
        if (args.length == 0) {
            Gui.main(args);
            return false;
        }

        try {
            Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("Invalid thread count, please use an integer");
            return false;
        }

        try {
            new File(args[1]).toPath();
        } catch (Exception e) {
            System.out.println("Invalid path, must be a directory");
            return false;
        }

        return true;
    }
}
