package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Model.YoutubeComment;
import YoutubeArchiverDatabase.Model.YoutubeVideo;
import YoutubeArchiverDatabase.Service.YoutubeDatabaseConnection;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeCommentDownloaderData;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeVideoDownloaderMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.cfg.Environment;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/// This class is intended to run dedicated .py scripts that have specific outputs:  youtube-comment-downloader;
/// extract-youtube-comments.py; and so on. The return values will be the YoutubeComment collections. These scripts
/// must already be installed using any python methods described in the readme.
public class PythonRunner
{

    /*
    * Python Runner Applications: The application list, configuration, and so on, can go on and on.. for
    *                             getting the publicly available data downloaded and stored. We're going
    *                             with a first-try using:  youtube-comment-downloader, yt-dlp.
    *
    * Youtube Comment Downloader: https://github.com/egbertbouman/youtube-comment-downloader
    * Youtube Downloader Project: https://github.com/yt-dlp/yt-dlp
    *
    * */

    private static final String YOUTUBE_COMMENT_DOWNLOADER = "youtube-comment-downloader";
    private static final String YOUTUBE_COMMENT_DOWNLOADER_YOUTUBEID_OPT = "--youtubeid";
    private static final String YOUTUBE_COMMENT_DOWNLOADER_FORMAT_OPT = "--format";
    private static final String YOUTUBE_COMMENT_DOWNLOADER_OUTPUT_OPT = "--output";
    private static final String YOUTUBE_COMMENT_DOWNLOADER_FORMAT_JSON = "json";

    private static final String YOUTUBE_VIDEO_DOWNLOADER = "yt-dlp";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_WRITE_METADATA_OPT = "--write-info-json";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_SKIP_VIDEO_OPT = "--skip-video";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_FORMAT_OPT = "-f";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_FORMAT_DEFAULT = "best";
    // private static final String YOUBUTE_VIDEO_DOWNLOADER_NO_MERGE_OPT = "--no-video-multistreams";
    private static final String YOUTUBE_VIDEO_DOWNLOADER_APPLICATION_OPT = "--js-runtimes node --remote-components ejs:github";

    private final Configuration configuration;

    public PythonRunner(Configuration config)
    {
        this.configuration = config;
    }

    /// Using an external process shell:  This method runs .py scripts to handle getting public YoutubeComment
    /// instances from an external application; and, thus, will not charge any user quota data to the user's
    /// Google API account.
    /// @param videoId The id of the video from which to get all comment threads.
    /// @param channelId The id of the channel the video belongs to.
    /// @return List of YoutubeComment instances that will arrange themselves as a tree in the database.
    public List<YoutubeComment> ExtractYoutubeComments(String channelId, String videoId)
    {
        // This will go in our bin directory; and will be deleted.
        //
        var tmpFile = "tmpFile.json";

        // Output of serializer
        var result = new ArrayList<YoutubeComment>();

        // Run Extraction Process:  Output is a json file in
        //
        var runResult = RunProcess((exitCode, directory) ->
        {
            if (exitCode != 0)
                return;

            // Post Run Callback:  Parse the JSON output
            //
            try (var fileBuffer = new FileReader(tmpFile))
            {
                // We're going to need a database connection
                var database = new YoutubeDatabaseConnection(this.configuration);

                // Read entire file!  @_@ (Language Level had to be upped...) (25)
                var fileContents = fileBuffer.readAllAsString();

                // Jackson:  Java <-> JSON API (premier Java JSON API; using tools v3.x.x)
                var mapper = new ObjectMapper();

                // no...THIS one will work.
                mapper = mapper.rebuild()
                        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                        .build();

                // Deserialize
                var commentData = mapper.readValue(fileContents, YoutubeCommentDownloaderData.class);

                // Map result to our database format
                for (var data : commentData.comments)
                {
                    var commentId = "";
                    var parentId = "";

                    if (!ParseCommentIds(data.cid, parentId, commentId))
                    {
                        // Log
                        continue;
                    }

                    var user = database.GetUser(data.author);
                    var video = database.GetVideo(videoId);
                    var channel = database.GetChannel(channelId);
                    var parent = parentId.isEmpty() ? null : database.GetComment(parentId);

                    var comment = new YoutubeComment();

                    // Primary Key
                    //
                    comment.setId(data.cid);

                    // Foreign Keys (these are mandatory. If they're not available, then we need to create placeholders)
                    //
                    comment.setUser(user);
                    comment.setVideo(video);
                    comment.setChannel(channel);
                    comment.setParent(parent);                      // Nullable Column (parentId)

                    // Comment Data:  Most fields are just for secretarial purposes. The modification of the comment
                    //                will be tracked; but the history only depends on the text being modified; or
                    //                the ETag value.
                    //
                    //                The AddUpdate method will create historical comments as needed.
                    //
                    comment.setApiCreated(false);                   // API not invoked for the python script
                    comment.setDateCreated(Instant.now());
                    comment.setDateModified(Instant.now());
                    comment.setText(data.text);                     // for the comment data

                    // Hash Value: Must be set after the rest of the data. The ETag from Google would be used otherwise.
                    //
                    comment.setHashValue(null);                     // ETag is a google API field which is also a "hash value" fingerprint

                    result.add(comment);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        },
        // Process Command Line
        YOUTUBE_COMMENT_DOWNLOADER,
        YOUTUBE_COMMENT_DOWNLOADER_YOUTUBEID_OPT, String.valueOf(videoId),
        YOUTUBE_COMMENT_DOWNLOADER_FORMAT_OPT, YOUTUBE_COMMENT_DOWNLOADER_FORMAT_JSON,
        YOUTUBE_COMMENT_DOWNLOADER_OUTPUT_OPT, tmpFile);

        return result;
    }

    /// Parses the commentId, and parentId, out of the comment structure:  {tree}.{of}.{comment}.{ids}.{parentId}.{commentId}
    /// @param cid The entire string of id data
    /// @param parentId The parent comment's id
    /// @param commentId The comment's id
    /// @return success / failure
    private boolean ParseCommentIds(String cid, String parentId, String commentId)
    {
        // Split comment ID for comment thread
        var indexOfSeparator = 0;
        var localCid = cid;

        // "out" variables (no pass by reference for primitives, these are considered reference variables)
        parentId = "";
        commentId = "";

        do
        {
            indexOfSeparator = localCid.lastIndexOf('.');

            if (indexOfSeparator >= 0)
            {
                // Parent (thread) ID
                if (!localCid.isEmpty())
                {
                    // Comment ID should be the last
                    if (commentId.isEmpty())
                    {
                        commentId = localCid.substring(indexOfSeparator, localCid.length() - 1);
                    }

                    else if (parentId.isEmpty())
                    {
                        parentId = localCid.substring(indexOfSeparator, localCid.length() - 1);
                    }
                }

                // Update Working cid
                localCid = cid.substring(0, indexOfSeparator);
            }

        } while (indexOfSeparator >= 0);

        // ParentId is allowed to be null (root comment of the thread). The comment ID must be present in the
        // cid value.
        //
        return !commentId.isEmpty();
    }

    /// Extracts youtube video from publicly available online free data using yt-dlp (Python Pre-requisite). The
    /// default format for extraction is mp4; but the downloader application is very full-featured (see github).
    /// The result will be put into an application folder; and the user may view videos on their offline application.
    /// This extractor will return a large body of metadata needed for the archiver.
    /// @param videoId The ID of the video to extract
    /// @param includeVideo Option to include the video (in default format)
    /// @return The metadata associated with the download (if no metadata was included, then this will be null) or null
    ///         if the process failed.
    public YoutubeVideoDownloaderMetadata ExtractYoutubeVideo(String videoId, boolean includeVideo)
    {
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

        var result = new AtomicReference<YoutubeVideoDownloaderMetadata>();

        // PostRunCallback:  Code to utilize output files; (String...): Join Command Line
        var runResult = RunProcess((exitCode, directory) ->
        {
            if (exitCode != 0)
                return;

            try
            {
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
                    result.set(null);
                    return;
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
                var metadata = mapper.readValue(fileContents, YoutubeVideoDownloaderMetadata.class);

                result.set(metadata);

                // Video File (optional)
                if (includeVideo)
                {
                    var videoFileOptional = Files.list(directory)
                                                 .filter(path -> path.toString().endsWith("." + metadata.ext))
                                                 .findFirst();

                    var videoFile = videoFileOptional.get();

                    // Failure: Missing file(s)
                    if (videoFile.toString().isEmpty())
                    {
                        System.out.println("Missing video file (from yt-dlp output directory)");
                        result.set(null);
                        return;
                    }

                    // Move Video File:  bin -> Youtube Data Directory (user)
                    Files.move(videoFile, Path.of(this.configuration.youtubeDataDirectory, videoFile.getFileName().toString()));
                }
            }
            catch (Exception e)
            {
                // Log Error
                e.printStackTrace();

                // Set Failed Result
                result.set(null);
            }

        }, YOUTUBE_VIDEO_DOWNLOADER,
           commandLine.stream()
                      .reduce("", (seed, current) ->
        {
            if (seed.isEmpty())
                return current;

            return seed + " " + current;
        }));

        // Success
        if (runResult)
        {
            return result.get();
        }

        // Failure
        else
        {
            return null;
        }
    }

    /// Runs external process: with IO directed to our application's log; with run directory created
    /// and consumed here and by the caller's post-run callback; and cleaning up any run directory
    /// files or resources post-run. All code is run within a try / catch block.
    /// @param postRunCallback Callback performed after the run is complete; and before the directory + files are deleted.
    ///                        Arguments:  Exit Code, Working Directory
    /// @param executable The executable name of the command (please leave this off the arguments)
    /// @param arguments Command line arguments
    /// @return true / false based on success of the process run + any user processing code in the callback.
    private boolean RunProcess(BiConsumer<Integer, Path> postRunCallback, String executable, String...arguments)
    {
        if (executable == null || executable.isBlank())
            throw new IllegalArgumentException("Invalid process executable");

        // Portability:  Shell usage varies per issue...
        //
        // 1) Windows (IN SOME CASES):  "cmd.exe /c ..." must be called with absolute paths to command executables
        // 2) Linux (TODO)
        //
        var executablePath = GetExecutablePath(executable);

        if (executablePath == null)
            throw new IllegalArgumentException("Executable path not found:  " + executable);

        // Procedure:  It's easier to use the system shell to handle commands when Java is built to be portable.
        //             The issue (currently) is that the python startup scripts vary depending on the package.
        //
        // 0) Prepare Process Folder / Command Line[s]
        // 1) Start command shell (cmd.exe, bash.exe, ...) with resolved command line
        //

        // Current Working Directory
        var workingDirectory = ApplicationHelpers.GetWorkingDirectory();

        // Create Application Temp Directory
        var tempDirectory = ApplicationHelpers.ResolvePath(executable);

        // User's Command Line (with the shell command)
        var commandLine = Arrays.stream(arguments).reduce(executablePath.toString(), (seed, current) ->
        {
            return seed + " " + current;
        });

        // Typically this kind of constructor would throw some kind of exception.. but probably not
        // the way this class was built since Java's try-catch-with-resources was made for
        // that purpose.
        //
        var builder = new ProcessBuilder("cmd.exe", "/c", commandLine);

        try
        {
            // Create Process Folder:  This will default to the first argument of the command line (name of exe)
            //

            // Delete Existing Folder
            var directoryIssue = !ClearDirectory(tempDirectory.toString(), true);

            if (directoryIssue)
                return false;

            // Create (temp) Folder
            var directory = Files.createDirectory(tempDirectory);

            // Options:  Working Directory, Inherit IO (console)
            builder.directory(directory.toFile());
            builder.inheritIO();

            // -> Start
            var process = builder.start();
            int exitCode = process.waitFor();

            System.out.println("\nExited with error code : " + exitCode);

            // Close Process
            process.destroyForcibly();

            // User Callback
            postRunCallback.accept(exitCode, directory);

            // Clean Directory
            ClearDirectory(tempDirectory.toString(), true);

            return exitCode == 0;
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /// Deletes all files in a directory + (optionally) deletes the directory
    /// @param directory Directory to clear
    /// @param deleteDirectory Option to delete the actual directory
    /// @return true / false for success or failure
    private boolean ClearDirectory(String directory, boolean deleteDirectory)
    {
        try
        {
            var directoryPath = Path.of(directory);
            var result = new AtomicReference<Boolean>();

            // Delete Existing Folder
            if (Files.exists(directoryPath))
            {
                // Delete Files
                Files.walk(directoryPath)
                     .forEach(path ->
                     {
                         try
                         {
                             if (!Files.isDirectory(path))
                                Files.delete(path);
                         }
                         catch (Exception ex)
                         {
                             ex.printStackTrace();
                         }
                     });

                // Delete Directory
                Files.deleteIfExists(directoryPath);
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /// Searches $PATH for executable full path
    /// @param executable Name of executable
    /// @return Resolved path to executable
    private Path GetExecutablePath(String executable)
    {
        try
        {
            var paths = System.getenv("PATH").split(";");

            for (var path : paths)
            {
                // Executable Path
                var absolutePath = Path.of(path, executable + ".exe");

                if (Files.exists(absolutePath))
                {
                    return absolutePath;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
