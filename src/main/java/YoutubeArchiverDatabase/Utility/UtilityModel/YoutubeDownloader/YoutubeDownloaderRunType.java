package YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;

public enum YoutubeDownloaderRunType
{
    VideoMetadata(0),
    VideoComments(1),
    VideoDescription(2),
    VideoSubtitleList(3),
    VideoDownload(4),
    VideoAudioDownload(5),
    ChannelVideoList(6);

    private int value;

    YoutubeDownloaderRunType(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return this.value;
    }

    /// Returns description of the enum value
    /// @return The string description
    public String getDescription() throws UnsupportedOperationException
    {
        switch (this.value)
        {
            case 0: return "Video Metadata";
            case 1: return "Video Comments";
            case 2: return "Video Description";
            case 3: return "Video Subtitle List";
            case 4: return "Video Download";
            case 5: return "Audio Download (only)";
            case 6: return "Channel Video List";
            default:
                throw new UnsupportedOperationException("Unhandled YoutubeDownloaderRunType value");
        }
    }
}
