package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "youtubeuserchannelmap", schema = "youtubearchiver")
public class YoutubeUserChannelMap extends YoutubeArchiverEntityBase
{
    public YoutubeUserChannelMap()
    {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    public YoutubeUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channelId", nullable = false)
    public YoutubeChannel channel;

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

    @Override
    public String getName()
    {
        return "YoutubeUserChannelMap@" + getId();
    }
}