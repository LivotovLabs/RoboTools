package eu.livotov.labs.android.robotools.compat.v1.text;

import java.util.ArrayList;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 21.03.13
 */
public class RTStringTokenizer
{

    private ArrayList tokens_;
    private int current_;


    public RTStringTokenizer(String string, String delimiter)
    {
        tokens_ = new ArrayList();
        current_ = 0;

        java.util.StringTokenizer tokenizer =
                new java.util.StringTokenizer(string, delimiter, true);

        boolean wasDelimiter = true;
        boolean isDelimiter = false;

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();

            isDelimiter = token.equals(delimiter);

            if (wasDelimiter)
            {
                tokens_.add(isDelimiter ? "" : token);
            } else if (!isDelimiter)
            {
                tokens_.add(token);
            }

            wasDelimiter = isDelimiter;
        }

        if (isDelimiter)
        {
            tokens_.add("");
        }
    }

    public int countTokens()
    {
        return tokens_.size();
    }

    public boolean hasMoreTokens()
    {
        return current_ < tokens_.size();
    }

    public boolean hasMoreElements()
    {
        return hasMoreTokens();
    }

    public Object nextElement()
    {
        return nextToken();
    }

    public String nextToken()
    {
        String token = (String) tokens_.get(current_);
        current_++;
        return token;
    }

}