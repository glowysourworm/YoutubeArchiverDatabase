package YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Version
{
    public String version;

    @JsonProperty("release_git_head")
    public String releaseGitHead;

    public String repository;
}
