package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Model.YoutubeComment;
import YoutubeArchiverDatabase.Service.YoutubeDatabaseConnection;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeCommentDownloaderData;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/// ShellRunner implementation for the youtube-comment-downloader python application
/// [...](https://github.com/egbertbouman/youtube-comment-downloader)
public class YoutubeCommentRunner extends ShellRunner
{
    private static final String YOUTUBE_COMMENT_DOWNLOADER_YOUTUBEID_OPT = "--youtubeid";
    private static final String YOUTUBE_COMMENT_DOWNLOADER_FORMAT_OPT = "--format";
    private static final String YOUTUBE_COMMENT_DOWNLOADER_OUTPUT_OPT = "--output";
    private static final String YOUTUBE_COMMENT_DOWNLOADER_FORMAT_JSON = "json";

    private static final String EXECUTABLE_NAME = "youtube-comment-downloader";

    // Application Configuration
    private final Configuration configuration;

    /// File that stores the resulting comments
    private Path resultFile;
    private ArrayList<YoutubeComment> results;
    private String videoId;
    private String channelId;

    /// Initializes the YoutubeCommentRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    public YoutubeCommentRunner(final Configuration configuration)
    {
        super(EXECUTABLE_NAME);

        this.configuration = configuration;
        this.resultFile = null;
        this.videoId = null;
        this.channelId = null;
        this.results = new ArrayList<>();
    }

    public List<YoutubeComment> Run(String channelId, String videoId) throws IllegalArgumentException
    {
        if (channelId == null || channelId.isBlank())
            throw new IllegalArgumentException("Missing or invalid channel id");

        if (videoId == null || videoId.isBlank())
            throw new IllegalArgumentException("Missing or invalid channel id");

        // Procedure:
        //
        // 1) Initialize
        // 2) Edit our command line
        // 3) Run -> (pre / post) run
        // 4) Reset (cleanup temp directory)
        // 5) Read Comment File

        this.results = new ArrayList<>();
        this.videoId = videoId;
        this.channelId = channelId;

        // Initialize
        if (!super.Initialize())
        {
            ApplicationHelpers.Log(LogMessage.Error("Error initializing youtube-comment-downloader:  process halted"));
            return this.results;
        }

        // Result File
        if (!CreateResultFile(super.GetExecutableTempDirectory()))
        {
            ApplicationHelpers.Log(LogMessage.Error("Error initializing youtube-comment-downloader:  process halted"));
            return this.results;
        }

        // Process Command Line -> (results created locally)
        var result = super.Run(YOUTUBE_COMMENT_DOWNLOADER_YOUTUBEID_OPT, String.valueOf(videoId),
                               YOUTUBE_COMMENT_DOWNLOADER_FORMAT_OPT, YOUTUBE_COMMENT_DOWNLOADER_FORMAT_JSON,
                               YOUTUBE_COMMENT_DOWNLOADER_OUTPUT_OPT, this.resultFile.toString());

        // Run Error
        if (!result)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error running youtube-comment-downloader:  process halted"));
            return this.results;
        }

        // Reset (cleanup)
        try
        {
            super.Reset();

            this.channelId = null;
            this.videoId = null;
            this.resultFile = null;
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error deleting temp directory for youtube-comment-downloader:  process halted", e));
            return this.results;
        }

        // Results:  Read / Parse comments completed on post run
        return this.results;
    }

    private boolean CreateResultFile(Path executableTempDirectory)
    {
        try
        {
            // This will go in our bin directory; and will be deleted.
            //
            this.resultFile = Path.of(executableTempDirectory.toString(),"tmpFile.json");

            return true;
        }
        catch (IllegalArgumentException e)
        {
            ApplicationHelpers.Log(LogMessage.Error(String.format("Error creating file for shell application %1s", EXECUTABLE_NAME), e));
            return false;
        }
    }


    @Override
    protected boolean ExecutePrerun(Path executableTempDirectory)
    {
        // We needed to add the output file to our command line
        return true;
    }

    @Override
    protected boolean ExecutePostrun(Path executableTempDirectory, int exitCode)
    {
        if (exitCode != 0)
            return false;

        // Clear List
        this.results = new ArrayList<>();

        // Check that comments file was created. There may not have been any comments - in which
        // case the youtube-comment-downloader does not create a file
        if (!Files.exists(this.resultFile))
        {
            return true;
        }

        // Post Run Callback:  Parse the JSON output
        //
        try (var fileBuffer = new FileReader(this.resultFile.toString()))
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
                var commentId = new StringBuilder();
                var parentId = new StringBuilder();

                if (!ParseCommentIds(data.cid, parentId, commentId))
                {
                    // Log
                    continue;
                }

                // These entities usually need to be patched up before sending the comment
                // to the database itself. This is for getting the initial comment data in
                // a format for our application.
                //
                var user = database.GetUser(data.author);
                var video = database.GetVideo(this.videoId);
                var channel = database.GetChannel(this.channelId);
                var parent = parentId.isEmpty() ? null : database.GetComment(parentId.toString());

                var comment = new YoutubeComment();

                // Primary Key
                //
                comment.setId(commentId.toString());

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

                this.results.add(comment);
            }

            ApplicationHelpers.Log(LogMessage.Info(String.format("Comments Read by %1s:  %2d", EXECUTABLE_NAME, this.results.size())));
            return true;
        }
        catch (IOException e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error accessing Youtube Archiver database", e));
        }

        return false;
    }

    /// Parses the commentId, and parentId, out of the comment structure:  {tree}.{of}.{comment}.{ids}.{parentId}.{commentId}
    /// @param cid The entire string of id data
    /// @param parentId The parent comment's id
    /// @param commentId The comment's id
    /// @return success / failure
    private boolean ParseCommentIds(String cid, StringBuilder parentId, StringBuilder commentId)
    {
        var threadIDParts = cid.split("\\.");

        // Next-to-Last ID
        if (threadIDParts.length > 1)
            parentId.append(threadIDParts[threadIDParts.length - 2].trim());

        // Last ID
        commentId.append(threadIDParts[threadIDParts.length - 1].trim());

        // ParentId is allowed to be null (root comment of the thread). The comment ID must be present in the
        // cid value.
        //
        return !commentId.isEmpty();
    }
}
