package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "youtubevideoextension", schema = "youtubearchiver")
public class YoutubeVideoExtension extends YoutubeArchiverEntityBase
{
    @Id
    @Column(name = "videoId", nullable = false, length = 100)
    private String videoId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "videoId", nullable = false)
    private YoutubeVideo youtubevideo;

    @Column(name = "duration")
    private LocalTime duration;

    @Column(name = "viewCount")
    private Integer viewCount;

    @Column(name = "likeCount")
    private Integer likeCount;

    @Column(name = "commentCount")
    private Integer commentCount;

    @Column(name = "isLive")
    private Boolean isLive;

    @Column(name = "wasLive")
    private Boolean wasLive;

    @Column(name = "isDownloaded")
    private Boolean isDownloaded;

    @Column(name = "isAudioOnlyDownload")
    private Boolean isAudioOnlyDownload;

    @Column(name = "formatExtension", length = 30)
    private String formatExtension;

    @Column(name = "fullTitle", length = 300)
    private String fullTitle;

    @Column(name = "fileName", length = 300)
    private String fileName;

    @Column(name = "description", length = 10000)
    private String description;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public YoutubeVideo getYoutubevideo() {
        return youtubevideo;
    }

    public void setYoutubevideo(YoutubeVideo youtubevideo) {
        this.youtubevideo = youtubevideo;
    }

    public LocalTime getDuration() {
        return duration;
    }

    public void setDuration(LocalTime duration) {
        this.duration = duration;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Boolean getIsLive() {
        return isLive;
    }

    public void setIsLive(Boolean isLive) {
        this.isLive = isLive;
    }

    public Boolean getWasLive() {
        return wasLive;
    }

    public void setWasLive(Boolean wasLive) {
        this.wasLive = wasLive;
    }

    public Boolean getIsDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(Boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public Boolean getIsAudioOnlyDownload() {
        return isAudioOnlyDownload;
    }

    public void setIsAudioOnlyDownload(Boolean isAudioOnlyDownload) {
        this.isAudioOnlyDownload = isAudioOnlyDownload;
    }

    public String getFormatExtension() {
        return formatExtension;
    }

    public void setFormatExtension(String formatExtension) {
        this.formatExtension = formatExtension;
    }

    public String getFullTitle() {
        return fullTitle;
    }

    public void setFullTitle(String fullTitle) {
        this.fullTitle = fullTitle;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String GetName()
    {
        return this.getVideoId();
    }
}