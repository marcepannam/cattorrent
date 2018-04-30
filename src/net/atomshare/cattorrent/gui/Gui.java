package net.atomshare.cattorrent.gui;

public class Gui {
    public static void main(String args[]) {
        Window myWindow = new Window(600, 400);
        StandardButton buttonShowBunny = new StandardButton("pokaz kroliczka", 0, 0);
        StandardButton buttonExit = new StandardButton("exit", 100, 0);
        DownLabel Lable = new DownLabel();
        myWindow.addButton(buttonShowBunny);
        myWindow.addButton(buttonExit);
        myWindow.addLabel(Lable);
        myWindow.makeVisible();
    }
}
