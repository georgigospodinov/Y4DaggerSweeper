package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Provides reading of ".props" files.
 *
 * @version 2.0
 */
public class Props {

    private final HashMap<String, String> STRING_PROPERTIES = new HashMap<>();
    private final HashMap<String, Long> LONG_PROPERTIES = new HashMap<>();
    private final HashMap<String, Integer> INT_PROPERTIES = new HashMap<>();
    private final HashMap<String, Double> DOUBLE_PROPERTIES = new HashMap<>();
    private final HashMap<String, Float> FLOAT_PROPERTIES = new HashMap<>();
    private static final String DEFAULT_PROPS_FILE = "default.props";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String COMMENT_SYMBOL = "#";
    private static final int KEY_INDEX = 0;
    private static final int VALUE_INDEX = 1;

    /**
     * Gives the number of properties currently in storage.
     *
     * @return the current number of properties
     */
    public int size() {
        int ss = STRING_PROPERTIES.size();
        int is = INT_PROPERTIES.size();
        int ls = LONG_PROPERTIES.size();
        int ds = DOUBLE_PROPERTIES.size();
        int fs = FLOAT_PROPERTIES.size();
        return ss + is + ls + ds + fs;
    }

    /**
     * Clears all the loaded properties.
     */
    public void clear() {
        STRING_PROPERTIES.clear();
        INT_PROPERTIES.clear();
        LONG_PROPERTIES.clear();
        FLOAT_PROPERTIES.clear();
        DOUBLE_PROPERTIES.clear();
    }

    /**
     * Get the {@link Integer} value associated with the given property.
     *
     * @param property the property to look for
     * @return the integer value associated with that property
     */
    public int getInt(String property) {
        if (!INT_PROPERTIES.containsKey(property))
            throw new NullPointerException("No integer property \"" + property + "\"");

        return INT_PROPERTIES.get(property);
    }

    /**
     * Get the {@link Long} value associated with the given property.
     * This method will check for an integer value if no long value is found.
     *
     * @param property the property to look for
     * @return the long value associated with that property
     */
    public long getLong(String property) {
        if (!LONG_PROPERTIES.containsKey(property)) {
            if (!INT_PROPERTIES.containsKey(property))
                throw new NullPointerException("No long property \"" + property + "\"");
            return INT_PROPERTIES.get(property);
        }

        return LONG_PROPERTIES.get(property);
    }

    /**
     * Get the {@link Float} value associated with the given property.
     *
     * @param property the property to look for
     * @return the double value associated with that property
     */
    public float getFloat(String property) {
        if (!FLOAT_PROPERTIES.containsKey(property))
            throw new NullPointerException("No float property \"" + property + "\"");

        return FLOAT_PROPERTIES.get(property);
    }

    /**
     * Get the {@link Double} value associated with the given property.
     * This method will check for a float value if no double value is found.
     *
     * @param property the property to look for
     * @return the long value associated with that property
     */
    public double getDouble(String property) {
        if (!DOUBLE_PROPERTIES.containsKey(property)) {
            if (!FLOAT_PROPERTIES.containsKey(property))
                throw new NullPointerException("No double property \"" + property + "\"");
            return FLOAT_PROPERTIES.get(property);
        }

        return DOUBLE_PROPERTIES.get(property);
    }

    /**
     * Get the number associated with the given property.
     * This method checks for a {@link Integer}, {@link Long}, {@link Float}, and {@link Double} in this order.
     * Once found, it is cast to integer and returned.
     *
     * @param property the property to look for
     * @return the value associated with that property after being cast to integer.
     */
    public int getAnyInt(String property) {
        if (INT_PROPERTIES.containsKey(property))
            return INT_PROPERTIES.get(property);

        if (LONG_PROPERTIES.containsKey(property))
            return LONG_PROPERTIES.get(property).intValue();

        if (FLOAT_PROPERTIES.containsKey(property))
            return FLOAT_PROPERTIES.get(property).intValue();

        if (DOUBLE_PROPERTIES.containsKey(property))
            return DOUBLE_PROPERTIES.get(property).intValue();

        throw new NullPointerException("No number property \"" + property + "\"");
    }

    /**
     * Checks if the String associated with the given property is equal to "true".
     *
     * @param property the property to look for
     * @return true if and only if the String associated with this property is equal to "true".
     */
    public boolean isTrue(String property) {
        if (!STRING_PROPERTIES.containsKey(property))
            throw new NullPointerException("No string property \"" + property + "\"");

        return STRING_PROPERTIES.get(property).equals("true");
    }

    /**
     * Get the {@link String} value associated with the given property.
     *
     * @param property the property to look for
     * @return the string value associated with that property
     */
    public String getString(String property) {
        if (!STRING_PROPERTIES.containsKey(property))
            throw new NullPointerException("No string property \"" + property + "\"");

        return STRING_PROPERTIES.get(property);
    }

    public void load() throws FileNotFoundException {
        load(DEFAULT_PROPS_FILE);
    }

    /**
     * Load properties from the specified file.
     *
     * @param filename the name of the file containing properties.
     *                 This can be absolute or relative path.
     * @throws FileNotFoundException if there is no file with the given name
     */
    public void load(String filename) throws FileNotFoundException {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
        }
        catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not find props file \"" + filename + "\"");
        }

        reader.lines().forEach(this::loadLine);
    }

    public Props() {
    }

    public Props(String filename) throws FileNotFoundException {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
        }
        catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not find props file \"" + filename + "\"");
        }

        reader.lines().forEach(this::loadLine);
    }

    private void loadLine(String line) {
        // Skip empty lines and comments
        if (line.isEmpty() || line.startsWith(COMMENT_SYMBOL)) return;

        String[] kv = line.split(KEY_VALUE_SEPARATOR);
        String key = kv[KEY_INDEX];
        String val = kv[VALUE_INDEX];

        try {  // Is this an integer?
            int value = Integer.parseInt(val);
            INT_PROPERTIES.put(key, value);
            return;
        }
        catch (NumberFormatException ignored) {
        }

        try {  // Is this a long?
            long value = Long.parseLong(val);
            LONG_PROPERTIES.put(key, value);
            return;
        }
        catch (NumberFormatException ignored) {
        }

        try {  // Is this a float?
            float value = Float.parseFloat(val);
            if (value == Float.POSITIVE_INFINITY)
                throw new NumberFormatException();
            if (value == Float.NEGATIVE_INFINITY)
                throw new NumberFormatException();
            FLOAT_PROPERTIES.put(key, value);
            return;
        }
        catch (NumberFormatException ignored) {
        }

        try {  // Is this a double?
            double value = Double.parseDouble(val);
            DOUBLE_PROPERTIES.put(key, value);
            return;
        }
        catch (NumberFormatException ignored) {
        }

        STRING_PROPERTIES.put(key, val);

    }
}
