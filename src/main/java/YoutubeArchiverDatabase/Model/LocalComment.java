package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "localcomment", schema = "youtubearchiver")
public class LocalComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private YoutubeUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channelId")
    private YoutubeChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videoId")
    private YoutubeVideo video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentId")
    private YoutubeComment comment;

    @Column(name = "dateCreated", nullable = false)
    private Instant dateCreated;

    @Column(name = "dateModified", nullable = false)
    private Instant dateModified;

    @Column(name = "suspicious", nullable = false)
    private Boolean suspicious;

    @Column(name = "malicious", nullable = false)
    private Boolean malicious;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "text", length = 10000)
    private String text;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public YoutubeUser getUser() {
        return user;
    }

    public void setUser(YoutubeUser user) {
        this.user = user;
    }

    public YoutubeChannel getChannel() {
        return channel;
    }

    public void setChannel(YoutubeChannel channel) {
        this.channel = channel;
    }

    public YoutubeVideo getVideo() {
        return video;
    }

    public void setVideo(YoutubeVideo video) {
        this.video = video;
    }

    public YoutubeComment getComment() {
        return comment;
    }

    public void setComment(YoutubeComment comment) {
        this.comment = comment;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Instant getDateModified() {
        return dateModified;
    }

    public void setDateModified(Instant dateModified) {
        this.dateModified = dateModified;
    }

    public Boolean getSuspicious() {
        return suspicious;
    }

    public void setSuspicious(Boolean suspicious) {
        this.suspicious = suspicious;
    }

    public Boolean getMalicious() {
        return malicious;
    }

    public void setMalicious(Boolean malicious) {
        this.malicious = malicious;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}