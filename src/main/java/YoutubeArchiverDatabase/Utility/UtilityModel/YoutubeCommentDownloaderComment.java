package YoutubeArchiverDatabase.Utility.UtilityModel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/// The comment structure for the youtube-comment-downloader
public class YoutubeCommentDownloaderComment
{
    // Use this ID to create the tree of comments
    @JsonIgnore
    public String parentCid;

    // The cid of the comment has a format for threads:  {rootId}.{replyId}.{subReplyId}... and so on.
    public String cid;
    public String text;
    public String time;

    // @{username}
    public String author;

    // This is the channel ID of the user that replied:  http://youtube.com/channel/{channelId}
    public String channel;
    public int votes;
    public int replies;

    // User's photo URL
    public String photo;
    public boolean heart;
    public boolean reply;
    public double time_parsed;

    @JsonCreator
    public YoutubeCommentDownloaderComment()
    {
        this.parentCid = "";
        this.cid = "";
        this.text = "";
        this.time = "";
        this.author = "";
        this.channel = "";
        this.votes = 0;
        this.replies = 0;
        this.photo = "";
        this.heart = false;
        this.reply = false;
        this.time_parsed = 0;
    }
}