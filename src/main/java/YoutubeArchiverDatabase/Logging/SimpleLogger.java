package YoutubeArchiverDatabase.Logging;

import YoutubeArchiverDatabase.Event.EventAggregator;
import YoutubeArchiverDatabase.Event.LogMessageEvent;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/// Thread-safe simple logger
public class SimpleLogger<T extends LogMessage> implements ILogger<T>
{
    private final List<T> logMessages;
    private final List<String> logs;
    private final int logCapacity = 1000;

    // {timestamp}   {level}   {message}
    private final static String MESSAGE_FORMAT = "%1s\t%2s\t%3s";

    public SimpleLogger()
    {
        this.logMessages = new CopyOnWriteArrayList<>();
        this.logs = new CopyOnWriteArrayList<>();
    }

    /// Returns log messages for output
    /// @return Log messages (current state)
    public List<T> GetMessages()
    {
        return this.logMessages;
    }

    public void Log(T logMessage)
    {
        if (this.logMessages.size() >= this.logCapacity)
        {
            this.logMessages.removeLast();
            this.logs.removeLast();
        }

        try
        {
            var log = FormatMessage(logMessage);

            this.logMessages.add(logMessage);
            this.logs.add(log);

            // Notify using event aggregator
            EventAggregator.GetEvent(LogMessageEvent.class)
                           .Publish(log);
        }
        catch (Exception e)
        {
            // CAN'T LOG!  ^_^
        }
    }

    public List<T> GetLogs()
    {
        return this.logMessages;
    }

    public String FormatMessage(T logMessage)
    {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (logMessage.Error == null)
            return String.format(MESSAGE_FORMAT, formatter.format(logMessage.Timestamp), logMessage.Level.toString(), logMessage.Message);

        else
        {
            // Message + Stack Trace
            var fullMessage =  Arrays.stream(logMessage.Error.getStackTrace())
                                     .map(StackTraceElement::toString)
                                     .reduce(logMessage.Message, (seed,element) ->
            {
                if (seed.isEmpty())
                    return element;

               return seed + "\n" + element;
            });

            return String.format(MESSAGE_FORMAT, formatter.format(logMessage.Timestamp), logMessage.Level.toString(), fullMessage);
        }
    }
}
