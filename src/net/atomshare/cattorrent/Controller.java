package net.atomshare.cattorrent;

import net.atomshare.cattorrent.gui.Gui;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    public List<Downloader> downloaders;

    public Controller() {
        this.downloaders = new ArrayList<>();
        }

    public void startDownload(File file, JProgressBar progressBar, JLabel logArea) {
        try  {
            Metainfo met = new Metainfo(file.getAbsolutePath());
            progressBar.setValue(0);
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
                    SwingUtilities.invokeLater(() -> {
                        Gui.logEvent(logArea, message);
                    });
                }
            }));
        } catch (IOException e) {
            System.out.println("Error occurred while opening the file:");
            System.out.println(e.getMessage());
            int idx = file.getName().lastIndexOf('.');
            if (idx > 0) {
                String extension = file.getName().substring(idx);
                if (!extension.equals(".torrent")) {
                    System.out.println("Are you sure it is a valid torrent file? Its extension is: \""
                            + extension + "\" and should be: \".torrent\"");
                }
            } else {
                System.out.println("Are you sure it is a valid torrent file?" +
                        " The file should have .torrent extension");
            }
        }
    }
}
