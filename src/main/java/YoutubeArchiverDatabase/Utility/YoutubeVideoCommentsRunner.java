package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.Comments;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderRunType;

import java.nio.file.Path;
import java.util.List;

public class YoutubeVideoCommentsRunner extends YoutubeDownloaderRunner<List<Comments>, String>
{
    private static final String EXECUTE_COMMENTS_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                            "--write-info-json --write-comments --skip-download -o %(id)s.json {videoId}";

    private String videoId;

    /// Initializes the YoutubeVideoCommentsRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    /// @param configuration Application Configuration
    ///
    public YoutubeVideoCommentsRunner(Configuration configuration)
    {
        super(YoutubeDownloaderRunType.VideoComments, configuration);
    }

    @Override
    public void Initialize(String initializationData)
    {
        this.videoId = initializationData;
    }

    @Override
    public void ReInitialize(String initializationData)
    {
        this.videoId = initializationData;
    }

    @Override
    public boolean IsInitialized()
    {
        return this.videoId != null;
    }

    /// Exposed function to allow derived classes to parse and return the result after
    /// run has complete.
    ///
    /// @param executableTempDirectory Temp directory for the run data
    /// @param exitCode                Exit code of the application
    /// @return The result of the shell operation
    @Override
    protected List<Comments> ProvideResult(Path executableTempDirectory, int exitCode)
    {
        try
        {
            var metadata = super.ParseVideoMetadata(executableTempDirectory);


            if (metadata != null)
                return metadata.comments;

            else
            {
                ApplicationHelpers.Log(LogMessage.Error("Error parsing yt-dlp metadata file (for comments list)"));
                return null;
            }
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error parsing yt-dlp metadata file (for comments list)"));
            return null;
        }
    }

    /// Required function to retrieve the command line for the shell command from a
    /// derived class. This should exclude the executable name.
    ///
    /// @param executableTempDirectory Temp directory for the run data
    /// @return Complete command line (excluding the executable name)
    @Override
    protected String GetCommandLineArguments(Path executableTempDirectory)
    {
        // Command Template:  Replace {videoId}
        //
        return EXECUTE_COMMENTS_TEMPLATE.replace("{videoId}", videoId);
    }
}
