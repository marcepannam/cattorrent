package net.atomshare.cattorrent.gui;

public class LabelTextManager {
    private StringBuilder tab[];
    private boolean newIndex;
    private int size;

    private class Iterator{
        int i;
        Iterator(int beginValue){
            i = beginValue;
        }
        void increment(){
            i++;
            i %= size;
        }
        int getI(){ return i; }
    }

    private Iterator myIterator;

    LabelTextManager(){
        size = 7;
        tab = new StringBuilder[size];
        for (int i = 0; i < size; i++){ tab[i] = new StringBuilder(); }
        newIndex = true;
        myIterator = new Iterator(0);
    }

    public void append(String toAppend){//
        int index = myIterator.getI();
        if(newIndex) {
            tab[index].delete(0, tab[index].length());
            newIndex = false;
        }
        tab[index].append(toAppend);
    }

    public void addENDL(){//
        append("<br>");
        newIndex = true;
        myIterator.increment();
    }

    @Override
    public String toString(){
        if(!newIndex){return null;}
        StringBuilder Text = new StringBuilder("<html>");
        Iterator temp = new Iterator(myIterator.getI());
        for(int i = 0; i < size; i++){
            Text.append(tab[temp.getI()]);
            temp.increment();
        }
        Text.append("</html>");
        return Text.toString();
    }
}
