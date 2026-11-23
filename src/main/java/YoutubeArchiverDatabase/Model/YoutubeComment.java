package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "youtubecomment", schema = "youtubearchiver")
public class YoutubeComment extends YoutubeArchiverEntityBase
{
    public YoutubeComment()
    {

    }

    @Id
    @Column(name = "id", nullable = false, length = 100)
    public String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId")
    public YoutubeComment parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    public YoutubeUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channelId", nullable = false)
    public YoutubeChannel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("''")
    @JoinColumn(name = "videoId", nullable = false)
    public YoutubeVideo video;

    @Column(name = "dateCreated", nullable = false)
    public Instant dateCreated;

    @Column(name = "dateModified", nullable = false)
    public Instant dateModified;

    @Column(name = "dateDeletedOnline")
    public Instant dateDeletedOnline;

    @Column(name = "apiCreated", nullable = false)
    public Boolean apiCreated;

    @Column(name = "deleted", nullable = false)
    public Boolean deleted;

    @Column(name = "deletedOnline", nullable = false)
    public Boolean deletedOnline;

    @Column(name = "hashValue", nullable = false, length = 1000)
    public String hashValue;

    @Column(name = "text", length = 10000)
    public String text;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public YoutubeComment getParent() {
        return parent;
    }

    public void setParent(YoutubeComment parent) {
        this.parent = parent;
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

    public Instant getDateDeletedOnline() {
        return dateDeletedOnline;
    }

    public void setDateDeletedOnline(Instant dateDeletedOnline) {
        this.dateDeletedOnline = dateDeletedOnline;
    }

    public Boolean getApiCreated() {
        return apiCreated;
    }

    public void setApiCreated(Boolean apiCreated) {
        this.apiCreated = apiCreated;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getDeletedOnline() {
        return deletedOnline;
    }

    public void setDeletedOnline(Boolean deletedOnline) {
        this.deletedOnline = deletedOnline;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
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
        return "YoutubeComment@" + getId();
    }
}