package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeVideoDownloaderMetadata;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/// ShellRunner for the yt-dlp application [...](https://github.com/yt-dlp/yt-dlp)
public class YoutubeVideoRunner extends ShellRunner
{
    /*
     *   yt-dlp:  Youtube Audio / Video Downloader
     *
     *   Use Case 1:  Extract Youtube Channel Videos:  List of video id's in an output file
     *   Use Case 2:  Extract Video Metadata:  .json file of fields related to the video
     *   Use Case 3:  Download Audo / Video
     *
     *   Issues:  Authentication / Cookies  (most of the time there aren't issues; but some commands work and some don't)
     *
     *       - Cookies 1:  A browser challenge may be issued that requires authentication. You may add a cookies.txt file
     *                     to the bin directory (this would not be desired on the user end). There are browser extensions
     *                     to accomplish getting the cookies file out.
     *
     *                     --cookies {cookies.txt}      (this option may be used; but not liked)
     *
     *       - Cookies 2:  Authenticate using your browser - and leave it open. (Not a terrible option)
     *
     *       - Cookies 3:  Use a command that looks for your cookies file from the browser. This has security issues that
     *                     are still open; but there may be a way to help development of these along... that's a simple
     *                     issue to fix.
     *
     *                     --cookies-from-browser {browser name}
     *
     *                     Supported Browsers [brave, chrome, chromium, edge, firefox, opera, safari, vivaldi, whale]
     *
     *                     Known Issue:  https://github.com/yt-dlp/yt-dlp/issues/7271
     *
     *                     Chrome-based browsers currently have trouble with this option; but there is a plugin to solve
     *                     it.
     *
     *         Workaround: ChromeCookieUnlock  (yt-dlp plugin)
     *
     *   Use Cases
     *   ---------
     *
     *   For each use case it is assumed that you will require browser cookies to authenticate. The rest of the command
     *   line will be how to accomplish the task of the use case.
     *
     *   Also default (application) options: --js-runtimes node, --remote-components ejs:github
     *
     *   The JS runtime will be needed for browser automation; and the remote components was recommended as a way to
     *   handle browser challenges - which will fetch requirements from online.
     *
     *   Use Case 1:  yt-dlp --flat-playlist --print id {channelId | channelUrl}
     *
     *       - Flat Playlist:  Significantly reduces the run-time (more investigation needed)
     *       - Print Command:  There could be other fields available; but this will print the video id
     *
     *   Use Case 2: yt-dlp --write-info-json --write-subs --write-description --write-comments  --skip-download {videoId | videoUrl}
     *
     *       - Write Info Json:        Option to write as a JSON file (recommended) (we have setup metadata class mapping)
     *       - Write Subtitle File:    Option to write a file that describes subtitles (optional when downloading video)
     *       - Write Description File: Option to write a file that contains the video description
     *       - Write Comments File:    Option to write a file that contains the comments (NEEDS TO BE REVIEWED)
     *       - Skip Download:  Does not download a video file
     *
     *   Use Case 3: yt-dlp [--extract-audio] {videoId | videoUrl}
     */

    private static final String EXECUTABLE_NAME = "yt-dlp";
    private static final String WRITE_METADATA_OPTION = "--write-info-json";
    private static final String SKIP_VIDEO_OPTION = "--skip-video";
    private static final String APPLICATION_OPTION = "--js-runtimes node --remote-components ejs:github";

    // Application Configuration
    private final Configuration configuration;

    /// File that stores the resulting comments
    private YoutubeVideoDownloaderMetadata results;
    private String videoId;
    private boolean includeVideo;

    /// Initializes the YoutubeCommentRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    public YoutubeVideoRunner(final Configuration configuration)
    {
        super(EXECUTABLE_NAME);

        this.configuration = configuration;
        this.videoId = null;
        this.results = new YoutubeVideoDownloaderMetadata();
    }

    /// User code that will execute after initialization; and before execution.
    ///
    /// @param executableTempDirectory Working temp directory for the shell command to execute in.
    /// @return (success / failure) Execution of the Run sequence will halt if this method returns false.
    @Override
    protected boolean ExecutePrerun(Path executableTempDirectory)
    {
        return true;
    }

    /// Runs the yt-dlp command line application and waits for results. Returns an empty result on failure.
    /// @param videoId ID of the video to download
    /// @param includeVideo Set to true to include the actual video - which is sent to the youtube data directory.
    /// @return Youtube video metadata instance for the video
    /// @throws IllegalArgumentException Invalid video id provided (null or empty)
    public YoutubeVideoDownloaderMetadata Run(String videoId, boolean includeVideo) throws IllegalArgumentException
    {
        if (videoId == null || videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid channel id");

        // Procedure:
        //
        // 1) Initialize
        // 2) Edit our command line
        // 3) Run -> (pre / post) run
        // 4) Reset (cleanup temp directory)
        // 5) Read Comment File

        this.results = new YoutubeVideoDownloaderMetadata();
        this.videoId = videoId;
        this.includeVideo = includeVideo;

        // Initialize
        if (!super.Initialize())
        {
            ApplicationHelpers.Log(LogMessage.Error("Error initializing youtube-comment-downloader:  process halted"));
            return this.results;
        }

        var commandLine = CreateCommandLine(includeVideo);

        // Process Command Line -> (results created locally during post run)
        if (!super.Run(commandLine))
        {
            ApplicationHelpers.Log(LogMessage.Error("Error running youtube-comment-downloader:  process halted"));
            return this.results;
        }

        // Reset (cleanup)
        try
        {
            super.Reset();

            this.videoId = null;
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error deleting temp directory for youtube-comment-downloader:  process halted", e));

            return this.results;
        }

        // Results:  Read / Parse comments completed on post run
        return this.results;
    }

    /// User code that will execute after the application has exited.
    ///
    /// @param executableTempDirectory Working temp directory for the shell command to execute in.
    /// @param exitCode                Exit code of the shell application
    /// @return (success / failure) Execution of the Run sequence will halt if this method returns false - indicating
    ///         that the Run method (as a whole) has failed.
    @Override
    protected boolean ExecutePostrun(Path executableTempDirectory, int exitCode)
    {
        if (exitCode != 0)
            return false;

        try
        {
            var directory = super.GetExecutableTempDirectory();

            // Post Run:  1) Output JSON metadata; 2) Video file(s) (named by videoId) moved to our directory
            //
            var jsonFileOptional = Files.list(directory)
                                        .filter(path -> path.toString().endsWith(".json"))
                                        .findFirst();

            var jsonFile = jsonFileOptional.get();

            // Failure: Missing file(s)
            if (jsonFile.toString().isEmpty())
            {
                ApplicationHelpers.Log(LogMessage.Error("Missing video info json file (from yt-dlp output directory)"));
                return false;
            }

            // Read Video Metadata
            var fileBuffer = new FileReader(jsonFile.toString());
            var fileContents = fileBuffer.readAllAsString();

            fileBuffer.close();

            var mapper = new ObjectMapper();

            // no...THIS one will work:  We have missing properties in our class representation; and null primitives
            //                           must be configured by default.
            //
            mapper = mapper.rebuild()
                            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                            .build();

            // Deserialize Metadata:  as YoutubeVideoDownloaderMetadata
            this.results = mapper.readValue(fileContents, YoutubeVideoDownloaderMetadata.class);

            // Video File (optional)
            if (this.includeVideo)
            {
                // Get the resulting video file extension from the returned metadata
                var videoFileOptional = Files.list(directory)
                                             .filter(path -> path.toString().endsWith("." + this.results.ext))
                                             .findFirst();

                var videoFile = videoFileOptional.get();

                // Failure: Missing file(s)
                if (videoFile.toString().isEmpty())
                {
                    ApplicationHelpers.Log(LogMessage.Error("Missing video file (from yt-dlp output directory)"));
                    return false;
                }

                // Move Video File:  bin -> Youtube Data Directory (user)
                //
                var youtubeDirectory = ApplicationHelpers.ResolvePath(this.configuration.youtubeDataDirectory);

                Files.move(videoFile, Path.of(youtubeDirectory.toString(), videoFile.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
            }

            return true;
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error(String.format("Error running %1s", EXECUTABLE_NAME), e));
        }

        return false;
    }

    private List<String> CreateCommandLine(boolean includeVideo)
    {
        if (this.videoId == null || this.videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid channel id");

        var commandLine = new ArrayList<String>();

        // Process: yt-dlp (https://github.com/yt-dlp/yt-dlp)

//        // Option: User Name
//        commandLine.add(USER_NAME_OPTION);
//        commandLine.add(this.configuration.youtubePrimaryUser);
//
//        // Option: User Password
//        commandLine.add(PASSWORD_OPTION);
//        commandLine.add(this.configuration.youtubePrimaryPassword);

        // Option:  Login / Authentication
        //commandLine.add(LOGIN_OPTION);

        // Option:  Write Json Info
        commandLine.add(WRITE_METADATA_OPTION);

        // Option:  Skip Video
        if (!includeVideo)
            commandLine.add(SKIP_VIDEO_OPTION);

        // Option:  Format (default to mp4)
//        commandLine.add(YOUTUBE_VIDEO_DOWNLOADER_FORMAT_OPT);
//        commandLine.add(YOUTUBE_VIDEO_DOWNLOADER_FORMAT_DEFAULT);

        // Option:  Application Related Options (nodejs, ffmpeg, are required)
        commandLine.add(APPLICATION_OPTION);

        // Video Target (URL | Video ID)
        commandLine.add(videoId);

        return commandLine;
    }
}
