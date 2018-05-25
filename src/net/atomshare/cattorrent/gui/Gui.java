public class  Gui {

    public static void main(String args[]) {
        Window myWindow = new Window(600, 400);
        StandardButton buttonDownloadFile = new StandardButton("download file", 0, 0);
        myWindow.addButton(buttonDownloadFile);
        StandardButton buttonExport =new StandardButton("export file", 100, 0);
        myWindow.addButton(buttonExport);
        StandardButton buttonChoose = new StandardButton("chose", 200, 0);
        myWindow.addButton(buttonChoose);
        DownLabel Lable = new DownLabel();
        myWindow.addLabel(Lable);
        StandartProgressBar Bar = new StandartProgressBar();
        myWindow.addStandartProgressBar(Bar);
        myWindow.makeVisible();
    }
}
