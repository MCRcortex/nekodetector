package me.cortex.jarscanner.scanner;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.cafedude.io.ClassFileWriter;
import me.coley.cafedude.transform.IllegalStrippingTransformer;
import me.cortex.jarscanner.Main;
import me.cortex.jarscanner.detection.Detection;
import me.cortex.jarscanner.detection.DetectionProblem;
import me.cortex.jarscanner.scanner.summary.JarScanSummary;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import software.coley.llzip.ZipIO;
import software.coley.llzip.format.compression.ZipCompressions;
import software.coley.llzip.format.model.LocalFileHeader;
import software.coley.llzip.format.model.ZipArchive;
import software.coley.llzip.util.ByteData;
import software.coley.llzip.util.ByteDataUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.ClassReader.SKIP_FRAMES;

public class JarScanner implements Scanner<JarScanSummary> {
    private final List<Detection> detections;

    /**
     * @param detections Detections to look for.
     */
    public JarScanner(@Nonnull List<Detection> detections) {
        this.detections = Collections.unmodifiableList(detections);
    }

    @Nonnull
    @Override
    public JarScanSummary runScan(@Nonnull Path jarPath) throws IOException {
        JarScanSummary summary = new JarScanSummary(jarPath);

        // Parse the archive using mechanisms more in-line with how the JVM treats loading contents
        // when using -cp and -jar launch flags.
        ZipArchive jarArchive = ZipIO.readJvm(jarPath);

        // Get all class files. The JVM allows classes to end with a trailing '/'
        List<LocalFileHeader> classEntries = jarArchive.getNameFilteredLocalFiles(name ->
                name.endsWith(".class") || name.endsWith(".class/"));

        // Iterate over the classes, and compare them against the detection implementations.
        for (LocalFileHeader classEntry : classEntries) {
            try (ByteData classData = ZipCompressions.decompress(classEntry)) {
                String classFileName = classEntry.getFileNameAsString();
                if (classData.length() > Integer.MAX_VALUE - 8) { // this should never happen???
                    // Class file size too large to represent in byte[]
                    summary.addProblem("Class file size too large to represent in byte[]: " + classFileName);
                } else {
                    // Parse the class
                    byte[] classBytes = ByteDataUtil.toByteArray(classData);
                    try {
                        scanClass(jarPath, classBytes, summary);
                    } catch (Throwable ex) {
                        // Assuming the exception is from ASM problems, try to handle the class again after
                        // patching with cafedude. If that fails, then oh well.
                        try {
                            // Patch
                            ClassFileReader reader = new ClassFileReader();
                            ClassFile classFile = reader.read(classBytes);
                            new IllegalStrippingTransformer(classFile).transform();
                            classBytes = new ClassFileWriter().write(classFile);

                            // Try scanning again
                            scanClass(jarPath, classBytes, summary);
                        } catch (Throwable exAgain) {
                            // Wasn't an ASM problem, record and continue to the next class.
                            summary.addProblem(new DetectionProblem(exAgain, "Failed to scan class: " + classFileName));
                        }
                    }
                }
            }
        }

        return summary;
    }

    /**
     * Scans the given class with provided {@link #detections}.
     *
     * @param classBytes Raw bytes of class.
     * @param summary    Summary to feed into
     */
    private void scanClass(Path jarPath, @Nonnull byte[] classBytes, @Nonnull JarScanSummary summary) {
        // Populate tree node from class bytes
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(classBytes);
        reader.accept(node, SKIP_FRAMES);

        // Pass node to all detections
        for (Detection detection : this.detections)
            detection.scan(jarPath, node, summary);
    }
}
