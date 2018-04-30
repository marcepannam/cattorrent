package net.atomshare.cattorrent.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Window extends JFrame implements ActionListener{

    private JFrame myWindow;
    private JLabel downLabel;
    private ArrayList<StandardButton> buttons ;

    public Window(int lenght, int width){
          myWindow = new JFrame();
          myWindow.setSize(lenght, width);
          myWindow.setTitle("Torrent client");
          myWindow.setLayout(null);
          myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          buttons = new ArrayList<>();
     }

    public void addButton(StandardButton toAdd){
        myWindow.add(toAdd.getButton());
        toAdd.getButton().addActionListener(this);
        buttons.add(toAdd);
    }

    public void addLabel(DownLabel toAdd){
         myWindow.add(toAdd.getMyLabel());
         downLabel = toAdd.getMyLabel();
    }

    public void makeVisible(){
        myWindow.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();
        int numberOfButton = 0;
        for (int i = 0; i < buttons.size();i++){
            if(buttons.get(i).equals(((JButton) source))){
                numberOfButton = i;
                break;
            }
        }
        System.out.println(numberOfButton);
        switch (numberOfButton){
            case 0:{
                StringBuilder bunny = new StringBuilder();
                bunny.append("BUNNY\n");
                downLabel.setText(bunny.toString());
                break;
            }

            case 1:{
                System.out.println("powinno sie wylonczyc");
                myWindow.dispose();
                break;
            }

            default:{
                System.out.println("wrong");
            }
        }
    }
}
