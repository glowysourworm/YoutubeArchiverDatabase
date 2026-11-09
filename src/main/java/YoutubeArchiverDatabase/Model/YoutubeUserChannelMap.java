package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "youtubeuserchannelmap", schema = "youtubearchiver")
public class YoutubeUserChannelMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private YoutubeUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channelId", nullable = false)
    private YoutubeChannel channel;

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

}