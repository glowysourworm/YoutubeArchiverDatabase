package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "localcomment", schema = "youtubearchiver")
public class LocalComment extends YoutubeArchiverEntityBase
{
    public LocalComment()
    {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    public YoutubeUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channelId")
    public YoutubeChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videoId")
    public YoutubeVideo video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentId")
    public YoutubeComment comment;

    @Column(name = "dateCreated", nullable = false)
    public Instant dateCreated;

    @Column(name = "dateModified", nullable = false)
    public Instant dateModified;

    @Column(name = "suspicious", nullable = false)
    public Boolean suspicious;

    @Column(name = "malicious", nullable = false)
    public Boolean malicious;

    @Column(name = "deleted")
    public Boolean deleted;

    @Column(name = "text", length = 10000)
    public String text;

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

    @Override
    public String getName()
    {
        return "LocalComment@" + getId();
    }
}