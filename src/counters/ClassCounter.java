package counters;

import io.WordReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class is used to mesure differents metrics
 * on a class. </br>
 * The metrics mesured are: loc, cloc and wmc.
 */
public class ClassCounter
{
    //region Constant parameters for the WordReader
    public static final String NEW_LINE = "\n";
    public static final String WHITE_SPACES = "[ \t\r]";                                                       // for comment
    public static final String SEPARATORS = "[;=\\[\\]\\{\\}\\(\\)]";
    public static final String STRING = "\"([^\"]|([^\\\\]\\\\(\\\\\\\\)*\"))*[^\\\\](\\\\\\\\)*\"";
    public static final String CHAR = "'([^']|([^\\\\]\\\\(\\\\\\\\)*'))*[^\\\\](\\\\\\\\)*'";
    public static final String COMMENT = "//.*";
    public static final String MULTI_LINE_COMMENT = "/\\*([^\\*]|(\\*[^/]))*\\*/";

    public static final String PREDICAT = "if|while|for|switch|else";
    //endregion

    private String filePath;            //The path to the file of the class

    private int loc = 0;                //Non-empty line
    private int cloc = 0;               //Line with comment
    private int wmc = 0;                //Weighted-method complexity

    public ClassCounter(String filePath)
    {
        this.filePath = filePath;
    }

    /**
     * THIS METHOD IS USED FOR TESTING
     * </p>
     * Print all the words of the class.
     *
     * @throws IOException If I/O errors occur
     */
    public void test() throws IOException
    {
        WordReader reader = readFile();

        String word = reader.readNexWord();
        while(word != null)
        {
            if(!word.equals("\n")) System.out.println("->| " + word + " |<-");
            word = reader.readNexWord();
        }
    }

    /**
     * Read the file and count loc, cloc and wmc.
     *
     * @throws IOException If an error occurs while reading the file
     */
    public void read() throws IOException
    {
        WordReader reader = readFile();

        try
        {
            countLOC(reader);
            reader.reset();                 //Return the head of the reader to the beginning of the file
            countCLOC(reader);
            reader.reset();                 //Return the head of the reader to the beginning of the file
            countWMC(reader);
        }
        catch (RuntimeException e)
        {
            //Reset the values
            loc = 0;
            cloc = 0;
            wmc = 0;

            throw new IOException("Can't read " + filePath, e);
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

    public int getWMC()
    {
        return wmc;
    }

    /**
     * Read the content of the file and convert it to a {@link WordReader}.
     *
     * @return A reader of the content of the file
     * @throws IOException If an error occur during the reading
     */
    private WordReader readFile() throws IOException
    {
        File file = new File(filePath);     //The file to read

        //Open the file
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file)))
        {
            StringBuilder textBuilder = new StringBuilder();

            //Read the content of the file
            int c = bufferedReader.read();
            while(c != -1)
            {
                textBuilder.append((char) c);
                c = bufferedReader.read();
            }

            //Create a reader with the content
            String text = textBuilder.toString();
            return new WordReader(text, WHITE_SPACES, SEPARATORS, STRING,
                    CHAR, COMMENT, MULTI_LINE_COMMENT);
        }
    }

    /**
     * Read the file and count the number of non-empty lines.
     *
     * @param reader The reader for the file
     */
    private void countLOC(WordReader reader)
    {
        boolean isLineEmpty = true;                 //Indicate if the line is empty

        String word = reader.readNexWord();         //Read first word
        while (word != null)
        {
            if(word.matches(MULTI_LINE_COMMENT))    //A multi line comment
            {
                //Update the loc count
                int size = getMultiLineCommentSize(word);
                loc += size - 1;

                //The last line will be counted normally
                isLineEmpty = false;
            }
            else if(!word.matches(NEW_LINE))        //Non-empty line
            {
                isLineEmpty = false;
            }
            else if(!isLineEmpty)                   //At end of line if line is not empty
            {
                loc++;                              //Update the loc count
                isLineEmpty = true;                 //Reset the flag
            }

            word = reader.readNexWord();            //Read next word
        }

        //Make sure we didn't skip last line
        if(!isLineEmpty)
        {
            loc++;
            isLineEmpty = true;
        }
    }

    /**
     * Read the file and count the number of commented lines.
     *
     * @param reader The reader of the file
     */
    private void countCLOC(WordReader reader)
    {
        boolean isCommentedLine = false;        //Indicate if the line has a comment

        String word = reader.readNexWord();     //Read first word
        while (word != null)
        {
            if(word.matches(NEW_LINE))           //At end of line
            {
                //Update the cloc count
                if(isCommentedLine)
                {
                    cloc++;
                }

                isCommentedLine = false;
            }
            else if(word.matches(COMMENT))      //fixme use regex
            {
                isCommentedLine = true;
            }
            else if(word.matches(MULTI_LINE_COMMENT))      //fixme use regex
            {
                // Update the cloc count with the size of the multi line comment
                int size = getMultiLineCommentSize(word);
                cloc += size - 1;

                //The last line will be counted normally
                isCommentedLine = true;
            }

            word = reader.readNexWord();        //Read next word
        }

        //Make sure we didn't skip last line
        if(isCommentedLine)
        {
            cloc++;
            isCommentedLine = false;
        }
    }

    /**
     * Read the file and calculate the weighted method complexity.
     *
     * @param reader The reader of the file
     */
    private void countWMC(WordReader reader)
    {
        //Build a regex to match method declaration
        String commentRegex =
                ClassCounter.COMMENT + "(\n|\r\n)"
                + "|" + ClassCounter.MULTI_LINE_COMMENT;        //Regex that matches comment
        String anythingRegex = "("
                + "[^;=\"/\\(\\)\\{\\}]"
                + "|" + commentRegex
                + ")*";                                         //Regex that matches word&comment
        String functionRegex = anythingRegex
                + "\\(" + anythingRegex + "\\)"
                + anythingRegex + "\\{";                        //Regex that matches method declaration

        //Read through the next to find method declaraction
        do
        {
            //Check if the head of the text matches a method declaration
            if(reader.match(functionRegex))
            {
                //Move to the start of the method
                String word;
                do
                {
                    word = reader.readNexWord();
                } while (!word.matches("\\{") && !isPredicat(word));

                //We could be looking at a predicat (if, while, ...) -> exist if yes
                if(isPredicat(word)) continue;

                //Calculate the complexity
                reader.readNexWord();                                   //Move inside the method
                int complexity = calculateFunctionComplexity(reader);

                //Update the WMC count
                wmc += complexity;
            }
        } while (reader.readNexWord() != null);                         //Move to the next word;

    }

    /**
     * Calculate the complexity of a function (at the head
     * of the reader). </br>
     * The start of the function is considered to be the
     * character after the first '{'.
     * </p>
     * Move the head of the reader to the end of the function. </br>
     * The end of the function is the character after the
     * ending '}'.
     *
     * @param reader A WordReader with the head at the
     *               start of the function
     * @return The complexity of the function
     */
    private int calculateFunctionComplexity(WordReader reader)
    {
        int count = 1;                  //Method complexity is 1 + # of predicats
        int imbrication = 1;

        do
        {
            String word = reader.readNexWord();

            if(word.matches("\\{"))
            {
                imbrication++;
            }
            else if(word.matches("\\}"))
            {
                imbrication--;
            }
            else if(isPredicat(word))
            {
                count++;
            }
        } while (imbrication > 0);

        return count;
    }

    /**
     * Return the size of a multi-line comment. </br>
     * The size is calculated as the number of
     * non-empty line inside the comment.
     *
     * @param comment The comment
     * @return The size of the comment
     */
    private int getMultiLineCommentSize(String comment)
    {
        int size = 0;                           //Nb of non-empty line in the comment
        boolean isLineEmpty = false;            //Indicate if the line is empty

        //Update the cloc count for every line in the multiline comment
        int i = 0;
        do
        {
            char c = comment.charAt(i);
            if(String.valueOf(c).matches(NEW_LINE))             //At end of line
            {
                //Update size
                if(!isLineEmpty)
                {
                    size++;
                }

                isLineEmpty = true;                             //Reset flag
            }
            else if(!isWhiteSpace(c))                           //Line is not empty
            {
                isLineEmpty = false;
            }

            i++;                                                //Move to next char
        } while (i < comment.length());

        //Count the last line
        if(!isLineEmpty)
        {
            size++;
        }

        return size;
    }

    private boolean isWhiteSpace(char c)
    {
        return isWhiteSpace(String.valueOf(c));
    }

    private boolean isWhiteSpace(String str)
    {
        return str.matches(WHITE_SPACES);
    }

    private boolean isComment(String str)
    {
        return str.matches(COMMENT) || str.matches(MULTI_LINE_COMMENT);
    }

    private boolean isPredicat(String str)
    {
        return str.matches(PREDICAT);
    }
}
