package YoutubeArchiverDatabase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Configuration
{
    public String databaseHost;
    public int databasePort;
    public String databaseUser;
    public String databasePassword;
    public String youtubePrimaryUser;
    public String youtubePrimaryPassword;
    public String youtubeDataDirectory;
    public String youtubeApiKey;

    @JsonCreator
    public Configuration()
    {
        this.databaseHost = "localhost";
        this.databasePort = 3306;
        this.databaseUser = "root";
        this.databasePassword = "";
        this.youtubePrimaryUser = "";
        this.youtubePrimaryPassword = "";
        this.youtubeDataDirectory = "youtube-data";
        this.youtubeApiKey = "<YOUR GOOGLE API KEY>";
    }

    @JsonIgnore
    @Override
    public String toString()
    {
        return String.format("Host: %1s,\n Port: %2d,\nUser: %3s,\nPassword: %4s,\nYoutube-Primary-User: %5s,\nYoutube-Primary-Password,\nYoutube-Api-Key: %5s,\nYoutube-Directory: %6s",
                this.databaseHost, this.databasePort, this.databaseUser, this.databasePassword, this.youtubePrimaryUser, this.youtubePrimaryPassword, this.youtubeApiKey, this.youtubeDataDirectory);
    }
}