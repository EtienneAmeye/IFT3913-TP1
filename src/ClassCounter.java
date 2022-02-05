import io.WordReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ClassCounter
{
    public static final String NEW_LINE = "\n";
    public static final String WHITE_SPACES = ";";
    public static final String WORD_SEPARATORS = "[\\{\\}\\(\\)]";
    public static final HashMap<String, String> DELIMITERS = new HashMap<>();
    static {
        DELIMITERS.put("\"", "[^\\\\]\"");
        DELIMITERS.put("'", "[^\\\\]'");
        DELIMITERS.put("//", "\n|\\z");
        DELIMITERS.put("/\\*", "\\*/");
    }

    private String filePath;

    private int loc = 0;                //Non-empty line
    private int cloc = 0;               //Line with comment
    private int predicat = 0;           //if, for, while, do while, switch

    public ClassCounter(String filePath)
    {
        this.filePath = filePath;
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
            StringBuilder text = new StringBuilder();

            //Read the content of the file
            int c = bufferedReader.read();
            while(c != -1)
            {
                text.append((char) c);
                c = bufferedReader.read();
            }

            //Create a reader with the content
            return new WordReader(text.toString(), WHITE_SPACES, WORD_SEPARATORS, DELIMITERS);
        }
    }

    public void test() throws IOException
    {
        WordReader reader = readFile();

        String word = reader.readNexWord();
        while(word != null)
        {
            if(!word.equals("\n")) System.out.println(word);
            word = reader.readNexWord();
        }
    }

    /**
     * Read the file and count loc, cloc and predicat.
     *
     * @throws IOException If an error occurs while reading the file
     */
    public void read() throws IOException
    {
        WordReader reader = readFile();

        boolean emptyLine = true;
        boolean nonCommentedLine = true;

        String word = reader.readNexWord();
        while(word != null)
        {
            //Check if end of line
            if(word.equals(NEW_LINE))
            {
                emptyLine = true;
                nonCommentedLine = true;
            }

            //Update the loc count
            if(emptyLine && !word.equals(NEW_LINE))
            {
                emptyLine = false;
                loc++;
            }

            //Update the cloc count
            if(nonCommentedLine)            //only if no comment is already on the line
            {
                nonCommentedLine = !countCLOC(word);
            }

            //Update the predicat count
            countPredicat(word);

            word = reader.readNexWord();
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

    public int getPredicat()
    {
        return predicat;
    }

    /**
     * Update the count for CLOC and return if
     * the word is a comment. <br/>
     * If the word isn't a comment, CLOC won't be
     * updated. Otherwise, it depends if the comment
     * is multi-line or not.
     *
     * @param word The current word
     * @return True if the word is a comment, false otherwise
     */
    private boolean countCLOC(String word)
    {
        if(word.startsWith("//"))           //fixme maybe use regex
        {
            cloc++;
            return true;
        }
        else if(word.startsWith("/*"))      //fixme maybe use regex
        {
            cloc++;

            int i = 0;
            do
            {
                if(word.charAt(i) == '\n') {
                    cloc++;
                    loc++;          //also update loc because it won't be otherwise
                }
                i++;
            } while (i < word.length());

            return true;
        }

        return false;
    }

    /**
     * Update the count for predicat.
     *
     * @param word The current word
     */
    private void countPredicat(String word)
    {
        if(isPredicat(word)) predicat++;
    }

    private boolean isComment(String str)
    {
        return str.startsWith("//") || str.startsWith("/*");        //fixme maybe use regex
    }

    private boolean isPredicat(String str)
    {
        //todo move to final variable
        return str.equals("if")
                || str.equals("for")
                || str.equals("while")
                || str.equals("switch");
    }
}
