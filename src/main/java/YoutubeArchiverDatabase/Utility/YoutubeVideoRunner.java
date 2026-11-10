package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Model.YoutubeComment;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeVideoDownloaderMetadata;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.io.FileReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/// ShellRunner for the yt-dlp application [...](https://github.com/yt-dlp/yt-dlp)
public class YoutubeVideoRunner extends ShellRunner
{
    private static final String EXECUTABLE_NAME = "yt-dlp";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_WRITE_METADATA_OPT = "--write-info-json";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_SKIP_VIDEO_OPT = "--skip-video";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_FORMAT_OPT = "-f";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_FORMAT_DEFAULT = "best";
    // private static final String YOUBUTE_VIDEO_DOWNLOADER_NO_MERGE_OPT = "--no-video-multistreams";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_APPLICATION_OPT = "--js-runtimes node --remote-components ejs:github";

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
            System.out.println("Error initializing youtube-comment-downloader:  process halted");
            return this.results;
        }

        var commandLine = CreateCommandLine(includeVideo);

        // Process Command Line -> (results created locally during post run)
        if (!super.Run(commandLine))
        {
            System.out.println("Error running youtube-comment-downloader:  process halted");
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
            System.out.println("Error deleting temp directory for youtube-comment-downloader:  process halted");
            e.printStackTrace();

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
                System.out.println("Missing video info json file (from yt-dlp output directory)");
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
                    System.out.println("Missing video file (from yt-dlp output directory)");
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
            // Log Error
            e.printStackTrace();
        }

        return false;
    }

    private List<String> CreateCommandLine(boolean includeVideo)
    {
        if (this.videoId == null || this.videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid channel id");

        var commandLine = new ArrayList<String>();

        // Process: yt-dlp (https://github.com/yt-dlp/yt-dlp)
        // commandLine.add(YOUTUBE_VIDEO_DOWNLOADER);

        // Option:  Write Json Info
        commandLine.add(YOUTUBE_VIDEO_DOWNLOADER_WRITE_METADATA_OPT);

        // Option:  Skip Video
        if (!includeVideo)
            commandLine.add(YOUTUBE_VIDEO_DOWNLOADER_SKIP_VIDEO_OPT);

        // Option:  Format (default to mp4)
//        commandLine.add(YOUTUBE_VIDEO_DOWNLOADER_FORMAT_OPT);
//        commandLine.add(YOUTUBE_VIDEO_DOWNLOADER_FORMAT_DEFAULT);

        // Option:  Application Related Options (nodejs, ffmpeg, are required)
        commandLine.add(YOUTUBE_VIDEO_DOWNLOADER_APPLICATION_OPT);

        // Video Target (URL | Video ID)
        commandLine.add(videoId);

        return commandLine;
    }
}
