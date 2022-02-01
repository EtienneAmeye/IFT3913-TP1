import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        //main function

        String filePath = "test";

        ClassCounter counter = new ClassCounter(filePath);
        counter.read();

        System.out.println(counter.getLOC());
    }
}
