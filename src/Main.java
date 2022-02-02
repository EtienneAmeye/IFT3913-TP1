import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        //main function

        String filePath = "test";

        ClassCounter counter = new ClassCounter(filePath);
        counter.read();

        System.out.println("\nLOC: " + counter.getLOC());
        System.out.println("\nCLOC: " + counter.getCLOC());

        //FOR TESTING
//        String test = "This is /*A comment*/ //Comment";
//
//        test = test.trim();
//        String newTest = test.replace("\\\"", "");
//        System.out.println(newTest);
//
//        do{
//            newTest = newTest.replaceFirst("\"[^\"]*\"", "");
//
//        } while(Pattern.matches(".*\"[^\"]*\".*", newTest));
//        System.out.println(newTest);
//
//        do{
//            newTest = newTest.replaceFirst("/\\*.*\\*/", "");
//        } while(Pattern.matches(".*/\\*.*\\*/.*", newTest));
//        System.out.println(newTest);
//
//        boolean resultA = Pattern.matches(".*(//|/\\*.*\\*/).*", newTest);
//        System.out.println("Result A: " + resultA);
    }
}
