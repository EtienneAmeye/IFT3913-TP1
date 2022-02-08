import counters.ClassCounter;

import java.io.IOException;

/**
 * This class is used for testing only.
 */
public class Testing
{
    public static void main(String[] args) throws IOException
    {
        String filePath = "temp";

        ClassCounter counter = new ClassCounter(filePath);
        counter.read();

        System.out.println();
        System.out.println("LOC: " + counter.getLOC());
        System.out.println("CLOC: " + counter.getCLOC());
        System.out.println("WMC: " + counter.getWMC());
    }
}
