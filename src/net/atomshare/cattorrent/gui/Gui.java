package net.atomshare.cattorrent.gui;

import net.atomshare.cattorrent.Controller;
import net.atomshare.cattorrent.Downloader;

import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Gui {

    public static void main(String args[]) throws InterruptedException, IOException {
        Controller c = new Controller();
        SwingUtilities.invokeLater(() -> createAndShowGUI(c));
        while (true) {
            boolean started = false;
            TimeUnit.SECONDS.sleep(1);
            for (Downloader d : c.downloaders) {
                started = true;
                d.run();
            }
            if (started) break;
        }
    }

    public static void createAndShowGUI(Controller c) {
        JFrame myWindow = new JFrame();
        ArrayList<File> files = new ArrayList<>();
        myWindow.setSize(600, 400);
        myWindow.setTitle("Cattorrent");
        myWindow.setLayout(null);
        myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel logArea = createLog(myWindow);
        myWindow.add(new JLabel());
        JLabel fileArea = createFileLabel(myWindow);
        createIcon(myWindow);
        createMenu(myWindow, logArea, fileArea, files);
        createDownloadButton(myWindow, logArea, files, c);
        myWindow.setVisible(true);
    }

    public static void logEvent(JLabel logArea, String msg){
        StringBuilder sb = new StringBuilder(logArea.getText());
        sb.insert(6, Instant.now() + " " + msg + "<br/>");
        String logMsg = sb.toString();
        SwingUtilities.invokeLater(() -> logArea.setText(logMsg));
    }

    private static JLabel createFileLabel(JFrame myWindow) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBounds(0, 0, 160, 40);
        panel.setOpaque(true);
        panel.setBackground(Color.GRAY);
        JLabel fileArea = new JLabel("<html>No file selected</html>", SwingConstants.CENTER);
        panel.add(fileArea, BorderLayout.CENTER);
        myWindow.add(panel);
        return fileArea;
    }

    private static void createIcon(JFrame myWindow) {
        URL imageURL = Gui.class.getResource("catlogo.png");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            JLabel label = new JLabel(icon, JLabel.RIGHT);
            label.setBackground(Color.WHITE);
            label.setOpaque(true);
            label.setBounds(160, 0, 440, 70);
            myWindow.add(label);
        } else {
            System.err.println("Unable to locate image in the filesystem");
        }
    }

    private static void createMenu(JFrame myWindow, JLabel logArea, JLabel fileArea, ArrayList<File> files) {
        //create a menu bar
        JMenuBar menuBar = new JMenuBar();

        //create menus
        JMenu fileMenu = new JMenu("File");
        JMenu aboutMenu = new JMenu("About");

        //create menu items
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener( actionEvent -> {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Open torrent file");
            fileChooser.setCurrentDirectory(new File("\\"));
            int option = fileChooser.showOpenDialog(new JButton());
            switch (option) {
                case (JFileChooser.APPROVE_OPTION) :
                    fileArea.setText("<html>File: " + fileChooser.getSelectedFile().getName() + "</html>");
                    logEvent(logArea, " File for download: " +
                            fileChooser.getSelectedFile().getName());
                    files.add(fileChooser.getSelectedFile());
                    break;
                case (JFileChooser.CANCEL_OPTION) :
                    break;
                case (JFileChooser.ERROR_OPTION) :
                    logEvent(logArea, "Error occurred while choosing a file.");
                    break;
            }
        });
        fileMenu.add(openMenuItem);
        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        myWindow.setJMenuBar(menuBar);
    }

    private static JLabel createLog(JFrame myWindow) {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBounds(0, 250, 600, 100);
        logPanel.add(new JLabel("Event log"), BorderLayout.NORTH);
        JLabel logArea = new JLabel();
        logArea.setOpaque(true);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.WHITE);
        logArea.setVerticalAlignment(JLabel.TOP);
        logArea.setText("<html></html>");
        JScrollPane scrollPane = new JScrollPane(logArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        myWindow.add(logPanel);
        return logArea;
    }

    private static void createDownloadButton(JFrame myWindow, JLabel logArea, ArrayList<File> files, Controller c) {
        JButton buttonDownloadFile = new JButton("Download");
        buttonDownloadFile.setBounds(0, 40, 160, 30);
        JPanel downloadsPanel = new JPanel();
        downloadsPanel.setLayout(new BoxLayout(downloadsPanel, BoxLayout.Y_AXIS));
        downloadsPanel.setBackground(Color.LIGHT_GRAY);
        downloadsPanel.setBounds(0, 70, 600, 180);
        myWindow.add(downloadsPanel);
        myWindow.add(buttonDownloadFile);
        buttonDownloadFile.addActionListener( actionEvent -> {
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            if (files.isEmpty()) {
                logEvent(logArea,"There is no file for download");
                return;
            }
            JLabel fileNameLabel = new JLabel(files.get(files.size()-1).getName());
            JPanel fileDownload = new JPanel(new BorderLayout());
            fileDownload.setMaximumSize(new Dimension(300, 30));
            fileDownload.setAlignmentX(Component.LEFT_ALIGNMENT);
            fileDownload.setBackground(Color.LIGHT_GRAY);
            fileDownload.setOpaque(true);
            fileDownload.add(progressBar, BorderLayout.EAST);
            fileDownload.add(fileNameLabel, BorderLayout.WEST);
            downloadsPanel.add(fileDownload);
            downloadsPanel.validate();
            c.startDownload(files.get(files.size()-1), progressBar, logArea);
        });
    }
}
