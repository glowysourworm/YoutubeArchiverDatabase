package YoutubeArchiverDatabase.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "usergroupusermap", schema = "youtubearchiver")
public class UserGroupUserMap extends YoutubeArchiverEntityBase
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private YoutubeUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userGroupId", nullable = false)
    private UserGroup userGroup;

    @Column(name = "isLeader", nullable = false)
    private Boolean isLeader;

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

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public Boolean getIsLeader() {
        return isLeader;
    }

    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }

    @Override
    public String GetName()
    {
        return this.getId().toString();
    }
}