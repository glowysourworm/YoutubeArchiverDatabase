module YoutubeArchiverDatabase {
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires tools.jackson.databind;
    requires com.google.api.services.youtube;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;

    exports YoutubeArchiverDatabase.Model;
    exports YoutubeArchiverDatabase.Service;
    exports YoutubeArchiverDatabase.Utility;
    exports YoutubeArchiverDatabase.Utility.UtilityModel;
}