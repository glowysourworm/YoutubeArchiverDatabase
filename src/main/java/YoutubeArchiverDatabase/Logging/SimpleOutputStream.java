package YoutubeArchiverDatabase.Logging;

import java.io.IOException;
import java.io.OutputStream;

/// Custom OutputStream for re-directing the system output to Log4j
public class SimpleOutputStream extends OutputStream
{
    private final ILogger<LogMessage> logger;
    private final LogLevel level;
    private final StringBuilder buffer = new StringBuilder();

    public SimpleOutputStream(ILogger<LogMessage> logger, LogLevel level)
    {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void write(int b) throws IOException
    {
        char c = (char) b;
        if (c == '\n') {
            flushBuffer();
        } else if (c != '\r') {
            buffer.append(c);
        }
    }

    private synchronized void flushBuffer()
    {
        if (buffer.length() > 0)
        {
            logger.Log(new LogMessage(buffer.toString(), level, null));
            buffer.setLength(0); // Clear buffer
        }
    }

    @Override
    public void close() throws IOException
    {
        flushBuffer();
        super.close();
    }
}
