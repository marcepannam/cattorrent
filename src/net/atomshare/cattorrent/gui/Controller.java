package net.atomshare.cattorrent.gui;

import net.atomshare.cattorrent.Downloader;
import net.atomshare.cattorrent.Metainfo;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.atomshare.cattorrent.gui.Gui.logEvent;

public class Controller {
    private Integer id;
    public List<Downloader> downloaders;

    public Controller() {
        this.downloaders = new ArrayList<>();
        id = 0;
    }

    /**
     this method takes file from the user
     and builds Downloader based on the file's data
     @return true on success, false on failure
     */
    public boolean startDownload(File file, JTable downloadsArea, JLabel logArea, JFrame myWindow) {
        try  {
            String path = file.getAbsolutePath();
            if (!path.endsWith(".torrent")) {
                logEvent(logArea, "Please select .torrent file");
                return false;
            }
            String target = path.substring(0, path.length() - 8);
            Metainfo met = new Metainfo(path);
            logEvent(logArea, file.getName() + " parsed successfuly");
            Downloader d;
            synchronized (this) {
                d = new Downloader(met, target, new Downloader.DownloadProgressListener() {
                    final int num = id++;
                    @Override
                    public void onProgress(float p) {
                        SwingUtilities.invokeLater(() -> {
                            downloadsArea.setValueAt(Math.round((1 - p) * 100), downloadsArea.convertRowIndexToView(num), 1);
                        });
                    }
                    @Override
                    public void onLog(String message) {
                        logEvent(logArea, message);
                    }
                });
            }
            new Thread(() -> {
                d.run();
                logEvent(logArea, "File saved: " + target);
            }).start();
            downloaders.add(d);
            return true;
        } catch (IOException e) {
            logEvent(logArea, "Error while parsing file " + file.getName());
            logEvent(logArea, e.getMessage());
            int idx = file.getName().lastIndexOf('.');
            if (idx > 0) {
                String extension = file.getName().substring(idx);
                if (!extension.equals(".torrent")) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(myWindow,
                            "It is impossible to open provided file.\n" +
                                    "Please check if it is a valid torrent file. Its extension is: \""
                            + extension + "\" and should be: \".torrent\"",
                            "Warning", JOptionPane.WARNING_MESSAGE));
                }
            } else {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(myWindow,
                        "It is impossible to open provided file.\n" +
                                "Please check if it is a valid torrent file.",
                        "Warning", JOptionPane.WARNING_MESSAGE));
            }
        }
        return false;
    }
}
