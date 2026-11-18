package YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HttpHeaders
{
    @JsonProperty("User-Agent")
    public String userAgent;

    @JsonProperty("Accept")
    public String accept;

    @JsonProperty("Accept-Language")
    public String acceptLanguage;

    @JsonProperty("Sec-Fetch-Mode")
    public String secFetchMode;
}
