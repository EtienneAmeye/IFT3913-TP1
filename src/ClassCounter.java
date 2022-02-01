import io.Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ClassCounter
{
    private String filePath;

    private int loc = 0;
    private int cloc = 1;

    public ClassCounter(String filePath)
    {
        this.filePath = filePath;
    }

    /**
     * Read the content of the file and convert it to a {@link Reader}.
     *
     * @return A reader of the content of the file
     * @throws IOException If an error occur during the reading
     */
    private Reader readFile() throws IOException
    {
        File file = new File(filePath);     //The file to read

        //Open the file
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file)))
        {
            StringBuilder text = new StringBuilder();

            //Read the content of the file
            int c = bufferedReader.read();
            while(c != -1)
            {
                text.append((char) c);
                c = bufferedReader.read();
            }

            //Create a reader with the content
            return new Reader(text.toString());
        }
    }

    public void read() throws IOException
    {
        Reader reader = readFile();

        //read and count
        List<String> line = reader.readLine();

        //Count the lines
        while(line != null)
        {
            //Only count non-empty line for now
            if(!line.isEmpty())
            {
                loc++;
            }

            line = reader.readLine();       //Move to the next line
        }
    }

    public int getLOC()
    {
        return loc;
    }

    public int getCLOC()
    {
        return cloc;
    }

    public int getDC()
    {
        return getLOC() / getCLOC();
    }
}
