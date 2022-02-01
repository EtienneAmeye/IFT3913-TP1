package io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Reader
{
    //Constantes
    public static final String SPACE_CHAR = "[ \t\r]+";
    public static final String NEW_LINE_CHAR = "[\n]+";
    public static final String END_OF_FILE_CHAR = "[\0]+";

    private String text;            //The text to read, as a String

    private int head;               //The head of the reader, indicate the current character to read
    private int line;               //The current line of the head
    private int column;             //The current column of the head

    public Reader(String text)
    {
        this.text = text;
        line = 0;
        column = 0;
        head = 0;
    }

    /**
     * Look at the next character.
     *
     * @return A character, as a String
     */
    private String peekChar()
    {
        if(head >= text.length())
        {
            return "\0";
        }

        return String.valueOf(text.charAt(head));
    }

    /**
     * Read the next character and move the head
     * of the reader to the following character.
     *
     * @return A character, as a String
     */
    private String readChar()
    {
        if(head >= text.length())
        {
            return "\0";
        }

        char c = text.charAt(head);
        if(c != '\0')
        {
            head++;
            if(c == '\n')
            {
                line++;
                column = 0;
            }
            else
            {
                column++;
            }
        }

        return String.valueOf(c);
    }

    /**
     * Read the next word in the text. A word is a continuous string of characters
     * excluding {@link #SPACE_CHAR}. <br/>
     * {@link #NEW_LINE_CHAR} is its own word and {@link #END_OF_FILE_CHAR}
     * is returned as a null word.
     * <p/>
     * This method moves the head of the read to the end of the word.
     *
     * @return A word, as a String.
     */
    public String readWord()
    {
        //Read next first non-space character
        String c;
        do
        {
            c = readChar();
        } while (Pattern.matches(SPACE_CHAR, c));

        //If the character is end-of-file, just return null
        if(Pattern.matches(END_OF_FILE_CHAR, c)) return null;

        //New line characters are returned directly
        if(Pattern.matches(NEW_LINE_CHAR, c)) return c;

        //Read the next word
        StringBuilder word = new StringBuilder(c);

        c = peekChar();         //Look at the next character without moving the head
        while (!Pattern.matches(SPACE_CHAR, c)
                && !Pattern.matches(NEW_LINE_CHAR, c)
                && !Pattern.matches(END_OF_FILE_CHAR, c))
        {

            word.append(c);
            readChar();         //Move to the next character

            c = peekChar();     //Look at the next character without moving the head
        }

        //Return the word
        return word.toString();
    }

    /**
     * Read all the following word ending at the end of the line (or end-of-file).
     * This method moves the head of the reader to the start of the next line.
     *
     * @return A list of words
     */
    public List<String> readLine()
    {
        ArrayList<String> lines = new ArrayList<>();

        String word = readWord();

        //Special case, return null when at end-of-file
        if(word == null) return null;

        //Read all the words of the line
        while(word != null &&
                !Pattern.matches(NEW_LINE_CHAR, word))      //end-of-line is null word
        {
            lines.add(word);                //Add the word to the list
            word = readWord();              //Move to the next word
        }

        return lines;
    }
}
