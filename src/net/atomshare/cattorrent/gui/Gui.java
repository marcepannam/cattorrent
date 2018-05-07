public class  Gui {

    public static void main(String args[]) {
        Window myWindow = new Window(600, 400);
        StandardButton buttonShowBunny = new StandardButton("pokaz kroliczka", 0, 0);
        StandardButton buttonExit = new StandardButton("exit", 100, 0);
        StandardButton buttonShowField = new StandardButton("showField", 200, 0);
        DownLabel Lable = new DownLabel();
        myWindow.addButton(buttonShowBunny);
        myWindow.addButton(buttonExit);
        myWindow.addLabel(Lable);
        StandardTextField textField = new StandardTextField("", 0, 200);
        myWindow.addTextField(textField);
        myWindow.addButton(buttonShowField);
        myWindow.makeVisible();
    }
}
