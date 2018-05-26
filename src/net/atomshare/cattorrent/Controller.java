package net.atomshare.cattorrent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Controller {
    public static void startDownload(File file, JProgressBar progressBar) {
        try  {
            Metainfo met = new Metainfo(file.getAbsolutePath());
            progressBar.setValue(0);
            Downloader d = new Downloader(met, p -> SwingUtilities.invokeLater(() -> progressBar.setValue(Math.round(p))));
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
