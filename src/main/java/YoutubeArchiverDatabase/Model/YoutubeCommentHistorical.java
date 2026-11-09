package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "youtubecommenthistorical", schema = "youtubearchiver")
public class YoutubeCommentHistorical {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "commentId", nullable = false, length = 100)
    private String commentId;

    @Column(name = "dateCreated", nullable = false)
    private Instant dateCreated;

    @Column(name = "apiCreated", nullable = false)
    private Boolean apiCreated;

    @Column(name = "hashValue", nullable = false, length = 1000)
    private String hashValue;

    @Column(name = "text", length = 10000)
    private String text;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Boolean getApiCreated() {
        return apiCreated;
    }

    public void setApiCreated(Boolean apiCreated) {
        this.apiCreated = apiCreated;
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

}