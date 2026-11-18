package YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Heatmap
{
    @JsonProperty("start_time")
    public Integer startTime;

    @JsonProperty("end_time")
    public Double endTime;

    public Double value;
}
