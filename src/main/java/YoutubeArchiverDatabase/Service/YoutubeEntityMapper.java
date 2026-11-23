package YoutubeArchiverDatabase.Service;

import YoutubeArchiverDatabase.Model.YoutubeChannel;
import YoutubeArchiverDatabase.Model.YoutubeVideo;
import YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader.YoutubeDownloaderMetadata;

import java.time.Instant;

/// Maps types to <-> from YoutubeArchiverDatabase entities
public class YoutubeEntityMapper
{
    /// Creates channel based on minimum requirements for putting it into the database
    /// @param channelId String ID of the channel (SHOULD NOT BE THE @Channel ID)
    /// @param apiCreated Created using the Google API
    /// @return A local entity for storage
    public static YoutubeChannel CreateNew(String channelId, boolean apiCreated) throws IllegalArgumentException
    {
        if (channelId == null || channelId.isEmpty())
            throw new IllegalArgumentException("channelId cannot be null or empty");

        var result = new YoutubeChannel();

        result.setId(channelId);
        result.setApiCreated(apiCreated);
        result.setDateCreated(Instant.now());

        // HASH VALUE:  This must be set last using the calculator!
        result.setHashValue(YoutubeEntityHashCalculator.CreateHashValue(result));

        return result;
    }

    /// Creates a YoutubeVideo entity from the yt-dlp metadata file (.info.json) contents
    /// @param channel Valid YoutubeChannel entity (for storage)
    /// @param metadata Metadata entity containing the contents of the result file
    /// @return A local entity for storage
    public static YoutubeVideo Map(YoutubeChannel channel, YoutubeDownloaderMetadata metadata)
    {
        if (metadata.id == null || metadata.id.isEmpty())
            throw new IllegalArgumentException("channelId cannot be null or empty");

        var result = new YoutubeVideo();

        result.setId(metadata.id);
        result.setDateCreated(Instant.now());
        result.setChannel(channel);
        result.setApiCreated(false);
        result.setImageURL(metadata.thumbnail);
        result.setTitle(metadata.title);
        result.setDescription(metadata.description);

        // HASH VALUE:  This must be set last using the calculator!
        result.setHashValue(YoutubeEntityHashCalculator.CreateHashValue(result));

        return result;
    }
}
