package util;

/**
 * Provides methods to log information to files.
 *
 * @version 3.1
 */
public class Logger {

    private static final int FLUSH_PERIOD = 5000;//ms

    private WrappedWriter writer;

    /**
     * Used to control the flushing thread.
     *
     * @see Logger#periodicFlush()
     */
    private boolean contentsUpdated = false;

    /**
     * Used to control the flushing thread.
     *
     * @see Logger#periodicFlush()
     */
    private boolean running;

    /**
     * Opens a file to log to.
     * Starts a thread to periodically flush the log, so that if the main process crashes, some log remains.
     *
     * @param filename the name of the file to use for logging
     */
    public Logger(String filename) {
        writer = new WrappedWriter(filename);
        running = true;
        new Thread(this::periodicFlush).start();
    }

    /**
     * Closes the logger.
     */
    public void close() {
        writer.close();
        contentsUpdated = false;  // Closing causes a flush, so periodicFlush knows not to flush.
        running = false;
    }

    /**
     * Writes the given line to the log file and appends a line separator.
     *
     * @param line line to write
     */
    public void log(String line) {
        writer.writeLine(line);
        contentsUpdated = true;
    }

    /**
     * Writes the given {@link Throwable}'s description and then
     * the {@link StackTraceElement}s of the given {@link Throwable} to the log file.
     *
     * @param t {@link Throwable} to log
     */
    public void log(Throwable t) {
        log(t.toString());
        StackTraceElement[] stack = t.getStackTrace();
        for (StackTraceElement element : stack)
            log("\t" + element.toString());
    }

    /**
     * Periodically flushes the log.
     * In case the program crashes before the writer was properly closed,
     * there might be some log to read.
     */
    private void periodicFlush() {
        while (running) {
            try {
                Thread.sleep(FLUSH_PERIOD);
            }
            catch (InterruptedException ignored) {
            }

            if (!contentsUpdated) continue;
            writer.flush();
            contentsUpdated = false;
        }
    }
}
