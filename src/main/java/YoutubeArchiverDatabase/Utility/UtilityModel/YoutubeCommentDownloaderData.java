package YoutubeArchiverDatabase.Utility.UtilityModel;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;

// This follows the result data structure for the (--output "json") option for the youtube-comment-downloader
public class YoutubeCommentDownloaderData
{
    public ArrayList<YoutubeCommentDownloaderComment> comments;

    @JsonCreator
    public YoutubeCommentDownloaderData()
    {
        this.comments = new ArrayList<>();
    }
}