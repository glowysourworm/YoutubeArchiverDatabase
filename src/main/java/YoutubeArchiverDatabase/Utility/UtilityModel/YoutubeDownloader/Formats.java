package YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Formats
{
    @JsonProperty("format_id")
    public String formatId;

    @JsonProperty("format_note")
    public String formatNote;

    public String ext;
    public String protocol;
    public String acodec;
    public String vcodec;
    public String url;
    public Integer width;
    public Integer height;
    public Double fps;
    public Integer rows;
    public Integer columns;
    public List<Fragments> fragments;

    @JsonProperty("audio_ext")
    public String audioExt;

    @JsonProperty("video_ext")
    public String videoExt;

    public Integer vbr;
    public Integer abr;
    public String resolution;

    @JsonProperty("aspect_ratio")
    public Double aspectRatio;

    @JsonProperty("http_headers")
    public HttpHeaders httpHeaders;

    public String format;
}
