/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opensme.cope.factgenerators.lsa;

/**
 *
 * @author econst
 */
import edu.ucla.sspace.text.PorterStemmer;
import eu.opensme.cope.componentvalidator.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An iterator over all of the tokens present in a {@link BufferedReader} that
 * are separated by any amount of white space.
 */
public class WordIter implements Iterator<String> {

    /**
     * A fixed pattern matching all non-whitespace characters.
     */
    private static final Pattern notWhiteSpace = Pattern.compile("\\S+");
    /**
     * The stream from which to read tokens
     */
    private final BufferedReader br;
    /**
     * The next token to return
     */
    private String next;
    /**
     * The matcher that is tokenizing the current line
     */
    private Matcher matcher;
    /**
     * The current line being considered
     */
    private String curLine;
    /**
     * True if tokens should be stemmed using the Porter Stemmer algorithm.
     */
    private boolean stemWords;
    /**
     * True if words remained from previous iteration
     */
    private boolean remainedFromPrevious;
    /**
     * ArrayList with words remained from previous iteration
     */
    ArrayList<String> nextWords = new ArrayList<String>(0);
    /**
     * ArrayList with stop words that should not be included
     */
    ArrayList<String> stopWords = new ArrayList<String>(0);    

    /**
     * Constructs an iterator for all the tokens contained in the string
     */
    public WordIter(String str) {
        this(new BufferedReader(new StringReader(str)));
        this.loadEnglishKeywords();
        this.loadJavaKeywords();
    }

    public WordIter(String str, boolean useStemming) {
        this(new BufferedReader(new StringReader(str)), useStemming);
        this.loadEnglishKeywords();
        this.loadJavaKeywords();        
    }

    /**
     * Constructs an iterator for all the tokens contained in text of the
     * provided reader.
     */
    public WordIter(BufferedReader br) {
        this(br, false);
        this.loadEnglishKeywords();
        this.loadJavaKeywords();        
    }

    public WordIter(BufferedReader br, boolean useStemming) {
        this.br = br;
        curLine = null;
        stemWords = useStemming;
        this.loadEnglishKeywords();
        this.loadJavaKeywords();         
        advance();
    }

    /**
     * Advances to the next word in the buffer.
     */
    private void advance() {
        try {
            // loop until we find a word in the reader, or there are no more
            // words
            while (true) {
                // if we haven't looked at any lines yet, or if the index into
                // the current line is already at the end 
                if (curLine == null || !matcher.find()) {

                    String line = br.readLine();

                    // if there aren't any more lines in the reader, then mark
                    // next as null to indicate that there are no more words
                    if (line == null) {
                        next = null;
                        br.close();
                        return;
                    }

                    // create a new matcher to find all the tokens in this line
                    matcher = notWhiteSpace.matcher(line);
                    curLine = line;

                    // skip lines with no matches
                    if (!matcher.find()) {
                        continue;
                    }
                }

                next = curLine.substring(matcher.start(), matcher.end());
                checkForCapitalVariables(next);
                if (nextWords.size() == 1) {
                    next = nextWords.get(0);
                    nextWords.remove(0);
                    break;
                } else if (nextWords.size() > 1) {
                    remainedFromPrevious = true;
                    next = nextWords.get(0);
                    nextWords.remove(0);
                    break;
                }

            }
        } catch (IOException ioe) {
            throw new IOError(ioe);
        }
    }

    private void checkForCapitalVariables(String input) {
        String[] temp = input.split("(?=[A-Z])");
        String toInsert = "";
        for (int i = 0; i < temp.length; i++) {
            if (temp[i].length()==1 &&
                    Character.isUpperCase(temp[i].charAt(0))){
                toInsert+=temp[i];
                if (i==temp.length-1 &&
                        toInsert.length()>1 &&
                        checkStopWords(toInsert.toLowerCase())){
                    nextWords.add(toInsert.toLowerCase());
                }
                continue;
            }
            
            for (int j = 0; j < temp[i].length(); j++) {
                if (temp[i].charAt(j) < 65
                        || (temp[i].charAt(j) > 90 && temp[i].charAt(j) < 97)
                        || temp[i].charAt(j) > 122) {
                    if (toInsert.length()>1 &&
                        checkStopWords(toInsert.toLowerCase()))
                        nextWords.add(toInsert.toLowerCase());
                    toInsert="";
                    continue;
                }else{
                    toInsert+=temp[i].charAt(j);
                }
            }
            
            if (toInsert.length()>1 &&
                checkStopWords(toInsert.toLowerCase())){
                nextWords.add(toInsert.toLowerCase());
                toInsert ="";
            }
        }
    }
    
    private boolean checkStopWords(String toTest){
       if (!stopWords.contains(toTest))
           return true;
       
       return false;
    }
    
    private void loadJavaKeywords(){
        try{
            BufferedReader input = new BufferedReader(new FileReader(new File(Utils.getJarFolder() + "commonWordList/JavaLanguageStopWords.txt")));
            String line = "";
            
            while((line = input.readLine()) != null){
                StringTokenizer tok = new StringTokenizer(line,",");
                while (tok.hasMoreTokens())
                    stopWords.add(tok.nextToken());
            }
            input.close();
        }catch (IOException e){
            
        }
    }

    private void loadEnglishKeywords(){
        try{
            BufferedReader input = new BufferedReader(new FileReader(new File(Utils.getJarFolder() + "commonWordList/EnglishLanguageStopWords.txt")));
            String line = "";
            
            while((line = input.readLine()) != null){
                StringTokenizer tok = new StringTokenizer(line,",");
                while (tok.hasMoreTokens())
                    stopWords.add(tok.nextToken());
            }
            input.close();
        }catch (IOException e){
            
        }        
    }    
    
    /**
     * Returns {@code true} if there is another word to return.
     */
    public boolean hasNext() {
        return next != null;
    }

    /**
     * Returns the next word from the reader.
     */
    public String next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        if (remainedFromPrevious) {
            String temp = nextWords.get(0);
            nextWords.remove(0);
            if (nextWords.isEmpty()) {
                remainedFromPrevious = false;
            }
            return temp;
        }

        String s = next;
        advance();
        if (stemWords) {
            return PorterStemmer.stem(s);
        }
        return s;
    }

    /**
     * Throws an {@link UnsupportedOperationException} if called.
     */
    public void remove() {
        throw new UnsupportedOperationException("remove is not supported");
    }
}
