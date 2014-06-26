package eu.livotov.labs.android.robotools.text;

import java.util.ArrayList;

public class StringTokenizer {

    private ArrayList<String> tokens;
    private int current;


    public StringTokenizer(String string, String delimiter) {
        tokens = new ArrayList<String>();
        current = 0;

        java.util.StringTokenizer tokenizer =
                new java.util.StringTokenizer(string, delimiter, true);

        boolean wasDelimiter = true;
        boolean isDelimiter = false;

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            isDelimiter = token.equals(delimiter);

            if (wasDelimiter) {
                tokens.add(isDelimiter ? "" : token);
            } else if (!isDelimiter) {
                tokens.add(token);
            }

            wasDelimiter = isDelimiter;
        }

        if (isDelimiter) {
            tokens.add("");
        }
    }

    public int countTokens() {
        return tokens.size();
    }

    public boolean hasMoreTokens() {
        return current < tokens.size();
    }

    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    public String nextElement() {
        return nextToken();
    }

    public String nextToken() {
        String token = tokens.get(current);
        current++;
        return token;
    }

}
