package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderRunType;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class YoutubeChannelVideoListRunner extends YoutubeDownloaderRunner<List<String>, String>
{
    private static final String EXECUTE_VIDEO_LIST_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                              "--flat-playlist --skip-download --print-to-file {videoFields} {videoListFileName} {channelId}";

    // Name for the channel video list
    private static final String VIDEO_LIST_FILE_NAME= "channel-video-list.txt";

    // This is a comma-separated list of video fields we use for YoutubeVideo
    private static final String VIDEO_METADATA_FIELDS = "id";

    // JSON types:  s = string, d = number, b = boolean
    private static final String VIDEO_METADATA_FIELD_TYPES = "s";

    // Youtube ID for the channel (URL also works for the downloader)
    private String channelId;

    public YoutubeChannelVideoListRunner(final Configuration configuration)
    {
        super(YoutubeDownloaderRunType.ChannelVideoList, configuration);
    }

    @Override
    public void Initialize(String initializationData)
    {
        this.channelId = initializationData;
    }

    @Override
    public void ReInitialize(String initializationData)
    {
        this.channelId = initializationData;
    }

    @Override
    public boolean IsInitialized()
    {
        return this.channelId != null;
    }

    /// Exposed function to allow derived classes to parse and return the result after
    /// run has complete.
    ///
    /// @param executableTempDirectory Temp directory for the run data
    /// @param exitCode                Exit code of the application
    /// @return The result of the shell operation
    @Override
    protected List<String> ProvideResult(Path executableTempDirectory, int exitCode)
    {
        try
        {
            return GetVideoListResult(executableTempDirectory);
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error reading channel video list file", e));
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
        return EXECUTE_VIDEO_LIST_TEMPLATE.replace("{videoFields}", finalVideoFields)
                                          .replace("{videoListFileName}", VIDEO_LIST_FILE_NAME)
                                          .replace("{channelId}", channelId);
    }

    private List<String> GetVideoListResult(Path executableTempDirectory) throws Exception
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

        var result = new ArrayList<String>();

        // Filter yt-dlp video field results
        Function<String, String> fieldStringFilter = (fieldValue ->
        {
            // NA => no result (we're going to use empty strings)
            if (fieldValue == null || fieldValue.equals("NA"))
                return "";

            return fieldValue;
        });

        try (var fileReader = new FileReader(videoListFile.toString()))
        {
            // Read Video Metadata
            var fileContents = fileReader.readAllAsString();

            fileReader.close();

            // Separate lines in the result file
            var entities = StringUtility.Split(fileContents,"\r\n", StringSplitOptions.RemoveEmptyEntries);

            // Use Video Fields (above):  Read off the field values from the pseudo-json
            //
            for (var entity : entities)
            {
                // Fields:  field1Value,field2Value,... (this was set up for multiple fields; but the print function didn't
                //          work properly.. The metadata file will have them.
                //
                var fieldValues = StringUtility.Split(entity, ",");

                if (fieldValues.length != 1)
                    continue;

                ApplicationHelpers.Log(LogMessage.Info("Youtube Video Extracted:  id=%1s",  fieldValues[0]));

                result.add(fieldValues[0].trim());
            }

            return result;
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error reading video list file", e));
            return null;
        }
    }
}
