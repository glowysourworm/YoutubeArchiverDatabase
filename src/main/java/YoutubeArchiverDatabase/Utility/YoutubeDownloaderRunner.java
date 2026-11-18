package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderMetadata;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderRunType;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/// ShellRunner for the yt-dlp application [...](https://github.com/yt-dlp/yt-dlp)
public abstract class YoutubeDownloaderRunner<T, TInit> extends ShellRunner implements IServiceNotifier<T>, IServiceInitializer<TInit>
{
    /*
     *   yt-dlp:  Youtube Audio / Video Downloader
     *
     *   Use Case 1:  Extract Youtube Channel Videos:  List of video id's in an output file
     *   Use Case 2:  Extract Video Metadata:  .json file of fields related to the video
     *   Use Case 3:  Extract Video List(s):   Lists may be printed to the console output; and are extracted there.
     *   Use Case 4:  Download Audo / Video
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
     *   The "Lists" that are output to the console using the --print command, or --list-subs command are extracted
     *   from the output using different methods. The --print command has a template option to use custom JSON. The
     *   other lists (subtitles_table, automatic_captions_table, ..) do not (currently) have JSON output; and must
     *   be extracted as by custom code.
     *
     *   Use Case 1:  yt-dlp --flat-playlist --print id {channelId | channelUrl}
     *
     *       - Flat Playlist:  Significantly reduces the run-time (more investigation needed)
     *       - Print Command:  There could be other fields available; but this will print the video id
     *
     *   Use Case 2: yt-dlp --write-info-json --write-subs --write-description --write-comments --skip-download {videoId | videoUrl}
     *
     *       - Write Info Json:        Option to write as a JSON file (recommended) (we have setup metadata class mapping)
     *       - Write Subtitle File:    Option to write a file that describes subtitles (optional when downloading video)
     *       - Write Description File: Option to write a file that contains the video description
     *       - Write Comments File:    Option to write a file that contains the comments (NEEDS TO BE REVIEWED)
     *       - Skip Download:  Does not download a video file
     *
     *   Use Case 3: yt-dlp --print [see video fields, automatic_captions_tables] --skip-download {videoId | videoUrl} > [temp.json, temp.txt]
     *
     *      - Print:  This has two options. The first will need a template string with fields (see documentation). The
     *                next will need custom code to parse the pseudo-csv table from the output.
     *
     *      - Pipe:  The > pipe will be used to create the temp file output from the console. A template parser would be
     *               wise to simplify the code logic with which to parse the output.
     *
     *   Use Case 4: yt-dlp [--extract-audio] {videoId | videoUrl}
     */

    private static final String EXECUTABLE_NAME = "yt-dlp";

    // Application Configuration
    private final Configuration configuration;

    // Run Configuration
    private final YoutubeDownloaderRunType runType;

    // Result of the complete execution of the shell program(s)
    private T result;

    // Listeners for notifications post-run
    private List<Consumer<T>> listeners;


    /// Initializes the YoutubeCommentRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    public YoutubeDownloaderRunner(final YoutubeDownloaderRunType runType, final Configuration configuration)
    {
        super(EXECUTABLE_NAME);

        this.configuration = configuration;
        this.runType = runType;
        this.result = null;
        this.listeners = new ArrayList<>();
    }

    /// Initializes service
    ///
    /// @param initializationData Data specific to the initialization
    @Override
    public abstract void Initialize(TInit initializationData);

    /// Allows for re-initialization of the service with some new data
    ///
    /// @param initializationData Data specific to the initialization
    @Override
    public abstract void ReInitialize(TInit initializationData);

    /// Returns true if the service is initialized
    ///
    /// @return True if the service is initialized
    @Override
    public abstract boolean IsInitialized();

    /// Adds listener Consumer to the notifications for the service
    ///
    /// @param listener User code to run on notify
    @Override
    public void AddListener(Consumer<T> listener)
    {
        this.listeners.add(listener);
    }

    /// Calls all listeners (in the order they were added)
    @Override
    public void Notify()
    {
        this.listeners.forEach(listener -> listener.accept(result));
    }

    /// Exposed function to allow derived classes to parse and return the result after
    /// run has complete.
    /// @param executableTempDirectory Temp directory for the run data
    /// @param exitCode Exit code of the application
    /// @return The result of the shell operation
    protected abstract T ProvideResult(Path executableTempDirectory, int exitCode);

    /// Required function to retrieve the command line for the shell command from a
    /// derived class. This should exclude the executable name.
    /// @param executableTempDirectory Temp directory for the run data
    /// @return Complete command line (excluding the executable name)
    protected abstract String GetCommandLineArguments(Path executableTempDirectory);

    /// Returns (final) application configuration
    protected Configuration GetConfiguration()
    {
        return this.configuration;
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

    /// Executes download runner. This method will check validation prior to running; setup; and teardown
    /// the temp directory and files involved with the run.
    /// @return Success / Failure
    /// @throws IllegalArgumentException Thrown when validation fails
    /// @throws IllegalStateException Thrown if the execution steps are out of order
    public boolean Execute() throws IllegalArgumentException, IllegalStateException
    {
        // Procedure:  Temp directory is set up by the parent class
        //
        // 1) Initialize (both parent class / derived initialization for the interface)
        // 2) Edit our command line
        // 3) Run -> (pre / post) run
        // 4) Reset (cleanup temp directory)
        // 5) Read Comment File

        // Initialize:  IServiceInitializer
        if (!this.IsInitialized())
        {
            ApplicationHelpers.Log(LogMessage.Error("Service not initialized. Must first call IServiceInitilizer.Initialize()"));
            return false;
        }

        // Initialize
        if (!super.Initialize())
        {
            ApplicationHelpers.Log(LogMessage.Error("Error initializing yt-dlp:  process halted"));
            return false;
        }

        var argumentString = GetCommandLineArguments(this.GetExecutableTempDirectory());

        // Process Command Line -> (results created locally during post run)
        if (!super.Run(argumentString))
        {
            ApplicationHelpers.Log(LogMessage.Error("Error running yt-dlp:  process halted"));
        }

        // Run Notifiers
        try
        {
            ApplicationHelpers.Log(LogMessage.Info("Notifying yt-dlp service listeners"));

            // -> IServiceNotifier
            Notify();
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error running service listener", e));
        }

        // Reset (cleanup)
        try
        {
            ApplicationHelpers.Log(LogMessage.Info("Cleaning up yt-dlp shell temp data"));

            super.Reset();
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error deleting temp directory for yt-dlp:  process halted", e));
            return false;
        }

        // Results:  Read / Parse comments completed on post run
        return true;
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
            // Post Run:
            //
            //    VideoMetadata:      Load metadata .json file
            //    VideoComments:      Load metadata .json file, set comments portion
            //    VideoDescription:   Load .description file, set description
            //    VideoSubtitleList:  Load subtitle .txt file; and execute parser
            //    VideoDownload:      Load metadata .json file, create ~/{youtube-data}/{channelId}/{videoId} video file
            //    VideoAudioDownload: Load metadata .json file, create (same) audio file (in the video directory)
            //    ChannelVideoList:   Load video list .txt file; and execute parser
            //

            this.result = ProvideResult(executableTempDirectory, exitCode);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error(String.format("Error running %1s", EXECUTABLE_NAME), e));
        }

        return this.result != null;
    }

    /// Verifies the video / audio download from the executable temp directory and returns the path to the
    /// result after moving the file to the youtube data directory specified in the Configuration.
    /// @param executableTempDirectory Temp directory for the shell run
    /// @return Path to the moved file
    /// @throws Exception Any exceptions related to file parsing, location, copying and deleting.
    protected Path VerifyAndMoveProduct(Path executableTempDirectory) throws Exception
    {
        // NOTE*** Only two files should be present in the output directory. There will be a metadata
        // file (.info.json), and an audio / video file (unknown extension)
        //

        // Validate:  1) Two files in the directory; 2) One file is named *.info.json
        //
        var metadataFileFound = new AtomicBoolean(false);
        var productFile = new AtomicReference<Path>();
        var count = new AtomicInteger(0);

        Files.walk(executableTempDirectory).forEach(path ->
        {
            if (path.toFile().isDirectory())
                return;

            if (path.toString().endsWith(".info.json"))
                metadataFileFound.set(true);

            else
                productFile.set(path);

            count.incrementAndGet();
        });

        // Validation Failure:  File count, Product File, Metadata File
        //
        if (count.get() != 2 ||
            productFile.get() == null ||
            !metadataFileFound.get())
        {
            ApplicationHelpers.Log(LogMessage.Error("Invalid file output (from yt-dlp output directory)"));
            return null;
        }

        // Move Video File:  bin -> Youtube Data Directory (user)
        //
        var youtubeDirectory = ApplicationHelpers.ResolvePath(this.configuration.youtubeDataDirectory);
        var destinationFile = Path.of(youtubeDirectory.toString(), productFile.get().getFileName().toString());

        return Files.move(productFile.get(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /// Parses video metadata file from the executable temp directory and returns the result. This may be used in
    /// any derived class; but the command line must be present for yt-dlp:  --write-info-json
    /// @param executableTempDirectory Temp directory for the shell run
    /// @return Metadata object for the youtube video
    /// @throws Exception Any error may occur related to file location, reading, and parsing.
    protected YoutubeDownloaderMetadata ParseVideoMetadata(Path executableTempDirectory) throws Exception
    {
        Path jsonFile = null;

        var jsonFileOptional = Files.list(executableTempDirectory)
                                    .filter(path -> path.toString().endsWith(".json"))
                                    .findFirst();

        jsonFile = jsonFileOptional.get();

        // Failure: Missing file(s)
        if (jsonFile.toString().isEmpty())
        {
            ApplicationHelpers.Log(LogMessage.Error("Missing video info json file (from yt-dlp output directory)"));
            return null;
        }

        try(var fileBuffer = new FileReader(jsonFile.toString()))
        {
            // Read Video Metadata
            var fileContents = fileBuffer.readAllAsString();

            fileBuffer.close();

            var mapper = new ObjectMapper();

            // no...THIS one will work:  We have missing properties in our class representation; and null primitives
            //                           must be configured by default.
            //
            mapper = mapper.rebuild()
                    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
                    .build();

            // Deserialize Metadata:  as YoutubeVideoDownloaderMetadata
            return mapper.readValue(fileContents, YoutubeDownloaderMetadata.class);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error parsing video metadata", e));
            throw e;
        }
    }
}
