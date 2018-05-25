import javax.swing.*;
import java.awt.*;

public class DownLabel {
    private JLabel myLabel;

    DownLabel(){
        myLabel = new JLabel("terminal");
        myLabel.setBounds(0, 250, 600, 120);
        myLabel.setBackground(Color.CYAN);
        myLabel.setForeground(Color.BLUE);
    }

    JLabel getMyLabel(){return myLabel;}
}
