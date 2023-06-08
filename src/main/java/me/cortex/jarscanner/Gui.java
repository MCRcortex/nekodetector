package me.cortex.jarscanner;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public class Gui {
    public static boolean USING_GUI;
    private static JTextArea textArea;
    private static JButton searchDirPicker;
    private static Path searchDir = new File(System.getProperty("user.home")).toPath();

    public static void main(String[] args) {
        createAndDisplayGui();
    }

    private static void createAndDisplayGui() {
        USING_GUI = true;
        textArea = new JTextArea(20, 40);
        JPanel panel = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel credsPanel = new JPanel();
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

        JButton runButton = new JButton("Run!");
        runButton.addActionListener(e -> {
            textArea.append("\n" + "Starting Scan -"
                               + " this may take a while depending on the size of the directories and JAR files.");
            Main.run(4, searchDir, true, out -> {
                textArea.append(out + "\n");
                return out;
            });

            Detector.checkForStage2(s -> {
                textArea.append(s + "\n");
                return s;
            });

            textArea.append("Done scanning!");
        });
        panel2.add(runButton);
        panel2.add(credsButton);
        panel.add(searchDirPickerLabel);
        panel.add(searchDirPicker);
        panel3.add(createTextArea());
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(panel2, BorderLayout.CENTER);
        frame.getContentPane().add(panel3, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(600, 300));
        frame.setMaximumSize(new Dimension(600, 300));
        frame.setPreferredSize(new Dimension(600, 300));

    }

    private static String[] credits = new String[] {
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
