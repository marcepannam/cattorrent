package net.atomshare.cattorrent.gui;

import net.atomshare.cattorrent.Controller;

import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Gui {

    public static void createMenu(JFrame myWindow, JLabel logArea, ArrayList<File> files) {
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
                    logArea.setText("<html>Opening file: " +
                            fileChooser.getSelectedFile().getAbsolutePath()+"<br/></html>");
                    System.out.println("Opening file: " +
                            fileChooser.getSelectedFile().getName());
                    files.add(fileChooser.getSelectedFile());
                    break;
                case (JFileChooser.CANCEL_OPTION) :
                    break;
                case (JFileChooser.ERROR_OPTION) :
                    logArea.setText("<html>Error occurred while choosing a file<br/></html>");
                    break;
            }
        });
        //openMenuItem.setActionCommand("New");
        fileMenu.add(openMenuItem);
        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        myWindow.setJMenuBar(menuBar);
    }

    public static JLabel createLog(JFrame myWindow) {
        JLabel log = new JLabel("Log");
        log.setBounds(0, 200, 50, 50);
        myWindow.add(log);

        JLabel logArea = new JLabel();
        logArea.setBounds(0, 250, 600, 120);
        logArea.setOpaque(true);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.WHITE);
        myWindow.add(logArea);
        return logArea;
    }

    public static void createDownloadButton(JFrame myWindow, ArrayList<File> files) {
        JButton buttonDownloadFile = new JButton("Download");
        buttonDownloadFile.setBounds(0, 50, 100, 20);
        buttonDownloadFile.addActionListener( actionEvent -> {
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setBounds(50, 150, 100, 20);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            JLabel fileNameLabel = new JLabel(files.get(0).getName());
            fileNameLabel.setBounds(0, 150, 50, 20);
            myWindow.add(fileNameLabel);
            myWindow.add(progressBar);
            myWindow.repaint();
            Controller.startDownload(files.get(0), progressBar);
        });
        myWindow.add(buttonDownloadFile);
    }

    public static void main(String args[]) {
        JFrame myWindow = new JFrame();
        ArrayList<File> files = new ArrayList<>();
        myWindow.setSize(600, 400);
        myWindow.setTitle("Cattorrent");
        myWindow.setLayout(null);
        myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel logArea = createLog(myWindow);
        createMenu(myWindow, logArea, files);
        createDownloadButton(myWindow, files);
        myWindow.setVisible(true);
    }
}
