package net.atomshare.cattorrent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.atomshare.cattorrent.gui.Gui.logEvent;

public class Controller {
    public List<Downloader> downloaders;

    public Controller() {
        this.downloaders = new ArrayList<>();
        }

    /**
     this method takes file from the user
     and builds Downloader based on the file's data
     @return true on success, false on failure
     */
    public boolean startDownload(File file, JProgressBar progressBar, JLabel logArea, JFrame myWindow) {
        try  {
            Metainfo met = new Metainfo(file.getAbsolutePath());
            logEvent(logArea, file.getName() + " parsed successfuly");
            downloaders.add(new Downloader(met, new Downloader.DownloadProgressListener() {
                @Override
                public void onProgress(float p) {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(Math.round((1 - p) * 100));
                        progressBar.validate();
                    });
                }

                @Override
                public void onLog(String message) {
                    logEvent(logArea, message);
                }
            }));
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
