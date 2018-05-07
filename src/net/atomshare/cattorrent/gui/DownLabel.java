import javax.swing.*;
import java.awt.*;

public class DownLabel {
    private JLabel myLabel;

    DownLabel(){
        myLabel = new JLabel("terminal");
        myLabel.setBounds(0, 200, 600, 100);
        myLabel.setForeground(Color.BLUE);
    }

    JLabel getMyLabel(){return myLabel;}
}
