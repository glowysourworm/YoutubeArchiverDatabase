package YoutubeArchiverDatabase.Utility.UtilityModel;

import com.fasterxml.jackson.annotation.JsonCreator;

/// This class represents a small portion of the yt-dlp video "info" - which I'm renaming meta-data for
/// this application's purposes. See a result using the --write-info-json option for yt-dlp.
public class YoutubeVideoDownloaderMetadata
{
    // Note*** All fields are assumed to be about the video unless otherwise specified

    public String id;
    public String channel;
    public String title;
    public String thumbnail;
    public boolean is_live;
    public boolean was_live;
    public long filesize_approx;
    public int comment_count;
    public int like_count;
    public int channel_follower_count;
    public boolean channel_is_verified;

    /// URL of the video (the same as the watch URL)
    public String webpage_url;

    /// Resolution width of the video
    public int width;

    /// Resolution height of the video
    public int height;

    /// Frames per second of the video
    public int fps;

    /// Local description of the video (i.e. not the official description - which is put into another file)
    public String description;
    public String channel_id;
    public String channel_url;

    /// Duration of the video in seconds
    public int duration;

    public int view_count;

    /// Extension of the -f setting
    public String video_ext;

    /// Extension set by yt-dlp (seems to be the one preferred by the downloader)
    public String ext;

    /// Release timestamp of the video
    public long release_timestamp;

    /// Uploader of the video (channel owner name / title)
    public String uploader;

    /// ID of user that uploaded the video (channel owner)
    public String uploader_id;

    public long upload_date;

    @JsonCreator
    public YoutubeVideoDownloaderMetadata()
    {

    }
}
