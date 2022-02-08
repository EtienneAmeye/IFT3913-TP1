package io;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to read through a text by
 * separating it into words.
 * </br>
 * The separation is done using regular expression (regex)
 * specified by the user.
 */
public class WordReader
{
    public static final String DEFAULT_NEW_LINE = "\n";
    public static final String DEFAULT_END_OF_FILE = "\\z";

    private String whiteSpaces;
    private String wordSeparators;
    private HashMap<String, String> delimiters;

    private String text;            //The text to read, as a String
    private int head;               //The head of the reader, indicate the current character to read

    public WordReader(String text, String whiteSpaces, String... specialWords)
    {
        this.whiteSpaces = whiteSpaces;
        this.wordSeparators = buildWordSeparators(whiteSpaces, null, specialWords);

        this.text = text;
        head = 0;
    }

    public WordReader(String text, String whiteSpaces, HashMap<String, String> delimiters,
                      String... specialWords)
    {
        this.whiteSpaces = whiteSpaces;
        this.delimiters = delimiters;
        this.wordSeparators = buildWordSeparators(whiteSpaces, delimiters, specialWords);

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
    private char readChar()
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

        //Check delimiters
        for(Map.Entry<String, String> delimiter : delimiters.entrySet())
        {
            if(match(delimiter.getKey()))
            {
                //Extract the word
                int wordStart = head;
                int wordEnd = moveAfterFirst(delimiter.getValue());
                word = text.substring(wordStart, wordEnd);

                break;              //Stop the loop
            }
        }

        if(word == null)            //If word isn't null, then we matched a delimiter
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

                //Move to the next word separator
                int wordEnd = moveToFirst(wordSeparators);

                //No word separators -> probably end-of-file
                if (wordEnd == -1)
                {
                    wordEnd = text.length();
                }

                word = text.substring(wordStart, wordEnd);
            }
        }

        return word;
    }

    /**
     * Check if the text at the head of the reader
     * matches the given regex. </br>
     * There is a match if the text starts with an
     * substring that matches the regex. </br>
     * The entire text does not have to match the regex.
     *
     * @param regex The regex to match
     * @return True if there is a match, false otherwise
     */
    public boolean match(String regex)
    {
        String str = text.substring(head);
        return match(str, regex);
    }

    /**
     * Return the head to the start of the text.
     */
    public void reset()
    {
        head = 0;
    }

    public String getText()
    {
        return text;
    }

    public int getHead()
    {
        return head;
    }

    /**
     * Build a regex for the all the word separators. </br>
     * The regex is build by combining default special
     * character (like \n), whitespaces, delimiters,
     * and the special words given by the user.
     *
     * @param whiteSpaces A regex that matches whitespaces
     * @param delimiters A map of delimiters
     * @param specialWords Array of regex that each match a special word
     * @return A regex matching all type of word separator
     */
    private String buildWordSeparators(String whiteSpaces, HashMap<String, String> delimiters,
                                       String... specialWords)
    {
        StringBuilder wordSeparatorBuilder = new StringBuilder();

        //Add default newline and end-of-line to word separators
        wordSeparatorBuilder.append(DEFAULT_NEW_LINE);
        wordSeparatorBuilder.append("|").append(DEFAULT_END_OF_FILE);

        //Add white spaces to word separators
        if(whiteSpaces != null
                && !whiteSpaces.isEmpty()) {
            wordSeparatorBuilder.append("|").append(whiteSpaces);
        }

        //Add the delimiters
        if(delimiters != null)
        {
            for(Map.Entry<String, String> delimiter : delimiters.entrySet())
            {
                //Add starting delimiter
                wordSeparatorBuilder.append("|").append(delimiter.getKey());

                //Add ending delimiter (only if different from starting)
                if(!delimiter.getValue().equals(delimiter.getKey()))
                {
                    wordSeparatorBuilder.append("|").append(delimiter.getValue());
                }
            }
        }

        //Add the custom word separators
        if(specialWords != null)
        {
            for(String specialWord : specialWords)
            {
                if(!specialWord.isEmpty())
                {
                    wordSeparatorBuilder.append("|").append(specialWord);
                }
            }
        }

        return wordSeparatorBuilder.toString();
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
    private int moveToFirst(String regex)
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
    private int moveAfterFirst(String regex)
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

    /**
     * Check if the given text matches the given regex. </br>
     * There is a match if the text starts with an
     * substring that matches the regex. </br>
     * The entire text does not have to match the regex.
     *
     * @param regex The regex to match
     * @return True if there is a match, false otherwise
     */
    private boolean match(String text, String regex)
    {
        Matcher matcher = Pattern.compile(regex).matcher(text);     //Create a matcher
        return matcher.lookingAt();
    }
}
