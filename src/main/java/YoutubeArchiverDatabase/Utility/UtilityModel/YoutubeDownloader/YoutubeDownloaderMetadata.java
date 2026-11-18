package YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class YoutubeDownloaderMetadata
{
    public String id;
    public String title;
    public List<Formats> formats;
    public List<Thumbnails> thumbnails;
    public String thumbnail;
    public String description;

    @JsonProperty("channel_id")
    public String channelId;

    @JsonProperty("channel_url")
    public String channelUrl;
    public Integer duration;

    @JsonProperty("view_count")
    public Integer viewCount;

    @JsonProperty("age_limit")
    public Integer ageLimit;

    @JsonProperty("webpage_url")
    public String webpageUrl;

    public List<String> categories;
    public List<String> tags;

    @JsonProperty("playable_in_embed")
    public Boolean playableInEmbed;

    @JsonProperty("live_status")
    public String liveStatus;

    @JsonProperty("media_type")
    public String mediaType;

    @JsonProperty("_format_sort_fields")
    public List<String> formatSortFields;

    @JsonProperty("automatic_captions")
    public AutomaticCaptions automaticCaptions;
    public Subtitles subtitles;

    @JsonProperty("comment_count")
    public Integer commentCount;
    public List<Heatmap> heatmap;

    @JsonProperty("like_count")
    public Integer likeCount;
    public String channel;

    @JsonProperty("channel_follower_count")
    public Integer channelFollowerCount;

    @JsonProperty("channel_is_verified")
    public Boolean channelIsVerified;
    public String uploader;

    @JsonProperty("uploader_id")
    public String uploaderId;

    @JsonProperty("uploader_url")
    public String uploaderUrl;

    @JsonProperty("upload_date")
    public String uploadDate;
    public Integer timestamp;
    public String availability;

    @JsonProperty("webpage_url_basename")
    public String webpageUrlBasename;

    @JsonProperty("webpage_url_domain")
    public String webpageUrlDomain;
    public String extractor;

    @JsonProperty("extractor_key")
    public String extractorKey;

    @JsonProperty("display_id")
    public String displayId;
    public String fulltitle;

    @JsonProperty("duration_string")
    public String durationString;

    @JsonProperty("is_live")
    public Boolean isLive;

    @JsonProperty("was_live")
    public Boolean wasLive;
    public Integer epoch;
    public List<Comments> comments;
    public String format;

    @JsonProperty("format_id")
    public String formatId;
    public String ext;
    public String protocol;
    public String language;

    @JsonProperty("format_note")
    public String formatNote;

    @JsonProperty("filesize_approx")
    public Integer filesizeApprox;

    // Average bitrate of audio and video in Kbps
    public Double tbr;
    public Integer width;
    public Integer height;
    public String resolution;
    public Integer fps;

    @JsonProperty("dynamic_range")
    public String dynamicRange;

    /// Name of the video codec in use
    public String vcodec;

    /// Average video bitrate in Kbps
    public Double vbr;

    @JsonProperty("aspect_ratio")
    public Double aspectRatio;

    /// Name of the audio codec in use
    public String acodec;

    /// Average audio bitrate in Kbps
    public Double abr;

    /// Audio sampling rate in Hertz
    public Integer asr;

    @JsonProperty("audio_channels")
    public Integer audioChannels;

    @JsonProperty("_type")
    public String type;

    @JsonProperty("_version")
    public Version version;
}
