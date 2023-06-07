package me.cortex.jarscanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.jar.JarFile;

public class Main {
    private static ExecutorService executorService;

    public static void main(String[] args) throws Exception {
        // Detector.scan(new JarFile("FloatingDamage.jar"), new
        // File("floatingdamage.jar").toPath());
        // if (true) return;

        // check args
        if (!checkArgs(args)) {
            return;
        }

        run(Integer.parseInt(args[0]), new File(args[1]).toPath(), args.length > 2 && Boolean.parseBoolean(args[2]), s -> {
            System.out.println(s);
            return s;
        });
    }

    public static void run(int threadCount, Path path, boolean emitWalkErrors, Function<String, String> output) {
        executorService = Executors.newFixedThreadPool(threadCount);
        Detector.checkForStage2();
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
                    JarFile jf;
                    try {
                        jf = new JarFile(file.toFile());
                    } catch (Exception e) {
                        if (finalEmitWalkErrors) {
                            System.out.println("Failed to access jar: " + file.toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                    executorService.submit(() -> Detector.scan(jf, file, output));
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

        System.out.println("Done scanning");
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
