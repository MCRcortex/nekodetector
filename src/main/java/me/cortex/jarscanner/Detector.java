package me.cortex.jarscanner;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.*;

/**
 * Detector class for Nekodetector, which scans for malicious code signatures from the Nekoclient malware.
 * <p>ORIGINAL SOURCE: https://github.com/MCRcortex/nekodetector</p>
 *
 * @author MCRcortex (https://github.com/MCRcortex)
 * @author Huskydog9988 (https://github.com/Huskydog9988)
 */
public class Detector {

    /**
     * Scans for malicious code signatures in the specified {@link JarFile} located at the specified {@link Path}.
     *
     * @param file The {@link JarFile} to scan.
     * @param path The {@link Path} of the {@link JarFile} to scan.
     * @return {@code true} if a signature match was found, otherwise {@code false}.
     */
    public static boolean scan(JarFile file, Path path, Function<String, String> output) {
        // Create boolean to store whether a signature match was found
        boolean signatureMatchFound = false;

        // Check Jar file for infection signatures
        try {
            // Scan .class files for signatures
            signatureMatchFound = file.stream()
                    .filter(entry -> entry.getName().endsWith(Constants.CLASS_FILE_EXTENSION))
                    .anyMatch(entry -> {
                        try {
                            return scanClass(getByteArray(file.getInputStream(entry)));
                        } catch (IOException e) {
                            output.apply("Failed to scan class in Jar file [" + path + "] due to an IO error: " + entry.getName());
                            output.apply("Error:" + e.getMessage());
                            return false;
                        } catch (IllegalArgumentException e) {
                            output.apply("Failed to scan class in Jar file [" + path + "] due to a parsing error: " + entry.getName());
                            output.apply(
                                    "This is likely due to a malformed class file or an issue with the JAR file itself.");
                            output.apply("Error:" + e.getMessage());

                            return false;
                        }
                    });
        } catch (Exception e) {
            output.apply("Failed to scan Jar file: " + path);
            output.apply("Error:" + e.getMessage());
        } finally {
            // Close Jar file
            try {
                file.close();
            } catch (IOException e) {
                output.apply("Failed to close Jar file after scan: " + path);
                output.apply("Error:" + e.getMessage());
            }
        }

        // Return whether a signature match was found
        return signatureMatchFound;
    }

    private static byte[] getByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    private static final AbstractInsnNode[] SIG1 = new AbstractInsnNode[] {
            new TypeInsnNode(NEW, "java/lang/String"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new TypeInsnNode(NEW, "java/lang/String"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getConstructor",
                    "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKESPECIAL, "java/net/URL", "<init>",
                    "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Constructor", "newInstance",
                    "([Ljava/lang/Object;)Ljava/lang/Object;"),
            new MethodInsnNode(INVOKESTATIC, "java/lang/Class", "forName",
                    "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"),
    };

    private static final AbstractInsnNode[] SIG2 = new AbstractInsnNode[] {
            new MethodInsnNode(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;"),
            new MethodInsnNode(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "concat",
                    "(Ljava/lang/String;)Ljava/lang/String;"), // TODO:FIXME: this might not be in all of them
            new MethodInsnNode(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B"),
            new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/io/File", "getPath", "()Ljava/lang/String;"),
            new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "([Ljava/lang/String;)Ljava/lang/Process;"),
    };

    // The IP
    private static final AbstractInsnNode[] SIG3 = new AbstractInsnNode[] {
            new IntInsnNode(BIPUSH, 56),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new InsnNode(ICONST_1),
            new IntInsnNode(BIPUSH, 53),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new InsnNode(ICONST_2),
            new IntInsnNode(BIPUSH, 46),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new InsnNode(ICONST_3),
            new IntInsnNode(BIPUSH, 50),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new InsnNode(ICONST_4),
            new IntInsnNode(BIPUSH, 49),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new InsnNode(ICONST_5),
            new IntInsnNode(BIPUSH, 55),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new IntInsnNode(BIPUSH, 6),
            new IntInsnNode(BIPUSH, 46),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new IntInsnNode(BIPUSH, 7),
            new IntInsnNode(BIPUSH, 49),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new IntInsnNode(BIPUSH, 8),
            new IntInsnNode(BIPUSH, 52),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new IntInsnNode(BIPUSH, 9),
            new IntInsnNode(BIPUSH, 52),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new IntInsnNode(BIPUSH, 10),
            new IntInsnNode(BIPUSH, 46),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new IntInsnNode(BIPUSH, 11),
            new IntInsnNode(BIPUSH, 49),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new IntInsnNode(BIPUSH, 12),
            new IntInsnNode(BIPUSH, 51),
            new InsnNode(BASTORE),
            new InsnNode(DUP),
            new IntInsnNode(BIPUSH, 13),
            new IntInsnNode(BIPUSH, 48)
    };

    private static boolean same(AbstractInsnNode a, AbstractInsnNode b) {
        if (a instanceof TypeInsnNode) {
            TypeInsnNode aa = (TypeInsnNode) a;
            return aa.desc.equals(((TypeInsnNode) b).desc);
        }
        if (a instanceof MethodInsnNode) {
            MethodInsnNode aa = (MethodInsnNode) a;
            return aa.owner.equals(((MethodInsnNode) b).owner)
                    && aa.name.equals(((MethodInsnNode) b).name)
                    && aa.desc.equals(((MethodInsnNode) b).desc);
        }
        if (a instanceof InsnNode) {
            return true;
        }
        throw new IllegalArgumentException("TYPE NOT ADDED");
    }

    public static boolean scanClass(byte[] clazz) {
        ClassReader reader = new ClassReader(clazz);
        ClassNode node = new ClassNode();
        try {
            reader.accept(node, 0);
        } catch (Exception e) {
            return false;// Yes this is very hacky but should never happen with valid clasees
        }
        for (MethodNode method : node.methods) {
            {
                // Method 1, this is a hard detect, if it matches this it is 100% chance
                // infected
                boolean match = true;
                int j = 0;
                for (int i = 0; i < method.instructions.size() && j < SIG1.length; i++) {
                    AbstractInsnNode insn = method.instructions.get(i);
                    if (insn.getOpcode() == -1) {
                        continue;
                    }
                    if (insn.getOpcode() == SIG1[j].getOpcode()) {
                        if (!same(insn, SIG1[j++])) {
                            match = false;
                            break;
                        }
                    }
                }
                if (j != SIG1.length) {
                    match = false;
                }
                if (match) {
                    return true;
                }
            }

            {
                // Method 2, this is a near hard detect, if it matches this it is 95% chance
                // infected
                boolean match = false;
                outer: for (int q = 0; q < method.instructions.size(); q++) {
                    int j = 0;
                    for (int i = q; i < method.instructions.size() && j < SIG2.length; i++) {
                        AbstractInsnNode insn = method.instructions.get(i);
                        if (insn.getOpcode() != SIG2[j].getOpcode()) {
                            continue;
                        }

                        if (insn.getOpcode() == SIG2[j].getOpcode()) {
                            if (!same(insn, SIG2[j++])) {
                                continue outer;
                            }
                        }
                    }
                    if (j == SIG2.length) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    return true;
                }
            }

            // Method 3, this looks for a byte array with the IP. This is a likely match.
            {
                boolean match = false;
                // where we're looking in the SIG3 array
                int pos = 0;
                for (int i = 0; i < method.instructions.size(); i++) {
                    if (pos == SIG3.length) {
                        break;
                    }
                    AbstractInsnNode insn = method.instructions.get(i);
                    if (insn.getOpcode() == -1) {
                        continue;
                    }
                    if (insn.getOpcode() == SIG3[pos].getOpcode()) {
                        // the opcode matches

                        if (SIG3[pos].getType() == AbstractInsnNode.INT_INSN) {
                            // check if operand matches
                            IntInsnNode iInsn = (IntInsnNode) insn;
                            IntInsnNode sigInsn = (IntInsnNode) SIG3[pos];
                            if (iInsn.operand == sigInsn.operand) {
                                // operands match
                                match = true;
                                pos++;
                            }
                        } else {
                            // this is a regular InsnNode; just match
                            match = true;
                            pos++;
                        }
                    } else {
                        match = false;
                        pos = 0;
                    }
                }

                if (match) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks for signs of stage 2 infection and returns a list of files that are flagged as suspicious.
     * Based on:
     * https://github.com/fractureiser-investigation/fractureiser#am-i-infected
     */
    public static List<String> checkForStage2() {
        // Create list to store suspicious files found
        List<String> suspiciousFilesFound = new ArrayList<>();

        // windows checks
        Path windowsStartupDirectory = (Objects.isNull(System.getenv("APPDATA"))
                ? Paths.get(System.getProperty("user.home"), "AppData", "Roaming")
                : Paths.get(System.getenv("APPDATA"), new String[0]))
                .resolve(Paths.get("Microsoft", "Windows", "Start Menu", "Programs", "Startup"));
        boolean windows = Files.isDirectory(windowsStartupDirectory, new LinkOption[0])
                && Files.isWritable(windowsStartupDirectory);

        String[] maliciousFiles = {
                ".ref",
                "client.jar",
                "lib.dll",
                "libWebGL64.jar",
                "run.bat"
        };

        if (windows) {
            // only checking for the folder because the file can be renamed
            File edgeFolder = new File(System.getenv("APPDATA") + "\\Microsoft Edge");
            if (edgeFolder.exists()) {
                suspiciousFilesFound.add(edgeFolder.getAbsolutePath());
            }

            File startFolder = new File("Microsoft\\Windows\\Start Menu\\Programs\\Startup");
            // get all files in the startup folder, and check if they match the malicious
            if (startFolder.exists() && startFolder.isDirectory()) {
                File[] startFiles = startFolder.listFiles();

                for (int i = 0; i < startFiles.length; i++) {

                    for (int j = 0; j < maliciousFiles.length; j++) {
                        if (startFiles[i].getName().equals(maliciousFiles[j])) {
                            suspiciousFilesFound.add(startFiles[i].getAbsolutePath());
                        }
                    }
                }
            }
        }

        // linux checks
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            File file = new File("~/.config/.data/lib.jar");
            if (file.exists()) {
                suspiciousFilesFound.add(file.getAbsolutePath());
            }
        }

        // Return list of suspicious files found
        return suspiciousFilesFound;
    }
}
