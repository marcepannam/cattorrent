package net.atomshare.cattorrent.gui;

import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;

public class Gui {

    public static void main(String args[]) {
        Controller c = new Controller();
        SwingUtilities.invokeLater(() -> createAndShowGUI(c));
    }

    public static void createAndShowGUI(Controller c) {
        JFrame myWindow = new JFrame();
        ArrayList<File> files = new ArrayList<>();
        myWindow.setSize(600, 400);
        myWindow.setTitle("Cattorrent");
        myWindow.setLayout(null);
        myWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JLabel logArea = createLog(myWindow);
        JLabel fileArea = createFileLabel(myWindow);
        createIcon(myWindow);
        createMenu(myWindow, logArea, fileArea, files);
        JTable downloadsArea = createDownloadsPanel(myWindow);
        createTorrentButton(myWindow);
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
        panel.setBounds(0, 0, 320, 40);
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
            label.setBounds(320, 0, 280, 70);
            myWindow.add(label);
        } else {
            System.err.println("Unable to locate image in the filesystem");
        }
    }

    private static JTable createDownloadsPanel(JFrame myWindow) {
        JTable downloadsTable = new JTable(new DefaultTableModel(new Object[][][]{}, new String[]{"Name", "Progress"}));
        downloadsTable.setAutoCreateRowSorter(true);
        downloadsTable.setBackground(Color.LIGHT_GRAY);
        JTableHeader header = downloadsTable.getTableHeader();
        header.setBackground(new Color(160, 120, 240));
        header.setFont(new Font("Verdana", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(downloadsTable);
        scrollPane.setBounds(0, 70, 600, 280);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setBackground(Color.LIGHT_GRAY);
        myWindow.add(scrollPane);
        downloadsTable.getModel().addTableModelListener(tableModelEvent -> { });
        downloadsTable.getColumn("Progress").setCellRenderer(new ProgressRenderer());
        return downloadsTable;
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

    private static void createTorrentButton(JFrame myWindow) {
        JButton buttonTorrent = new JButton("Create torrent");
        buttonTorrent.setBounds(160, 40, 160, 30);
        myWindow.add(buttonTorrent);
        buttonTorrent.addActionListener(actionEvent -> {
            //to do
        });
    }

    private static void createDownloadButton(
            JFrame myWindow, JLabel logArea, JTable downloadsArea, ArrayList<File> files, Controller c) {
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
            JFrame myWindow, JLabel logArea, JTable downloadsArea, ArrayList<File> files, Controller c) {
        if (!c.startDownload(files.get(files.size()-1), downloadsArea, logArea, myWindow)) return;
        String name = files.get(files.size()-1).getName();
        JPanel fileDownload = new JPanel(new BorderLayout());
        DefaultTableModel model = (DefaultTableModel) downloadsArea.getModel();
        model.addRow(new Object[]{name, 0});
        downloadsArea.add(fileDownload);
        downloadsArea.validate();
    }
    static class ProgressRenderer extends JProgressBar implements TableCellRenderer {
        ProgressRenderer() {
            super(0, 100);
            this.setValue(0);
            this.setStringPainted(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.setValue((Integer)value);
            return this;
        }
    }

}
