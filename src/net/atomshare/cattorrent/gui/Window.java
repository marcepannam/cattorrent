
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Window extends JFrame implements ActionListener{

    private JFrame myWindow;
    private JLabel downLabel;
    private int howManyTimesDispose = 0;
    private ArrayList<StandardButton> buttons ;
    private ArrayList<StandardTextField> textFields;
    private StringBuilder toWriteOut = new StringBuilder();

    public Window(int lenght, int width){
          myWindow = new JFrame();
          myWindow.setSize(lenght, width);
          myWindow.setTitle("Torrent client");
          myWindow.setLayout(null);
          myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          buttons = new ArrayList<>();
          textFields = new ArrayList<>();
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

    public void addTextField(StandardTextField toAdd){
        myWindow.add(toAdd.getMyTextField());
        textFields.add(toAdd);
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
                if(toWriteOut.length() == 0){
                    toWriteOut.append("<html>(\\ /)<br>(o.O)<br>(^ ^)<br><br></html>");
                }else{
                    toWriteOut.delete(toWriteOut.length() - 7, toWriteOut.length());
                    toWriteOut.append("(\\ /)<br>(o.O)<br>(^ ^)<br><br></html>");
                }
                downLabel.setText(toWriteOut.toString());
                break;
            }

            case 1:{
                System.out.println("powinno sie wylonczyc");
                for(int i = 0; i <= howManyTimesDispose; i++) {
                    System.out.println("wylaczam");
                    myWindow.dispose();
                }
                break;
            }

            case 2:{
                System.out.println("powinno wyskoczyc okno");
                StandardTextField beShown = new StandardTextField("special", 0, 100);
                StandardButton takeInfoButton = new StandardButton("take info", 0, 150);
                addButton(takeInfoButton);
                addTextField(beShown);
                makeVisible();
                howManyTimesDispose++;
                break;
            }

            case 3:{
                System.out.println("klikniete nowe okno");
                String info;
                info = textFields.get(1).getMyTextField().getText();
                downLabel.setText(info);
                break;
            }

            default:{
                System.out.println("wrong");
            }
        }
    }
}
