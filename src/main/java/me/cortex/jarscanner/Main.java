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

        // check for stage 2 infection
        Detector.checkForStage2();

        executorService = Executors.newFixedThreadPool(Integer.parseInt(args[0]));
        Path path = new File(args[1]).toPath();
        boolean emitWalkErrors = false;
        if (args.length > 2) {
            emitWalkErrors = args[2].equals("y");
        }
        boolean finalEmitWalkErrors = emitWalkErrors;

        // scan all jars in path
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
                executorService.submit(() -> Detector.scan(jf, file));
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
        executorService.shutdown();
        executorService.awaitTermination(100000, TimeUnit.DAYS);

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
            System.out.println(
                    "Usage: java -jar scanner.jar <threads:int> <scanpath:string> <optional 'y' for failed jar file opening>");

            System.out.println("Example: java -jar scanner.jar 4 C:\\Users\\Cortex\\Desktop\\ y");
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
