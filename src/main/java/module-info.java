module YoutubeArchiverDatabase {
    requires jakarta.persistence;
    requires jakarta.annotation;
    requires com.google.api.services.youtube;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires org.hibernate.orm.core;
    requires tools.jackson.databind;
    requires google.api.client;

    exports YoutubeArchiverDatabase.Model;
    exports YoutubeArchiverDatabase.Service;
    exports YoutubeArchiverDatabase.Utility;
    exports YoutubeArchiverDatabase.Utility.UtilityModel;
    exports YoutubeArchiverDatabase;
    exports YoutubeArchiverDatabase.Event;
    exports YoutubeArchiverDatabase.Logging;
    exports YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;
}