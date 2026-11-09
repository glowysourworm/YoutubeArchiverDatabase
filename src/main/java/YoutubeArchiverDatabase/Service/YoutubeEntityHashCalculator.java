package YoutubeArchiverDatabase.Service;

import YoutubeArchiverDatabase.Model.YoutubeChannel;
import YoutubeArchiverDatabase.Model.YoutubeComment;
import YoutubeArchiverDatabase.Model.YoutubeUser;
import YoutubeArchiverDatabase.Model.YoutubeVideo;

import java.util.Objects;

/// Class for calculating hash values based on the entity data that we care about
/// for showing "if the entity has changed". This will be separated from the hash
/// code calculations for the entities themselves so as not to mix up how we're
/// getting and setting the "hashValue" (which relates to the etag).
public final class YoutubeEntityHashCalculator
{

    /// Creates the hash value for the entity YoutubeChannel (takes into account the logic of how the entity was created)
    /// @param channel The channel which is to be directly persisted
    /// @return Calculated hash value
    public static String CreateHashValue(YoutubeChannel channel)
    {
        if (channel.getApiCreated())
        {
            return channel.getHashValue();
        }
        else
        {
            return String.valueOf(Objects.hash(channel.getDescription(),
                                    channel.getImageURL(),
                                    channel.getName()));
        }
    }

    /// Creates the hash value for the entity YoutubeComment (takes into account the logic of how the entity was created)
    /// @param comment The comment which is to be directly persisted
    /// @return Calculated hash value
    public static String CreateHashValue(YoutubeComment comment)
    {
        if (comment.getApiCreated())
        {
            return comment.getHashValue();
        }
        else
        {
            return String.valueOf(Objects.hash(comment.getText()));
        }
    }

    /// Creates the hash value for the entity YoutubeUser (takes into account the logic of how the entity was created)
    /// @param user The user which is to be directly persisted
    /// @return Calculated hash value
    public static String CreateHashValue(YoutubeUser user)
    {
        if (user.getApiCreated())
        {
            return user.getHashValue();
        }
        else
        {
            return String.valueOf(Objects.hash(user.getName()));
        }
    }

    /// Creates the hash value for the entity YoutubeVideo (takes into account the logic of how the entity was created)
    /// @param video The video which is to be directly persisted
    /// @return Calculated hash value
    public static String CreateHashValue(YoutubeVideo video)
    {
        if (video.getApiCreated())
        {
            return video.getHashValue();
        }
        else
        {
            return String.valueOf(Objects.hash(video.getDescription(), video.getImageURL(), video.getTitle()));
        }
    }
}
