package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderRunType;

import java.nio.file.Path;

public class YoutubeVideoDownloadRunner extends YoutubeDownloaderRunner<Path, String>
{
    private static final String EXECUTE_VIDEO_DOWNLOAD_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                                    "--write-info-json -o %(id)s.json {videoId}";

    private String videoId;

    /// Initializes the YoutubeCommentRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    public YoutubeVideoDownloadRunner(final Configuration configuration)
    {
        super(YoutubeDownloaderRunType.VideoAudioDownload, configuration);
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

    @Override
    protected String GetCommandLineArguments(final Path executableTempDirectory)
    {
        // Command Template:  Replace {videoId}
        //
        return EXECUTE_VIDEO_DOWNLOAD_TEMPLATE.replace("{videoId}", this.videoId);
    }

    @Override
    protected Path ProvideResult(Path executableTempDirectory, int exitCode)
    {
        if (exitCode != 0)
            return null;

        try
        {
            return VerifyAndMoveVideoResult(executableTempDirectory);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error moving or locating result audio file", e));
            return null;
        }
    }
}
