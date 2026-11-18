package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderMetadata;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderRunType;

import java.nio.file.Path;

public class YoutubeVideoMetadataRunner extends YoutubeDownloaderRunner<YoutubeDownloaderMetadata, String>
{
    private static final String EXECUTE_METADATA_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                            "--write-info-json --write-comments --write-description --skip-download -o %(id)s.json {videoId}";

    private String videoId;

    /// Initializes the YoutubeCommentRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    /// @param configuration Application Configuration
    ///
    public YoutubeVideoMetadataRunner(Configuration configuration)
    {
        super(YoutubeDownloaderRunType.VideoMetadata, configuration);
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
    protected YoutubeDownloaderMetadata ProvideResult(Path executableTempDirectory, int exitCode)
    {
        try
        {
            return super.ParseVideoMetadata(executableTempDirectory);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error parsing metadata", e));
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
        return EXECUTE_METADATA_TEMPLATE.replace("{videoId}", videoId);
    }
}
