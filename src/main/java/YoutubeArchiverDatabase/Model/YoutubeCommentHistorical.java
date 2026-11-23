package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "youtubecommenthistorical", schema = "youtubearchiver")
public class YoutubeCommentHistorical extends YoutubeArchiverEntityBase
{
    public YoutubeCommentHistorical()
    {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer id;

    @Column(name = "commentId", nullable = false, length = 100)
    public String commentId;

    @Column(name = "dateCreated", nullable = false)
    public Instant dateCreated;

    @Column(name = "apiCreated", nullable = false)
    public Boolean apiCreated;

    @Column(name = "hashValue", nullable = false, length = 1000)
    public String hashValue;

    @Column(name = "text", length = 10000)
    public String text;

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

    @Override
    public String getName()
    {
        return "YoutubeCommentHistorical@" + getId();
    }

}