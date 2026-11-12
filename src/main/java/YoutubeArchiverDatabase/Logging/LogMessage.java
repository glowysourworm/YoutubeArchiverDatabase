package YoutubeArchiverDatabase.Logging;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class LogMessage
{
    public String Message;
    public Throwable Error;
    public LogLevel Level;
    public OffsetDateTime Timestamp;

    public LogMessage()
    {
        this.Message = "";
        this.Error = null;
        this.Level = LogLevel.Info;
        this.Timestamp = Instant.now().atOffset(OffsetDateTime.now().getOffset());
    }
    public LogMessage(String message, LogLevel level, Throwable error)
    {
        this.Message = message;
        this.Level = level;
        this.Error = error;
        this.Timestamp = Instant.now().atOffset(OffsetDateTime.now().getOffset());
    }

    public static LogMessage Info(String message)
    {
        var result = new LogMessage();

        result.Message = message;
        result.Level = LogLevel.Info;
        result.Error = null;

        return result;
    }
    public static LogMessage Info(String format, Object...arguments)
    {
        var result = new LogMessage();

        result.Message = String.format(format, arguments);
        result.Level = LogLevel.Info;
        result.Error = null;

        return result;
    }
    public static LogMessage Error(String message)
    {
        var result = new LogMessage();

        result.Message = message;
        result.Level = LogLevel.Error;
        result.Error = null;

        return result;
    }
    public static LogMessage Error(String message, Exception e)
    {
        var result = new LogMessage();

        result.Message = message;
        result.Level = LogLevel.Error;
        result.Error = e;

        return result;
    }
    public static LogMessage Error(String format, Exception e, Object...arguments)
    {
        var result = new LogMessage();

        result.Message = String.format(format, arguments);
        result.Level = LogLevel.Error;
        result.Error = e;

        return result;
    }
}
