package net.atomshare.cattorrent.gui;

import net.atomshare.cattorrent.Controller;

import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;

public class Gui {

    public static void main(String args[]) {
        SwingUtilities.invokeLater(Gui::createAndShowGUI);
    }

    public static void createAndShowGUI() {
        JFrame myWindow = new JFrame();
        ArrayList<File> files = new ArrayList<>();
        myWindow.setSize(600, 400);
        myWindow.setTitle("Cattorrent");
        myWindow.setLayout(null);
        myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel logArea = createLog(myWindow);
        createMenu(myWindow, logArea, files);
        createDownloadButton(myWindow, logArea, files);
        myWindow.setVisible(true);
    }

    public static void logEvent(JLabel logArea, String msg){
        StringBuilder sb = new StringBuilder(logArea.getText());
        sb.insert(6, Instant.now() + " " + msg + "<br/>");
        logArea.setText(sb.toString());
    }

    private static void createMenu(JFrame myWindow, JLabel logArea, ArrayList<File> files) {
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

    private static void createDownloadButton(JFrame myWindow, JLabel logArea, ArrayList<File> files) {
        JButton buttonDownloadFile = new JButton("Download");
        buttonDownloadFile.setBounds(0, 50, 140, 20);
        JPanel downloadsPanel = new JPanel();
        downloadsPanel.setLayout(new BoxLayout(downloadsPanel, BoxLayout.PAGE_AXIS));
        downloadsPanel.setBackground(Color.LIGHT_GRAY);
        downloadsPanel.setBounds(0, 70, 600, 180);
        myWindow.add(downloadsPanel);
        myWindow.add(buttonDownloadFile, BorderLayout.NORTH);
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
            fileDownload.add(progressBar, BorderLayout.EAST);
            fileDownload.add(fileNameLabel, BorderLayout.WEST);
            downloadsPanel.add(fileDownload);
            downloadsPanel.validate();
            Controller.startDownload(files.get(files.size()-1), progressBar);
        });
    }
}
