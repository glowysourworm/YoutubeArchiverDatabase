package YoutubeArchiverDatabase.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "youtubecomment", schema = "youtubearchiver")
public class YoutubeComment {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "parentId", nullable = false)
    private Long parentId;

    @Column(name = "channelId", nullable = false)
    private Long channelId;

    @Column(name = "videoId", nullable = false)
    private Long videoId;

    @Column(name = "comment", length = 10000)
    private String comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}