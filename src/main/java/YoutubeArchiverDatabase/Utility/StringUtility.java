package YoutubeArchiverDatabase.Utility;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

/// String utility that removes implicit regex from the standard String.split(...).
public final class StringUtility
{
    public static String[] Split(String theString, char character)
    {
        return SplitImpl(theString, String.valueOf(character), EnumSet.of(StringSplitOptions.None));
    }
    public static String[] Split(String theString, char character, StringSplitOptions options)
    {
        return SplitImpl(theString, String.valueOf(character), EnumSet.of(options));
    }
    public static String[] Split(String theString, char character, EnumSet<StringSplitOptions> options)
    {
        return SplitImpl(theString, String.valueOf(character), options);
    }
    public static String[] Split(String theString, String separator)
    {
        return SplitImpl(theString, separator,  EnumSet.of(StringSplitOptions.None));
    }
    public static String[] Split(String theString, String separator, StringSplitOptions options)
    {
        return SplitImpl(theString, separator, EnumSet.of(options));
    }
    public static String[] Split(String theString, String separator, EnumSet<StringSplitOptions> options)
    {
        return SplitImpl(theString, separator, options);
    }
    private static String[] SplitImpl(String theString, String separator, EnumSet<StringSplitOptions> options)
    {
        if (theString == null)
            throw new NullPointerException("StringUtility.Split, input string was null");

        if (theString.isEmpty())
            return new String[]{theString};

        // Escapes the string to a regex literal
        var regexSeparator = Pattern.quote(separator);

        // Options:  None
        if (options.contains(StringSplitOptions.None) &&
            options.size() == 1)
            return theString.split(regexSeparator);

        // Process Result
        var splitResult = theString.split(regexSeparator);
        var result = new ArrayList<String>(List.of(splitResult));

        for (int index = result.size() - 1; index >= 0; index--)
        {
            // Options:  TrimEnds
            if (options.contains(StringSplitOptions.TrimEnds))
                result.set(index, result.get(index).trim());

            // Options:  Remove Empty Entries
            if (options.contains(StringSplitOptions.RemoveEmptyEntries) &&
                result.get(index).isBlank())
                result.remove(index);
        }

        return result.toArray(String[]::new);
    }
}
