package net.atomshare.cattorrent.gui;

import javax.swing.JButton;
import java.lang.String;


public class StandardButton extends JButton{

    private JButton myButton;
    private int x, y;

    public StandardButton (String nameOfButton, int X, int Y) {
        this(nameOfButton, X, Y, 100, 20);
    }

    public StandardButton(String nameOfButton, int X, int Y, int length, int width) {//position x, y of left top corner. length, width - dimensions of button
        x = X;
        y = Y;
        myButton = new JButton(nameOfButton);
        myButton.setBounds(X, Y, length, width);
    }

    boolean equals(JButton toCompare){
        return myButton == toCompare;
    }

    @Override
    public int hashCode(){
        return myButton.hashCode();
    }

    JButton getButton(){return myButton;}
}