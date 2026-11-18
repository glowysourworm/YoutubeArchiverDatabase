package YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Comments
{
    public String id;
    public String parent;
    public String text;

    @JsonProperty("like_count")
    public Integer likeCount;

    @JsonProperty("author_id")
    public String authorId;
    public String author;

    @JsonProperty("author_thumbnail")
    public String authorThumbnail;

    @JsonProperty("author_is_uploader")
    public Boolean authorIsUploader;

    @JsonProperty("author_is_verified")
    public Boolean authorIsVerified;

    @JsonProperty("is_favorited")
    public Boolean isFavorited;

    @JsonProperty("_time_text")
    public String timeText;
    public Integer timestamp;

    @JsonProperty("is_pinned")
    public Boolean isPinned;
}
