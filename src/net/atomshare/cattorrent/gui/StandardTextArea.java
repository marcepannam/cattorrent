import javax.swing.*;

public class StandardTextArea {

    private JTextArea myTextField;

    StandardTextArea(String startInformation, int X, int Y){
        myTextField = new JTextArea(startInformation);
        myTextField.setBounds(X, Y, 150, 50);
    }

    public JTextArea getMyTextArea(){
        return myTextField;
    }
}
