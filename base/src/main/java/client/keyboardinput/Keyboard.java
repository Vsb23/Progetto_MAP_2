//********************************************************************
//  Keyboard.java       Author: Lewis and Loftus
//
//  Facilitates keyboard input by abstracting details about input
//  parsing, conversions, and exception handling.
//********************************************************************

package keyboardinput;

import java.io.*;
import java.util.*;

/**
 * Provides static methods for reading different types of input from standard input.
 * Handles input parsing, conversions and error handling transparently.
 * 
 * @author Lewis and Loftus
 * @version 1.0
 */
public class Keyboard {
    private static boolean printErrors = true;

    private static int errorCount = 0;

    /**
     * Returns the current count of input errors encountered.
     * @return the number of input errors since last reset
     */
    public static int getErrorCount() {
        return errorCount;
    }

    /**
     * Resets the input error counter to zero.
     */
    public static void resetErrorCount() {
        errorCount = 0;
    }

    /**
     * Checks if error messages are being displayed.
     * @return true if error messages are printed, false otherwise
     */
    public static boolean getPrintErrors() {
        return printErrors;
    }

    /**
     * Sets whether error messages should be displayed.
     * @param flag true to show error messages, false to hide them
     */
    public static void setPrintErrors(boolean flag) {
        printErrors = flag;
    }

    /**
     * Internal method to handle input errors.
     * @param str the error message to display
     */
    private static void error(String str) {
        errorCount++;
        if (printErrors)
            System.out.println(str);
    }

    // ************* Tokenized Input Stream Section ******************

    private static String current_token = null;

    private static StringTokenizer reader;

    private static BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

    /**
     * Gets the next input token (may read new line).
     * @return the next token from input
     */
    private static String getNextToken() {
        return getNextToken(true);
    }

    /**
     * Gets the next input token, optionally skipping to next line.
     * @param skip true to skip whitespace, false to read exactly one token
     * @return the next token from input
     */
    private static String getNextToken(boolean skip) {
        String token;

        if (current_token == null)
            token = getNextInputToken(skip);
        else {
            token = current_token;
            current_token = null;
        }

        return token;
    }

    /**
     * Internal method to read next token from input stream.
     * @param skip true to skip whitespace
     * @return the next token or null on error
     */
    private static String getNextInputToken(boolean skip) {
        final String delimiters = " \t\n\r\f";
        String token = null;

        try {
            if (reader == null)
                reader = new StringTokenizer(in.readLine(), delimiters, true);

            while (token == null || ((delimiters.indexOf(token) >= 0) && skip)) {
                while (!reader.hasMoreTokens())
                    reader = new StringTokenizer(in.readLine(), delimiters,
                            true);

                token = reader.nextToken();
            }
        } catch (Exception exception) {
            token = null;
        }

        return token;
    }

    /**
     * Checks if current input line has been fully read.
     * @return true if no more tokens on current line, false otherwise
     */
    public static boolean endOfLine() {
        return !reader.hasMoreTokens();
    }

    // ************* Reading Section *********************************

    /**
     * Reads a full line of text from standard input.
     * @return the string read, or null on error
     */
    public static String readString() {
        String str;

        try {
            str = getNextToken(false);
            while (!endOfLine()) {
                str = str + getNextToken(false);
            }
        } catch (Exception exception) {
            error("Errore durante la lettura della stringa, valore null restituito.");
            str = null;
        }
        return str;
    }

    /**
     * Reads a single word (whitespace delimited) from standard input.
     * @return the word read, or null on error
     */
    public static String readWord() {
        String token;
        try {
            token = getNextToken();
        } catch (Exception exception) {
            error("Errore durante la lettura della stringa, valore null restituito.");
            token = null;
        }
        return token;
    }

    /**
     * Reads a boolean value from standard input.
     * Accepts "true" or "false" (case insensitive).
     * @return the boolean value read, or false on error
     */
    public static boolean readBoolean() {
        String token = getNextToken();
        boolean bool;
        try {
            if (token.toLowerCase().equals("true"))
                bool = true;
            else if (token.toLowerCase().equals("false"))
                bool = false;
            else {
                error("Errore durante la lettura del boolean, valore false restituito.");
                bool = false;
            }
        } catch (Exception exception) {
            error("Errore durante la lettura del boolean, valore false restituito.");
            bool = false;
        }
        return bool;
    }

    /**
     * Reads a single character from standard input.
     * @return the character read, or Character.MIN_VALUE on error
     */
    public static char readChar() {
        String token = getNextToken(false);
        char value;
        try {
            if (token.length() > 1) {
                current_token = token.substring(1, token.length());
            } else
                current_token = null;
            value = token.charAt(0);
        } catch (Exception exception) {
            error("Errore durante la lettura del carattere, valore MIN_VALUE restituito.");
            value = Character.MIN_VALUE;
        }

        return value;
    }

    /**
     * Reads an integer from standard input.
     * @return the integer read, or Integer.MIN_VALUE on error
     */
    public static int readInt() {
        String token = getNextToken();
        int value;
        try {
            value = Integer.parseInt(token);
        } catch (Exception exception) {
            error("Errore durante la lettura del valore intero, valore MIN_VALUE restituito.");
            value = Integer.MIN_VALUE;
        }
        return value;
    }

    /**
     * Reads a long integer from standard input.
     * @return the long value read, or Long.MIN_VALUE on error
     */
    public static long readLong() {
        String token = getNextToken();
        long value;
        try {
            value = Long.parseLong(token);
        } catch (Exception exception) {
            error("Errore durante la lettura del long, valore MIN_VALUE restituito.");
            value = Long.MIN_VALUE;
        }
        return value;
    }

    /**
     * Reads a float from standard input.
     * @return the float value read, or Float.NaN on error
     */
    public static float readFloat() {
        String token = getNextToken();
        float value;
        try {
            value = (new Float(token)).floatValue();
        } catch (Exception exception) {
            error("Il valore inserito non è un numero valido");
            value = Float.NaN;
        }
        return value;
    }

    /**
     * Reads a double from standard input.
     * @return the double value read, or Double.NaN on error
     */
    public static double readDouble() {
        String token = getNextToken();
        double value;
        try {
            value = (new Double(token)).doubleValue();
        } catch (Exception exception) {
            error("Il valore inserito non è un numero valido");
            value = Double.NaN;
        }
        return value;
    }
}