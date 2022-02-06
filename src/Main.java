import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        String filePath = "test";
        String projectPath = "C:\\Users\\etien\\Documents\\Programmation\\Projet\\SimWorld\\src";
        String outputPath = "";

        CounterController controller = new CounterController(projectPath, outputPath);
        controller.count();

//        ClassCounter counter = new ClassCounter(filePath);
//        counter.test();
//
//        System.out.println();
//        System.out.println("LOC: " + counter.getLOC());
//        System.out.println("CLOC: " + counter.getCLOC());
//        System.out.println("Predicat: " + counter.getPredicat());
    }
}
