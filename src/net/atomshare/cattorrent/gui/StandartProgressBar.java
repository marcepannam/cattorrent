import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;

public class StandartProgressBar {
    private JProgressBar myProgressBar;
    private JButton myButton;
    private Integer currentlyProgres;
    private int sizeOfFile;
    private boolean ready = false;

    StandartProgressBar(){
        myProgressBar = new JProgressBar(0, 100);
        myButton = new JButton("start");
        myProgressBar.setBounds(500, 300, 100, 20);
        myProgressBar.setValue(0);
        myProgressBar.setStringPainted(true);
        System.out.println("YES");
        myButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!ready){return;}
                myButton.setEnabled(false);
                System.out.println("wywolalo");
                myProgressBar.setValue(0);
                while (currentlyProgres != sizeOfFile){
                    int percent = currentlyProgres * 100 / sizeOfFile;
                    myProgressBar.setValue(percent);
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                ready = false;
            }
        });
    }

    void takeData(int sizeOfFile_, Integer currentlyProgres_){
        currentlyProgres = currentlyProgres_;
        sizeOfFile = sizeOfFile_;
        ready = true;
        myProgressBar.setValue(5);
        System.out.println("value fixed");
    }

    JProgressBar giveProgressBar(){
        return myProgressBar;
    }

}
