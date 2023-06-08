package me.cortex.jarscanner;

/**
 * Constants class for Nekodetector.
 * <p>ORIGINAL SOURCE: https://github.com/MCRcortex/nekodetector</p>
 *
 * @author mica-alex (https://github.com/mica-alex)
 * @author Huskydog9988 (https://github.com/Huskydog9988)
 */
public class Constants {

    /**
     * ANSI code for color red.
     */
    public static final String ANSI_RED = "\u001B[31m";


    /**
     * ANSI code for color white.
     */
    public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * ANSI code for color green.
     */
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     * ANSI code for reset.
     */
    public static final String ANSI_RESET = "\u001B[0m";

    /**
     * The Java class file extension that is used to scan for malicious code signatures.
     */
    public static final String CLASS_FILE_EXTENSION = ".class";

    /**
     * The Java Jar file extension that is used to scan for malicious code signatures.
     */
    public static final String JAR_FILE_EXTENSION = ".jar";
}
