package YoutubeArchiverDatabase.Utility;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.Subtitles;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderRunType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class YoutubeVideoSubtitlesRunner extends YoutubeDownloaderRunner<List<Subtitles>, String>
{
    private static final String EXECUTE_VIDEO_SUBTITLE_LIST_TEMPLATE = "--js-runtimes node --cookies-from-browser firefox --remote-components ejs:github " +
                                                                        "--skip-download --print-to-file automatic_captions_tables %(id)s.txt {videoId}";

    private String videoId;

    /// Initializes the YoutubeCommentRunner with a working temp directory and shell
    /// for the current platform:  (see ApplicationHelpers for any platform-specific methods)
    ///
    /// @param configuration Application Configuration
    ///
    public YoutubeVideoSubtitlesRunner(Configuration configuration)
    {
        super(YoutubeDownloaderRunType.VideoSubtitleList, configuration);
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
    protected List<Subtitles> ProvideResult(Path executableTempDirectory, int exitCode)
    {
        Path subtitlesFile;

        try
        {
            // Subtitles File:  Should be the only .txt file in the temp directory
            //
            var subtitlesFileOptional = Files.list(executableTempDirectory)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .findFirst();

            subtitlesFile = subtitlesFileOptional.get();
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error reading subtitles file", e));
            return null;
        }

        try(var fileBuffer = new BufferedReader(new FileReader(subtitlesFile.toString())))
        {
            // Subtitles:  Language (tab) Name (tab) Format(s) (csv)
            //
            String line = "";
            int counter = 0;

            var result = new ArrayList<Subtitles>();

            while ((line = fileBuffer.readLine()) != null)
            {
                // The fields themselves are usually without any white space
                //
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
        return EXECUTE_VIDEO_SUBTITLE_LIST_TEMPLATE.replace("{videoId}", videoId);
    }
}
