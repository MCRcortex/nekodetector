package me.cortex.jarscanner;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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
        createAndDisplayGui();
    }

    private static void createAndDisplayGui() {
        USING_GUI = true;
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
}
