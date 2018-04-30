package net.atomshare.cattorrent.gui;

import javax.swing.*;

public class DownLabel {
    private JLabel myLabel;

    DownLabel(){
        myLabel = new JLabel("terminal");
        myLabel.setBounds(0, 200, 600, 100);
    }

    JLabel getMyLabel(){return myLabel;}
}
