package YoutubeArchiverDatabase.Model;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class YoutubeArchiverEntityBase
{
    public YoutubeArchiverEntityBase()
    {

    }

    public abstract String GetName();
}
