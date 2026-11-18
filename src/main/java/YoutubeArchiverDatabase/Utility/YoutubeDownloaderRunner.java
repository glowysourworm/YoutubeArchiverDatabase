package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Model.YoutubeChannel;
import YoutubeArchiverDatabase.Model.YoutubeVideo;
import YoutubeArchiverDatabase.Service.YoutubeEntityHashCalculator;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.Comments;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.Subtitles;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderMetadata;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderRunType;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

/// ShellRunner for the yt-dlp application [...](https://github.com/yt-dlp/yt-dlp)
public class YoutubeDownloaderRunner extends ShellRunner
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

    // Print command separator character (the output is a simple table, so any character string wil do.. something
    // random or unlikely to be used)
    //private static final String PRINT_SEPARATOR = "\r\n";

    // Name for the channel video list
    private static final String VIDEO_LIST_FILE_NAME= "channel-video-list.txt";

    // This is a comma-separated list of video fields we use for YoutubeVideo
    private static final String VIDEO_METADATA_FIELDS = "id,title,fulltitle,description,thumbnail,uploader_id,uploader_url,release_timestamp," +
                                                        "modified_timestamp,channel_id,channel,channel_url,view_count,like_count,dislike_count,repost_count," +
                                                        "live_status,availability,playlist_id,playlist_title,playlist," +
                                                        "playlist_count,playlist_index,playlist_channel_id,playlist_uploader_id,playlist_webpage_url";

    // JSON types:  s = string, d = number, b = boolean
    private static final String VIDEO_METADATA_FIELD_TYPES = "s,s,s,s,s,s,s,d," +
                                                             "d,s,s,s,d,d,d,d," +
                                                             "s,s,s,s,s," +
                                                             "d,d,s,s,s";

    private static final String EXECUTE_METADATA_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                            "--write-info-json --write-comments --write-description --skip-download -o %(id)s.json {videoId}";

    private static final String EXECUTE_COMMENTS_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                            "--write-info-json --write-comments --skip-download -o %(id)s.json {videoId}";

    private static final String EXECUTE_DESCRIPTION_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                               "--write-description --skip-download -o %(id)s.description {videoId}";

    private static final String EXECUTE_VIDEO_LIST_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                              "--flat-playlist --skip-download --print-to-file {videoFields} {videoListFileName} {channelId}";

    private static final String EXECUTE_VIDEO_SUBTITLE_LIST_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                                       "--skip-download --print-to-file automatic_captions_tables %(id)s.txt {videoId}";

    private static final String EXECUTE_VIDEO_DOWNLOAD_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                                  "--write-info-json -o %(id)s.json {videoId}";

    private static final String EXECUTE_AUDIO_DOWNLOAD_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                                  "--write-info-json --extract-audio -o %(id)s.json {videoId}";

    // Application Configuration
    private final Configuration configuration;

    // Run Configuration
    private YoutubeDownloaderRunType runType;

    // Results data depends on how the command line is used. Most of the time, you'll have a
    // metadata output (.info.json) which is useful for all the video related data. Some
    // output data is sent to a special file (per use case).

    // Result of the .info.json file
    private YoutubeDownloaderMetadata metadataResult;

    // Result of the video list download for a channel
    private List<YoutubeVideo> videoListResult;

    // Result of the video subtitles list option
    private List<Subtitles> videoSubtitles;

    // Result of the video comments list option
    private List<Comments> videoComments;

    // Result of the video download (path to the video)
    private Path videoDownloadResult;

    // Result of the audio download (path to the audio)
    private Path audioDownloadResult;

    // Result of the .description file for a video (sizeable string)
    private String descriptionResult;

    /// Initializes the YoutubeCommentRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    public YoutubeDownloaderRunner(final Configuration configuration)
    {
        super(EXECUTABLE_NAME);

        this.configuration = configuration;
        this.metadataResult = null;
        this.videoListResult = null;
        this.videoSubtitles = null;
        this.videoComments = null;
        this.descriptionResult = null;
    }

    /// Extracts: Comments, Description for the Youtube video. Returns null if there was a runtime error.
    /// @param videoId Youtube ID of the video
    /// @return Set of meta-data based on yt-dlp
    public YoutubeDownloaderMetadata ExecuteGetMetadata(String videoId) throws IllegalArgumentException
    {
        if (videoId == null || videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid video id");

        // Command Template:  Replace {videoId}
        //
        var commandLine = EXECUTE_METADATA_TEMPLATE.replace("{videoId}", videoId);

        // -> Run (will execute all phases of the run; and parse result object(s))
        try
        {
            RunImpl(YoutubeDownloaderRunType.VideoMetadata, commandLine);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error completing yt-dlp command", e));
        }

        return this.metadataResult;
    }


    /// Extracts the comments from the video webpage. Returns null if there was a runtime error.
    /// @param videoId Youtube ID of the video
    /// @return Set of comment data based on yt-dlp
    public List<Comments> ExecuteGetComments(String videoId) throws IllegalArgumentException
    {
        if (videoId == null || videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid channel id");

        // Command Template:  Replace {videoId}
        //
        var commandLine = EXECUTE_COMMENTS_TEMPLATE.replace("{videoId}", videoId);

        // -> Run (will execute all phases of the run; and parse result object(s))
        try
        {
            RunImpl(YoutubeDownloaderRunType.VideoComments, commandLine);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error completing yt-dlp comments fetch", e));
        }

        return this.videoComments;
    }

    /// Extracts video data for a Youtube channel. Returns null if there was a runtime error.
    /// @param channelId Youtube ID of the channel
    /// @return List of YoutubeVideo entities for the channel (will not contain related data)(no persistence to database)
    public List<YoutubeVideo> ExecuteGetVideoList(String channelId) throws IllegalArgumentException
    {
        if (channelId == null || channelId.isBlank())
            throw new IllegalArgumentException("Missing or invalid channel id");

        // Video Fields:  The output template has limitations. We're going to use { json video fields }
        //                which will print  { result1 }, { result2 }, ...
        //
        var videoFieldNames = StringUtility.Split(VIDEO_METADATA_FIELDS, ",");
        var videoFieldTypes = StringUtility.Split(VIDEO_METADATA_FIELD_TYPES, ",");
        var videoFields = new ArrayList<String>();
        var template = "%({fieldName}){fieldType},";

        // Replace field template to join pseudo-json output
        for (int index = 0; index <  videoFieldNames.length; index++)
        {
            var value = template.replace("{fieldName}", videoFieldNames[index])
                                .replace("{fieldType}", videoFieldTypes[index]);

            videoFields.add(value);
        }

        // Join fields to make the final template for yt-dlp. Each line of the output will be a separate
        // JSON entity. We can parse it from there.
        var finalVideoFields = videoFields.stream()
                                          .reduce("", (seed, current) ->
        {
            if (seed.isBlank())
                return current;

            return seed + current;
        });

        // Remove trailing comma:  field1,field2,...,fieldn,
        finalVideoFields = finalVideoFields.substring(0, finalVideoFields.length() - 1);

        // Command Template:  Replace {videoFields}, {videoListFileName}, {channelId}
        //
        var commandLine = EXECUTE_VIDEO_LIST_TEMPLATE.replace("{videoFields}", finalVideoFields)
                                                     .replace("{videoListFileName}", VIDEO_LIST_FILE_NAME)
                                                     .replace("{channelId}", channelId);

        // -> Run (will execute all phases of the run; and parse result object(s))
        try
        {
            RunImpl(YoutubeDownloaderRunType.ChannelVideoList, commandLine);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error completing yt-dlp comments fetch", e));
        }

        return this.videoListResult;
    }

    /// Extracts the description from the video webpage. Returns null if there was a runtime error.
    /// @param videoId Youtube ID of the video
    /// @return Description of the video (length may exceed database capacity for the field)
    public String ExecuteGetVideoDescription(String videoId) throws IllegalArgumentException
    {
        if (videoId == null || videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid video id");

        // Command Template:  Replace {videoId}
        //
        var commandLine = EXECUTE_DESCRIPTION_TEMPLATE.replace("{videoId}", videoId);

        // -> Run (will execute all phases of the run; and parse result object(s))
        try
        {
            RunImpl(YoutubeDownloaderRunType.VideoDescription, commandLine);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error completing yt-dlp fetch", e));
        }

        return this.descriptionResult;
    }

    /// Extracts the description from the video webpage. Returns null if there was a runtime error.
    /// @param videoId Youtube ID of the video
    /// @return List of subtitle options (can be used with video download)
    public List<Subtitles> ExecuteGetSubtitleList(String videoId) throws IllegalArgumentException
    {
        if (videoId == null || videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid video id");

        // Command Template:  Replace {videoId}
        //
        var commandLine = EXECUTE_VIDEO_SUBTITLE_LIST_TEMPLATE.replace("{videoId}", videoId);

        // -> Run (will execute all phases of the run; and parse result object(s))
        try
        {
            RunImpl(YoutubeDownloaderRunType.VideoSubtitleList, commandLine);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error completing yt-dlp fetch", e));
        }

        return this.videoSubtitles;
    }

    /// Downloads the specified video. Returns null if there was a runtime error.
    /// @param videoId Youtube ID of the video
    /// @return Absolute path to the video file
    public Path ExecuteDownloadVideo(String videoId) throws IllegalArgumentException
    {
        if (videoId == null || videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid video id");

        // Command Template:  Replace {videoId}
        //
        var commandLine = EXECUTE_VIDEO_DOWNLOAD_TEMPLATE.replace("{videoId}", videoId);

        // -> Run (will execute all phases of the run; and parse result object(s))
        try
        {
            RunImpl(YoutubeDownloaderRunType.VideoDownload, commandLine);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error completing yt-dlp fetch", e));
        }

        return this.videoDownloadResult;
    }

    /// Downloads the specified audio. Returns null if there was a runtime error.
    /// @param videoId Youtube ID of the video
    /// @return Absolute path to the audio file
    public Path ExecuteDownloadAudio(String videoId) throws IllegalArgumentException
    {
        if (videoId == null || videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid video id");

        // Command Template:  Replace {videoId}
        //
        var commandLine = EXECUTE_AUDIO_DOWNLOAD_TEMPLATE.replace("{videoId}", videoId);

        // -> Run (will execute all phases of the run; and parse result object(s))
        try
        {
            RunImpl(YoutubeDownloaderRunType.VideoAudioDownload, commandLine);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error completing yt-dlp fetch", e));
        }

        return this.audioDownloadResult;
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

    protected boolean RunImpl(YoutubeDownloaderRunType runType, String argumentString) throws IllegalArgumentException
    {
        // Procedure:
        //
        // 1) Initialize
        // 2) Edit our command line
        // 3) Run -> (pre / post) run
        // 4) Reset (cleanup temp directory)
        // 5) Read Comment File

        this.runType = runType;

        // Initialize
        if (!super.Initialize())
        {
            ApplicationHelpers.Log(LogMessage.Error("Error initializing yt-dlp:  process halted"));
            return false;
        }

        // Process Command Line -> (results created locally during post run)
        if (!super.Run(argumentString))
        {
            ApplicationHelpers.Log(LogMessage.Error("Error running yt-dlp:  process halted"));
            return false;
        }

        // Reset (cleanup)
        try
        {
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

            switch (this.runType)
            {
                case VideoMetadata:
                {
                    this.metadataResult = ParseVideoMetadata(executableTempDirectory);
                    return this.metadataResult != null;
                }
                case VideoComments:
                {
                    this.videoComments = LoadCommentsFromMetadata(executableTempDirectory);
                    return this.videoComments != null;
                }
                case VideoDescription:
                {
                    this.descriptionResult = LoadVideoDescription(executableTempDirectory);
                    return this.descriptionResult != null;
                }
                case VideoSubtitleList:
                {
                    this.videoSubtitles = LoadSubtitleList(executableTempDirectory);
                    return this.videoSubtitles != null;
                }
                case VideoDownload:
                {
                    // These share code (for now). There would be other options available to the user.
                    // So, in the future, this may be a separate method.
                    this.videoDownloadResult = VerifyAndMoveVideoResult(executableTempDirectory);
                    return this.videoDownloadResult != null;
                }
                case VideoAudioDownload:
                {
                    // These share code (for now). Essentially, the downloader performs an extra operation
                    // to extract the audio. There would be other options available to the user. So, in the
                    // future, this may be a separate method.
                    this.audioDownloadResult = VerifyAndMoveVideoResult(executableTempDirectory);
                    return this.audioDownloadResult != null;
                }
                case ChannelVideoList:
                {
                    this.videoListResult = GetVideoListResult(executableTempDirectory);
                    return this.videoListResult != null;
                }
                default:
                    throw new Exception("Invalid run type");
            }
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error(String.format("Error running %1s", EXECUTABLE_NAME), e));
        }

        return false;
    }

    /// Cleans up directory and files from previous run. Must be called before starting a new run.
    /// @throws Exception This occurs usually when there is a problem cleaning up files or folders.
    public void Reset() throws Exception
    {
        if (super.HasRun())
            super.Reset();
    }

    //region (private) Post-Run Parsers

    private List<YoutubeVideo> GetVideoListResult(Path executableTempDirectory) throws Exception
    {
        // Channel Video List File:  Should be the only .txt file in the temp directory. The format
        // was specified above.
        //
        // { field1Name : field1Value, field2Name : field2Value, ... },{ [entity2] }, { [entity3] }, ...
        //
        var videoListFileOptional = Files.list(executableTempDirectory)
                                         .filter(path -> path.getFileName().toString().equals(VIDEO_LIST_FILE_NAME))
                                         .findFirst();

        var videoListFile = videoListFileOptional.get();

        var result = new ArrayList<YoutubeVideo>();

        // Filter yt-dlp video field results
        Function<String, String> fieldStringFilter = (fieldValue ->
        {
            // NA => no result (we're going to use empty strings)
            if (fieldValue == null || fieldValue.equals("NA"))
                return "";

            return fieldValue;
        });

        // Filter / Parse timestamp
        Function<String, Instant> parseTimestamp = (fieldValue ->
        {
            try
            {
                var unixTimestamp = Long.parseLong(fieldValue);
                return Instant.ofEpochSecond(unixTimestamp);
            }
            catch (Exception e)
            {
                ApplicationHelpers.Log(LogMessage.Error("Error parsing timestamp for video list file", e));
                return Instant.MIN;
            }
        });

        try (var fileReader = new FileReader(videoListFile.toString()))
        {
            // Read Video Metadata
            var fileContents = fileReader.readAllAsString();

            fileReader.close();

            // Separate lines in the result file
            var entities = StringUtility.Split(fileContents,"\r\n");

            // Use Video Fields (above):  Read off the field values from the pseudo-json
            //
            for (var entity : entities)
            {
                // Fields:  field1Value,field2Value,...
                //
                var fieldValues = StringUtility.Split(entity, ",");

                var youtubeChannel = new YoutubeChannel();
                var youtubeVideo = new YoutubeVideo();

                // VERIFY INDICES!  (see VIDEO_METADATA_FIELDS)
                youtubeVideo.setId(fieldStringFilter.apply(fieldValues[0]));
                youtubeVideo.setTitle(fieldStringFilter.apply(fieldValues[1]));
                youtubeVideo.setDescription(fieldStringFilter.apply(fieldValues[3]));
                youtubeVideo.setImageURL(fieldStringFilter.apply(fieldValues[4]));
                youtubeVideo.setApiCreated(false);

                // ChannelId
                youtubeChannel.setId(fieldStringFilter.apply(fieldValues[9]));
                youtubeChannel.setName(fieldStringFilter.apply(fieldValues[10]));
                youtubeChannel.setImageURL(fieldStringFilter.apply(fieldValues[11]));
                youtubeChannel.setApiCreated(false);

                // -> Set data for YoutubeChannel (non-API) (we need to come back for playlist data, also)
                youtubeVideo.setChannel(youtubeChannel);

                youtubeVideo.setDateCreated(parseTimestamp.apply(fieldValues[7]));
                youtubeVideo.setDateModified(parseTimestamp.apply(fieldValues[8]));

                // Hash Value:  This is the dual-purpose etag / Hash Code representation of the data. We must
                //              be consistent on how this is set.
                //
                youtubeVideo.setHashValue(YoutubeEntityHashCalculator.CreateHashValue(youtubeVideo));

                ApplicationHelpers.Log(LogMessage.Info("Youtube Video Extracted:  id=%1s, title=%2s",  youtubeVideo.getId(), youtubeVideo.getTitle()));

                result.add(youtubeVideo);
            }

            return result;
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error reading video list file", e));
            return null;
        }
    }

    private Path VerifyAndMoveVideoResult(Path executableTempDirectory) throws Exception
    {
        // Video Metadata:  Required to know the file was downloaded properly; and to get its extension.
        //
        var metadata = ParseVideoMetadata(executableTempDirectory);

        if (metadata == null)
        {
            ApplicationHelpers.Log(LogMessage.Error("Cannot find or parse video download metadata. This is required to move the file to the data directory"));
            return null;
        }

        // Get the resulting video file extension from the returned metadata
        var videoFileName = metadata.id + "." + metadata.ext;
        var videoFile = Path.of(executableTempDirectory.toString(), videoFileName);

        // Failure: Missing file(s)
        if (!Files.exists(videoFile))
        {
            ApplicationHelpers.Log(LogMessage.Error("Missing video metadata file (from yt-dlp output directory):  " + videoFile.toString()));
            return null;
        }

        // Move Video File:  bin -> Youtube Data Directory (user)
        //
        var youtubeDirectory = ApplicationHelpers.ResolvePath(this.configuration.youtubeDataDirectory);
        var destinationFile = Path.of(youtubeDirectory.toString(), videoFileName);

        return Files.move(videoFile, destinationFile , StandardCopyOption.REPLACE_EXISTING);
    }
    private List<Subtitles> LoadSubtitleList(Path executableTempDirectory) throws Exception
    {
        // Subtitles File:  Should be the only .txt file in the temp directory
        //
        var subtitlesFileOptional = Files.list(executableTempDirectory)
                .filter(path -> path.toString().endsWith(".txt"))
                .findFirst();

        var subtitlesFile = subtitlesFileOptional.get();

        try(var fileBuffer = new BufferedReader(new FileReader(subtitlesFile.toString())))
        {
            // Subtitles:  Language (tab) Name (tab) Format(s) (csv)
            //
            String line = "";
            int counter = 0;

            var result = new ArrayList<Subtitles>();

            while ((line = fileBuffer.readLine()) != null)
            {
                // The fields themselves are usually without any white space. We'll throw an exception if
                // this is off at all.
                var fields = Arrays.stream(StringUtility.Split(line, " ", EnumSet.of(StringSplitOptions.TrimEnds, StringSplitOptions.RemoveEmptyEntries)))
                                   .filter(current -> !current.isBlank())
                                   .map(Object::toString)
                                   .map(String::trim)
                                   .toArray();

                if (fields.length != 3)
                    throw new Exception("Invalid subtitles file:  fields expected (Language, Name, Formats) (probably a white space issue)");

                if (counter++ == 0)
                    continue;

                else
                {
                    var subtitles = new Subtitles();

                    subtitles.languageAbbr = fields[0].toString();
                    subtitles.languageName = fields[1].toString();
                    subtitles.formats = Arrays.stream(fields[2]         // vbr,json3, ... (with no white spaces)
                                              .toString()
                                              .split(","))
                                              .toList();

                    result.add(subtitles);
                }
            }

            return result;
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error reading .description file (from yt-dlp output directory)"));
            return null;
        }
    }

    private List<Comments> LoadCommentsFromMetadata(Path executableTempDirectory) throws Exception
    {
        var metadata = ParseVideoMetadata(executableTempDirectory);

        if (metadata != null)
            return metadata.comments;

        else
        {
            ApplicationHelpers.Log(LogMessage.Error("Error parsing yt-dlp metadata file (for comments list)"));
            return null;
        }
    }

    private String LoadVideoDescription(Path executableTempDirectory) throws Exception
    {
        var descriptionFileOptional = Files.list(executableTempDirectory)
                .filter(path -> path.toString().endsWith(".description"))
                .findFirst();

        var descriptionFile = descriptionFileOptional.get();

        // Failure: Missing file(s)
        if (descriptionFile.toString().isEmpty())
        {
            ApplicationHelpers.Log(LogMessage.Error("Missing video info json file (from yt-dlp output directory)"));
            return null;
        }

        try(var fileBuffer = new FileReader(descriptionFile.toString()))
        {
            // Read Description
            return fileBuffer.readAllAsString();
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error reading .description file (from yt-dlp output directory)"));
            throw e;
        }
    }

    private YoutubeDownloaderMetadata ParseVideoMetadata(Path executableTempDirectory) throws Exception
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

    //endregion
}
