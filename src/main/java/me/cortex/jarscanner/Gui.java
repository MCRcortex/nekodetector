package me.cortex.jarscanner;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public class Gui {
    private static TextArea textArea;
    private static JButton searchDirPicker;
    private static Path searchDir = Path.of(System.getProperty("user.home"));

    public static void main(String[] args) {
        createAndDisplayGui();
    }

    private static void createAndDisplayGui() {
        JPanel panel = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel searchDirPickerLabel = new JLabel("Select Search Directory:");
        searchDirPickerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        searchDirPicker = new JButton(Path.of(System.getProperty("user.home")).toFile().getName());
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

        JButton runButton = new JButton("Run!");
        runButton.addActionListener(e -> {
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
        panel.add(searchDirPickerLabel);
        panel.add(searchDirPicker);
        panel3.add(createTextArea());
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(panel2, BorderLayout.CENTER);
        frame.getContentPane().add(panel3, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private static JScrollPane createTextArea() {
        textArea = new TextArea(20, 60);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);

        return scrollPane;
    }
}
