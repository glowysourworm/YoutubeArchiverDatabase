package YoutubeArchiverDatabase.Logging;

import java.util.List;

public interface ILogger<T extends LogMessage>
{
    void Log(T logMessage);
    List<T> GetLogs();

    /// Applies logger format to the message
    String FormatMessage(T logMessage);
}
