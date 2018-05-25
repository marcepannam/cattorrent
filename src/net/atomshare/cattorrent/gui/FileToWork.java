public class FileToWork {
    private String file;
    private  boolean accessable;

    FileToWork(String beginningInfo){
        file = beginningInfo;
        accessable = false;
    }

    void changeFile(String newFile){
        file = newFile;
        accessable = true;
    }

    String takeFile() throws Exception {
        if(!accessable) throw new Exception();
        accessable = false;
        return file;
    }
}
