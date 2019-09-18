package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Wraps {@link BufferedWriter}, so that methods can be called without having to try-catch.
 *
 * @version 1.1
 */
public class WrappedWriter {
    private Logger l;
    private BufferedWriter writer;

    private void defaultCatch(Exception e) {
        if (l == null) e.printStackTrace();
        else l.log(e);
    }

    /**
     * Opens a {@link BufferedWriter} to the given filename.
     *
     * @param filename filename of output file
     * @param l        {@link Logger} to be used for logging {@link IOException}s.
     */
    public WrappedWriter(String filename, Logger l) {
        this.l = l;
        try {
            writer = new BufferedWriter(new FileWriter(filename));
        }
        catch (IOException e) {
            defaultCatch(e);
        }
    }

    /**
     * Opens a {@link BufferedWriter} to the given filename.
     * {@link IOException}s will be printed to standard error.
     * This is equivalent to using {@link WrappedWriter#WrappedWriter(String, Logger)} with null as the second argument.
     *
     * @param filename filename of output file
     */
    public WrappedWriter(String filename) {
        this(filename, null);
    }

    /**
     * Writes a {@link String}.
     *
     * @param s String to be written
     */
    public void write(String s) {
        try {
            writer.write(s);
        }
        catch (IOException e) {
            defaultCatch(e);
        }
    }

    /**
     * Writes the given {@link String} and then a line separator.
     * This method is equivalent to calling {@link WrappedWriter#write(String)}, followed by {@link WrappedWriter#newLine()}.
     *
     * @param s String to be written
     */
    public void writeLine(String s) {
        write(s);
        newLine();
    }

    /**
     * Writes a single character. (Unicode)
     *
     * @param c the character to be written
     */
    public void write(int c) {
        try {
            writer.write(c);
        }
        catch (IOException e) {
            defaultCatch(e);
        }
    }

    /**
     * Writes a line separator.
     * The line separator string is defined by the system property line.separator,
     * and is not necessarily a single newline ('\n') character.
     */
    public void newLine() {
        try {
            writer.newLine();
        }
        catch (IOException e) {
            defaultCatch(e);
        }
    }

    /**
     * Flushes the writer.
     */
    public void flush() {
        try {
            writer.flush();
        }
        catch (IOException e) {
            defaultCatch(e);
        }
    }

    /**
     * Closes the writer, flushing it first.
     */
    public void close() {
        try {
            writer.close();
        }
        catch (IOException e) {
            defaultCatch(e);
        }
    }

}
