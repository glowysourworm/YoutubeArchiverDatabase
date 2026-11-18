package YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;

import java.util.List;

/// This will represent the command:  yt-dlp --list-subs
/// which will print a tab-separated list to a file:  Language Name Formats (comma-separated list)
public class Subtitles
{
    // ab, aa, af, ak, ...
    public String languageAbbr;

    /// Abkhazian, Afar, Afrikaans, Akan, ...
    public String languageName;

    /// vtt, srt, ttml, srv3, srv2, srv1, json3, ...
    public List<String> formats;
}
