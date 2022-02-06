package io;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordReader
{
    public static final String DEFAULT_NEW_LINE = "\n";
    public static final String DEFAULT_END_OF_FILE = "\\z";
    public static final String DEFAULT_WHITE_SPACE = "[ \t\r]";

    private String whiteSpaces;
    private String wordSeparators;
    private HashMap<String, String> specialDelimiters;

    private String text;            //The text to read, as a String
    private int head;               //The head of the reader, indicate the current character to read

    public WordReader(String text, HashMap<String, String> specialDelimiters)
    {
        this.specialDelimiters = specialDelimiters;
        this.whiteSpaces = buildWhiteSpaces(null);
        this.wordSeparators = buildWordSeparators(null, null, specialDelimiters);

        this.text = text;
        head = 0;
    }

    public WordReader(String text, String customWhiteSpaces, String customWordSeparator,
                      HashMap<String, String> specialDelimiters)
    {
        this.specialDelimiters = specialDelimiters;
        this.whiteSpaces = buildWhiteSpaces(customWhiteSpaces);
        this.wordSeparators = buildWordSeparators(customWhiteSpaces, customWordSeparator, specialDelimiters);

        this.text = text;
        head = 0;
    }

    /**
     * Look at the next character. <br/>
     * Returns \0 if at the end of the text.
     *
     * @return A character
     */
    public char peekChar()
    {
        if(head >= text.length())
        {
            return '\0';
        }

        return text.charAt(head);
    }

    /**
     * Read the next character and move the head
     * of the reader to the following character. </br>
     * Returns \0 if at the end of the text.
     *
     * @return A character
     */
    public char readChar()
    {
        if(head >= text.length())
        {
            return '\0';
        }

        char c = text.charAt(head);
        head++;
        return c;
    }

    /**
     * Read the next word in the text. A word is a continuous string of characters
     * enclosed by word-separator. Word-separator also count as word, unless they
     * are also white-space. <br/>
     * The end-of-file always mark the end of a word.
     * <p/>
     * Certain special words are enclosed by word-delimiters instead.
     * <p/>
     * This method moves the head of the read to the end of the word.
     * <p/>
     * Return null if at the end of the text.
     *
     * @return A word, as a String.
     */
    public String readNexWord()
    {
        String word = null;

        //Return null at the end of the text
        if(head >= text.length()) return null;

        //Skip leading whitespace
        while(match(whiteSpaces))
        {
            head++;                         //move head to the next character
        }

        //Check for delimiters
        for(Map.Entry<String, String> delimiter : specialDelimiters.entrySet())
        {
            //Found a delimiter
            if(match(delimiter.getKey()))
            {
                //Extract the entire section
                int sectionStart = head;
                int sectionEnd;

                moveToFirst(delimiter.getValue());                          //Move to the end delimiter

                //Special case for newline -> do not add the newline to the word
                if(match(DEFAULT_NEW_LINE))
                {
                    sectionEnd = head;                                      //Before the newline
                }
                else
                {
                    sectionEnd = moveAfterFirst(delimiter.getValue());      //After the delimiter
                }

                //No section end -> error
                if(sectionEnd == -1)
                {
                    throw new RuntimeException("Missing ending delimiters " + delimiter.getValue()
                            + " at " + peekChar() + "(" + head + ")");
                }

                word = text.substring(sectionStart, sectionEnd);
                break;      //exit the loop
            }
        }

        if(word == null)        //indicate that we have been through the loop
        {
            //Check if the word start with a separator
            if (match(wordSeparators))
            {
                //Treat the separator as a word
                int wordStart = head;
                int wordEnd = moveAfterFirst(wordSeparators);       //should always be > 0

                word = text.substring(wordStart, wordEnd);
            }
            //Read the word
            else
            {
                int wordStart = head;
                int wordEnd = moveToFirst(wordSeparators);

                //No word separators -> probably end-of-file
                if(wordEnd == -1)
                {
                    wordEnd = text.length();
                }

                word = text.substring(wordStart, wordEnd);
            }
        }

        return word;
    }

    /**
     * Move the head of the reader to the first occurance,
     * in the text, of the pattern define by regex.
     * </p>
     * If not match is found, the head doesn't move
     * and this returns -1.
     *
     * @param regex The pattern to search
     * @return The new position of the head, or -1
     */
    public int moveToFirst(String regex)
    {
        Matcher matcher = Pattern.compile(regex).matcher(text);     //Create a matcher

        if(matcher.find(head))                                      //Search for the first occurance of regex
        {
            int index = matcher.start();                            //Get its index
            head = index;                                           //Update the head of the reader
            return index;                                           //Return the index
        }

        return -1;                                                  //No match -> return -1
    }

    /**
     * Move the head of the reader to the character
     * after the first occurance, in the text, of
     * the pattern define by regex.
     * </p>
     * If not match is found, the head doesn't move
     * and this returns -1.
     *
     * @param regex The pattern to search
     * @return The new position of the head, or -1
     */
    public int moveAfterFirst(String regex)
    {
        Matcher matcher = Pattern.compile(regex).matcher(text);     //Create a matcher

        if(matcher.find(head))                                      //Search for the first occurance of regex
        {
            int index = matcher.end();                              //Get the index of the next character
            head = index;                                           //Update the head of the reader
            return index;                                           //Return the index
        }

        return -1;                                                  //No match -> return -1
    }

    public boolean match(String regex)
    {
        Matcher matcher = Pattern.compile(regex).matcher(text);     //Create a matcher

        if(matcher.find(head))                                      //Search for the first occurance of regex
        {
            int index = matcher.start();                            //Get its index
            return head == index;                                   //Check if the match is at the start
        }

        return false;                                               //No match -> return -1
    }

    public String getText()
    {
        return text;
    }

    public int getHead()
    {
        return head;
    }

    public void setHead(int pos)
    {
        head = pos;
    }

    public void reset()
    {
        head = 0;
    }

    private String buildWhiteSpaces(String customWhiteSpaces)
    {
        if(customWhiteSpaces == null || customWhiteSpaces.isEmpty()) return DEFAULT_WHITE_SPACE;
        return DEFAULT_WHITE_SPACE + "|" + customWhiteSpaces;
    }

    private String buildWordSeparators(String customWhiteSpaces, String customWordSeparators,
                                       HashMap<String, String> specialDelimiters)
    {
        StringBuilder wordSeparatorBuilder = new StringBuilder();

        //Add white spaces to word separators
        wordSeparatorBuilder.append(DEFAULT_WHITE_SPACE);
        if(customWhiteSpaces != null
                && !customWhiteSpaces.isEmpty()) {
            wordSeparatorBuilder.append("|").append(customWhiteSpaces);
        }

        //Add default newline and end-of-line to word separators
        wordSeparatorBuilder.append("|").append(DEFAULT_NEW_LINE);
        wordSeparatorBuilder.append("|").append(DEFAULT_END_OF_FILE);

        //Add the custom word separators
        if(customWordSeparators != null
                && !customWordSeparators.isEmpty()) {
            wordSeparatorBuilder.append("|").append(customWordSeparators);
        }

        //Add the delimiters as word separators
        if(specialDelimiters != null)
        {
            for (String key : specialDelimiters.keySet())
            {
                wordSeparatorBuilder.append("|").append(key);
            }
        }

        return wordSeparatorBuilder.toString();
    }
}
