
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Window extends JFrame implements ActionListener{

    private JFrame myWindow;
    private JLabel downLabel;
    private StandartProgressBar Bar;
    private ArrayList<StandardButton> buttons ;
    private ArrayList<StandardTextArea> textFields;
    private LabelTextManager toWriteOut = new LabelTextManager();
    private FileToWork file = new FileToWork("nothing has been chosen");

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

    public void addTextArea(StandardTextArea toAdd){
        myWindow.add(toAdd.getMyTextArea());
        textFields.add(toAdd);
    }

    public void addStandardFileChooser(JFileChooser chooser){
        myWindow.add(chooser);
    }

    public void addStandartProgressBar(StandartProgressBar S){
        myWindow.add(S.giveProgressBar());
        Bar = S;
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
            case 0:{//download file
                //
                try {
                    toWriteOut.append("I'm downloading file:   ");
                    toWriteOut.append(file.takeFile());
                    //downloading function
                    Bar.takeData(10000, 10);
                    Bar.giveProgressBar().setStringPainted(true);
                }catch (Exception e){
                    toWriteOut.append("You must choose some file");
                }
                toWriteOut.addENDL();
                downLabel.setText(toWriteOut.toString());
                break;
            }

            case 1:{
                try {
                    toWriteOut.append(file.takeFile());
                    //exporting function
                }catch (Exception e){
                    toWriteOut.append("You must choose some file");
                }
                toWriteOut.addENDL();
                downLabel.setText(toWriteOut.toString());
                break;
            }

            case 2:{
                System.out.println("check box");
                JButton open = new JButton();
                StandardFileChooser chooser = new StandardFileChooser();
                if(chooser.getMyFileChoser().showOpenDialog(open) == JFileChooser.APPROVE_OPTION) {
                    //
                }
                file.changeFile(chooser.getMyFileChoser().getSelectedFile().getAbsolutePath());
                toWriteOut.append(chooser.getMyFileChoser().getSelectedFile().getAbsolutePath());
                toWriteOut.addENDL();
                downLabel.setText(toWriteOut.toString());
                break;

            }

            default:{
                System.out.println("wrong");
            }
        }
    }
}
