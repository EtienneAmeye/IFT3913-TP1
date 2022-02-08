package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is used to write a matrice of string
 * into a csv file.
 */
public class CSVWriter
{
    public static final String SEPARATOR = ",";     //Default separator of the csv

    private String filePath;
    private String[][] content;

    public CSVWriter(String filePath)
    {
        this.filePath = filePath;
        content = null;
    }

    public CSVWriter(String filePath, String[][] content)
    {
        this.filePath = filePath;
        this.content = content;
    }

    /**
     * Write the content into a csv file.
     *
     * @throws IOException If I/O errors occur
     */
    public void write() throws IOException
    {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath)))       //Open the file
        {
            //Write all lines
            for(int i = 0; i < content.length; i++)
            {
                String[] line = content[i];

                //Write all columns of a line
                for(int j = 0; j < line.length; j++)
                {
                    writer.write(line[j]);

                    //Last column doesn't end in a comma
                    if(j != line.length-1)
                    {
                        writer.write(SEPARATOR);
                    }
                }

                //Last line doesn't end witn a newline
                if(i != content.length-1)
                {
                    writer.write("\n");
                }
            }
        }
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String[][] getContent()
    {
        return content;
    }

    public void setContent(String[][] content)
    {
        this.content = content;
    }
}
