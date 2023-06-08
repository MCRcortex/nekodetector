package me.cortex.jarscanner;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;

public class Gui {
    public static boolean USING_GUI;
    private static JTextArea textArea;
    private static JButton searchDirPicker;
    private static Path searchDir = new File(System.getProperty("user.home")).toPath();

    private static Thread scanThread;

    public static void main(String[] args) {
        boolean darkTheme = true;
        for (String arg : args) {
            if ("--lightTheme".equals(arg)) {
                darkTheme = false;
                break;
            }
        }

        createAndDisplayGui(darkTheme);
    }

    private static void setToDarkTheme() {
        try {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            UIManager.put("creditsBackground", new ColorUIResource(0x3c3f41));
            MetalLookAndFeel.setCurrentTheme(new DarkTheme());
            // Force metal look and feel
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Throwable e) {
            System.err.println("Unsupported dark theme look and feel");
        }
    }

    private static void createAndDisplayGui(boolean darkTheme) {
        USING_GUI = true;
        if (darkTheme) {
            setToDarkTheme();
        }
        textArea = new JTextArea(20, 40);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel searchDirPickerLabel = new JLabel("Select Search Directory:");
        searchDirPickerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        searchDirPicker = new JButton(new File(System.getProperty("user.home")).getName());
        searchDirPicker.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setFileHidingEnabled(false);
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                searchDir = file.toPath();
                searchDirPicker.setText(file.getName());
            }
        });

        JButton credsButton = new JButton("Credits");
        credsButton.addActionListener(e -> {
            showCredits();
        });

        // Auto scroll checkbox
        JCheckBox autoScrollCheckBox = new JCheckBox("Auto-scroll");
        autoScrollCheckBox.setSelected(true);

        // Cancel button
        JButton cancelButton = new JButton("Cancel!");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> {
            if (scanThread != null) {
                Main.cancelScanIfRunning();
                scanThread.interrupt();
            }
        });

        // Run button
        JButton runButton = new JButton("Run!");
        runButton.addActionListener(e -> {
            scanThread = new Thread(() -> {
                // Disable buttons (enable cancel)
                searchDirPicker.setEnabled(false);
                runButton.setEnabled(false);
                cancelButton.setEnabled(true);

                // Run scan
                try {
                    // Create log output function
                    Function<String, String> logOutput = out -> {
                        String processedOut = out.replace(Constants.ANSI_RED, "").replace(Constants.ANSI_GREEN, "").replace(Constants.ANSI_WHITE, "").replace(Constants.ANSI_RESET, "");
                        textArea.append(processedOut + "\n");
                        // Scroll to bottom of text area if auto-scroll is enabled
                        if (autoScrollCheckBox.isSelected()) {
                            textArea.setCaretPosition(textArea.getDocument().getLength());
                        }
                        return out;
                    };

                    Results run = Main.run(4, searchDir, true, logOutput);
                    Main.outputRunResults(run, logOutput);
                    textArea.append("Done scanning!");
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException || ex instanceof RejectedExecutionException) {
                        textArea.append("Scan cancelled!" + "\n");
                    } else {
                        textArea.append("Error while running scan!" + "\n");
                    }
                }

                // Re-enable buttons (disable cancel)
                searchDirPicker.setEnabled(true);
                runButton.setEnabled(true);
                cancelButton.setEnabled(false);
            });
            scanThread.start();
        });

        // Create grid bag layout
        frame.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(runButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(cancelButton, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(credsButton, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        buttonPanel.add(autoScrollCheckBox, gridBagConstraints);

        // Add button panel to top right of frame
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        frame.getContentPane().add(buttonPanel, gridBagConstraints);

        // Create panel for search dir picker
        JPanel searchDirPickerPanel = new JPanel();
        searchDirPickerPanel.add(searchDirPickerLabel);
        searchDirPickerPanel.add(searchDirPicker);

        // Add search dir picker panel to top left of frame
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        frame.getContentPane().add(searchDirPickerPanel, gridBagConstraints);

        // Create panel for log area
        JScrollPane logAreaPanel = createTextArea();

        // Add log area panel to bottom of frame
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        frame.getContentPane().add(logAreaPanel, gridBagConstraints);

        // Pack and display frame
        frame.pack();
        frame.setTitle("Neko Detector");
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(600, 300));
        frame.setMaximumSize(new Dimension(600, 300));
        frame.setPreferredSize(new Dimension(600, 300));

    }

    private static String[] credits = new String[]{
            "Credits to:",
            "Cortex, for decompiling and deobfuscating the malware, and making the initial detector.",
            "D3SL: Extensive reverse engineering, early discovery learned of later",
            "Nia: Extensive Stage 3 reverse engineering",
            "Jasmine: Coordination, writing the decompiler we've been using (Quiltflower)",
            "Emi: Coordination, initial discovery (for this team), and early research",
            "williewillus: Coordination, journalist",
            "quat: Documentation, initial infected sample research",
            "xylemlandmark: Coordination of documentation, crowd control",
            "Vazkii: they're pretty neat",
            "Elocin: Originally finding the malware itself"
    };

    private static void showCredits() {
        JFrame frame = new JFrame("Credits");
        JTextArea credits = new JTextArea();
        credits.setEditable(false);
        Color creditsBackground = UIManager.getColor("creditsBackground");
        if (creditsBackground != null) {
            credits.setBackground(creditsBackground);
        }
        for (String string : Gui.credits) {
            credits.append(string + "\n");
        }
        frame.add(credits);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private static JScrollPane createTextArea() {
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);

        return scrollPane;
    }

    private static class DarkTheme extends DefaultMetalTheme {
        private static final ColorUIResource PRIMARY_1 = new ColorUIResource(0x808080);
        private static final ColorUIResource PRIMARY_2 = new ColorUIResource(0x606060);
        private static final ColorUIResource PRIMARY_3 = new ColorUIResource(0x4d4d4d);
        private static final ColorUIResource SECONDARY_1 = new ColorUIResource(102, 102, 102);
        private static final ColorUIResource SECONDARY_2 = new ColorUIResource(85, 85, 85);
        private static final ColorUIResource SECONDARY_3 = new ColorUIResource(0x3c3f41);
        private static final ColorUIResource WHITE = new ColorUIResource(0x2b2b2b);
        private static final ColorUIResource BLACK = new ColorUIResource(255, 255, 255);

        @Override
        public String getName() {
            return "Dark theme";
        }

        @Override
        protected ColorUIResource getPrimary1() {
            return PRIMARY_1;
        }

        @Override
        protected ColorUIResource getPrimary2() {
            return PRIMARY_2;
        }

        @Override
        protected ColorUIResource getPrimary3() {
            return PRIMARY_3;
        }

        @Override
        public ColorUIResource getSecondary1() {
            return SECONDARY_1;
        }

        @Override
        public ColorUIResource getSecondary2() {
            return SECONDARY_2;
        }

        @Override
        public ColorUIResource getSecondary3() {
            return SECONDARY_3;
        }

        @Override
        protected ColorUIResource getWhite() {
            return WHITE;
        }

        @Override
        protected ColorUIResource getBlack() {
            return BLACK;
        }
    }
}
