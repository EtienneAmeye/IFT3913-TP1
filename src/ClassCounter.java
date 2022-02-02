import io.Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class ClassCounter
{
    private String filePath;

    private int loc = 0;
    private int cloc = 0;

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
        boolean multiLineComment = false;


        //read and count
        String line = reader.readLine();

        while(line != null)
        {
            //Remove white space character
            line = line.trim();

            if(multiLineComment)
            {
                loc++;
                cloc++;

                if(line.matches(".*\\*/.*"))
                {
                    multiLineComment = false;
                }
            }
            else if(!line.isEmpty())
            {
                loc++;

                //Delete '"' inside of strings
                line = line.replace("\\\"", "");

                //Delete strings
                do{
                    line = line.replaceFirst("\"[^\"]*\"", "");
                } while(Pattern.matches(".*\"[^\"]*\".*", line));

                //Check for multiline comment
                if(Pattern.matches(".*/\\*.*", line))
                {
                    cloc++;

                    //Delete single line comment
                    do{
                        line = line.replaceFirst("/\\*.*\\*/", "");
                    } while(Pattern.matches(".*/\\*.*\\*/.*", line));

                    //Check if the comment actually span multiple lines
                    if(Pattern.matches(".*/\\*.*", line))
                    {
                        multiLineComment = true;
                    }
                }
                //Check for inline comment
                if(Pattern.matches(".*//.*", line))
                {
                    cloc++;
                }
            }

            line = reader.readLine();
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
