package util;

/**
 * Provides a progress bar that can be printed to indicate how much of the data has been generated.
 *
 * @version 1.2
 */
public class ProgressBar {

    /* Format:
    |--------------------| 100% (a/b)
     */

    private static final String PROGRESS_BAR_DASH = "-";
    private static final int PROGRESS_BAR_LENGTH = 20; // this is in dashes
    private static final double PROGRESS_PER_DASH = 100d / PROGRESS_BAR_LENGTH;

    /**
     * Calls {@link ProgressBar#formatBar(int, int, boolean)} with the supplied arguments and true for the carruage return.
     *
     * @param workDone  the amount of completed work
     * @param workTotal the total amount of work that needs to be done
     * @return The formatted progress bar.
     * @see ProgressBar#formatBar(int, int, boolean)
     */
    public static String formatBar(int workDone, int workTotal) {
        return formatBar(workDone, workTotal, true);
    }

    /**
     * Return a progress bar formatted as:
     * |--------------------| 100% (workDone/workTotal)
     * The percentage is calculated as (workDone * 100.0 / workTotal).
     *
     * @param workDone           the amount of completed work
     * @param workTotal          the total amount of work that needs to be done
     * @param withCarriageReturn whether or not the returned string should start with a \r
     * @return The formatted progress bar.
     */
    public static String formatBar(int workDone, int workTotal, boolean withCarriageReturn) {
        StringBuilder sb = new StringBuilder();
        if (withCarriageReturn) sb.append("\r");
        sb.append("|");

        double progress = workDone * 100d / workTotal;
        int numberOfDashes = (int) (progress / PROGRESS_PER_DASH);
        int i;
        for (i = 0; i < numberOfDashes; i++) sb.append(PROGRESS_BAR_DASH);
        while (i < PROGRESS_BAR_LENGTH) {
            sb.append(" ");
            i++;
        }
        sb.append("|\t").append(String.format("%.2f", progress)).append("%\t");
        sb.append("(").append(workDone).append("/").append(workTotal).append(")");
        return sb.toString();
    }
}
