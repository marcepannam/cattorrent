import javax.swing.JFileChooser;

public class StandardFileChooser {
    private JFileChooser myFileChoser;

    public StandardFileChooser(){
        myFileChoser = new JFileChooser();
        myFileChoser.setCurrentDirectory(new java.io.File("~"));
        myFileChoser.setDialogTitle("please choose file to ...");
    }

    JFileChooser getMyFileChoser(){
        return myFileChoser;
    }
}
