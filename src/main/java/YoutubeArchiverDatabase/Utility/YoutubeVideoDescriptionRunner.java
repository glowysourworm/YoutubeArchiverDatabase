package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderRunType;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class YoutubeVideoDescriptionRunner extends YoutubeDownloaderRunner<String, String>
{
    private static final String EXECUTE_DESCRIPTION_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                                "--write-description --skip-download -o %(id)s.description {videoId}";

    private String videoId;

    /// Initializes the YoutubeCommentRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    /// @param configuration Application Configuration
    ///
    public YoutubeVideoDescriptionRunner(Configuration configuration)
    {
        super(YoutubeDownloaderRunType.VideoDescription, configuration);
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
    protected String ProvideResult(Path executableTempDirectory, int exitCode)
    {
        Path descriptionFile;

        // Get Description File:  .description
        try
        {
            var descriptionFileOptional = Files.list(executableTempDirectory)
                    .filter(path -> path.toString().endsWith(".description"))
                    .findFirst();

            descriptionFile = descriptionFileOptional.get();
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error reading .description file (from yt-dlp output directory)"));
            return null;
        }

        // Read Description File
        try(var fileBuffer = new FileReader(descriptionFile.toString()))
        {
            // Read Description
            return fileBuffer.readAllAsString();
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error reading .description file (from yt-dlp output directory)"));
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
        return EXECUTE_DESCRIPTION_TEMPLATE.replace("{videoId}", videoId);
    }
}
