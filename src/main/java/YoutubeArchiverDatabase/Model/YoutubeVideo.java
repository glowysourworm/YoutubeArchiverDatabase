package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "youtubevideo", schema = "youtubearchiver")
public class YoutubeVideo {
    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channelId", nullable = false)
    private YoutubeChannel channel;

    @Column(name = "title", length = 1000)
    private String title;

    @Column(name = "imageURL", length = 3000)
    private String imageURL;

    @Column(name = "hashValue", nullable = false, length = 1000)
    private String hashValue;

    @Column(name = "description", length = 10000)
    private String description;

    @Column(name = "apiCreated", nullable = false)
    private Boolean apiCreated;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deletedOnline", nullable = false)
    private Boolean deletedOnline;

    @Column(name = "dateCreated", nullable = false)
    private Instant dateCreated;

    @Column(name = "dateModified", nullable = false)
    private Instant dateModified;

    @Column(name = "dateDeletedOnline")
    private Instant dateDeletedOnline;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public YoutubeChannel getChannel() {
        return channel;
    }

    public void setChannel(YoutubeChannel channel) {
        this.channel = channel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}