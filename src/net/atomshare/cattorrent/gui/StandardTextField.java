import javax.swing.*;

public class StandardTextField {

    private JTextField myTextField;

    StandardTextField(String startInformation, int X, int Y){
        myTextField = new JFormattedTextField(startInformation);
        myTextField.setBounds(X, Y, 150, 50);
    }

    public JTextField getMyTextField(){
        return myTextField;
    }
}
