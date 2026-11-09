package YoutubeArchiverDatabase.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "youtubeuser", schema = "youtubearchiver")
public class YoutubeUser {
    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "userIconURL", length = 3000)
    private String userIconURL;

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

    @ColumnDefault("''")
    @Column(name = "hashValue", nullable = false, length = 1000)
    private String hashValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserIconURL() {
        return userIconURL;
    }

    public void setUserIconURL(String userIconURL) {
        this.userIconURL = userIconURL;
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

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

}