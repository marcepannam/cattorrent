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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Gui {

    public static void main(String args[]) throws InterruptedException, IOException {
        Controller c = new Controller();
        SwingUtilities.invokeLater(() -> createAndShowGUI(c));
        ExecutorService eservice = Executors.newCachedThreadPool();
        int idx = 0;
        while (true) {
            TimeUnit.SECONDS.sleep(1);
            while (idx < c.downloaders.size()) {
                eservice.execute(c.downloaders.get(idx++));
            }
            System.out.println(idx);
            if (idx>100) break;
            Thread.yield();
        }
        eservice.shutdown();
    }

    public static void createAndShowGUI(Controller c) {
        JFrame myWindow = new JFrame();
        ArrayList<File> files = new ArrayList<>();
        myWindow.setSize(600, 400);
        myWindow.setTitle("Cattorrent");
        myWindow.setLayout(null);
        myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel logArea = createLog(myWindow);
        JLabel fileArea = createFileLabel(myWindow);
        createIcon(myWindow);
        createMenu(myWindow, logArea, fileArea, files);
        JPanel downloadsArea = createDownloadsPanel(myWindow);
        createDownloadButton(myWindow, logArea, downloadsArea, files, c);
        myWindow.setVisible(true);
    }

    public static void logEvent(JLabel logArea, String msg){
        SwingUtilities.invokeLater(() ->{
            StringBuilder sb = new StringBuilder(logArea.getText());
            sb.insert(6, Instant.now() + " " + msg + "<br/>");
            logArea.setText(sb.toString());
        });
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

    private static JPanel createDownloadsPanel(JFrame myWindow) {
        JPanel downloadsPanel = new JPanel();
        downloadsPanel.setLayout(new BoxLayout(downloadsPanel, BoxLayout.Y_AXIS));
        downloadsPanel.setBackground(Color.LIGHT_GRAY);
        downloadsPanel.setBounds(0, 70, 600, 180);
        myWindow.add(downloadsPanel);
        return downloadsPanel;
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
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        myWindow.add(logPanel);
        return logArea;
    }

    private static void createDownloadButton(
            JFrame myWindow, JLabel logArea, JPanel downloadsArea, ArrayList<File> files, Controller c) {
        JButton buttonDownloadFile = new JButton("Download");
        buttonDownloadFile.setBounds(0, 40, 160, 30);
        myWindow.add(buttonDownloadFile);
        buttonDownloadFile.addActionListener(actionEvent -> {
            if (files.isEmpty()) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(myWindow,
                        "Please select file for download:\nFile -> Open"));
                logEvent(logArea, "Invalid attempt of download with no file selected");
                return;
            }
            createDownloadProgress(myWindow, logArea, downloadsArea, files, c);
        });
    }

    private static void createDownloadProgress(
            JFrame myWindow, JLabel logArea, JPanel downloadsArea, ArrayList<File> files, Controller c) {
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        if (!c.startDownload(files.get(files.size()-1), progressBar, logArea, myWindow)) return;
        progressBar.setStringPainted(true);
        JLabel fileNameLabel = new JLabel(files.get(files.size()-1).getName());
        JPanel fileDownload = new JPanel(new BorderLayout());
        fileDownload.setMaximumSize(new Dimension(300, 30));
        fileDownload.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileDownload.setBackground(Color.LIGHT_GRAY);
        fileDownload.setOpaque(true);
        fileDownload.add(progressBar, BorderLayout.EAST);
        fileDownload.add(fileNameLabel, BorderLayout.WEST);
        downloadsArea.add(fileDownload);
        downloadsArea.validate();
    }
}
