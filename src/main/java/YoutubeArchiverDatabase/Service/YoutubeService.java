package YoutubeArchiverDatabase.Service;


import YoutubeArchiverDatabase.Model.YoutubeComment;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YoutubeService
{
    private static String YOUTUBE_API_KEY = "";
    private final static String YOUTUBE_API_LIST_API_OPTIONS = "snippet,replies";
    private YouTube service;

    public YoutubeService()
    {
    }

    public void Initialize()
    {
        try
        {
            var jsonBuilder = new GsonFactory();
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            service = new YouTube.Builder(httpTransport, jsonBuilder, null)
                                 .setApplicationName("YoutubeArchiver")
                                 .build();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public List<YoutubeComment> GetCommentThreads(String videoId)
    {
        try
        {
            var response = service.commentThreads()
                    .list(Collections.singletonList(YOUTUBE_API_LIST_API_OPTIONS))
                    .setKey(YOUTUBE_API_KEY)
                    .setVideoId(videoId)
                    .execute();

            var result = new ArrayList<YoutubeComment>();

            for (var commentData : response.getItems())
            {
                var comment = new YoutubeComment();

                comment.setId(commentData.getId());

                result.add(comment);
            }

            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public long GetQuota()
    {
        // This can apparently be accomplished using the Google cloud monitoring API.. (TODO)

        return 0;
    }
}
