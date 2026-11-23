module YoutubeArchiverDatabase {
    requires jakarta.persistence;
    requires jakarta.annotation;
    requires com.google.api.services.youtube;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires org.hibernate.orm.core;
    requires tools.jackson.databind;
    requires google.api.client;

    // Jakarta / Hibernate Issue:  It's trying to reflect the private fields rather that use the properties. So,
    //                             instead of making them public, we'll follow the suggested route of "opening the
    //                             package to hibernate" .. which is a psychotic teenager's approach to "getting my
    //                             code to work" which is guaranteed to create "security vulnerabilities".
    //
    //                             Why not expose the private fields using properties? Why not just make them public??
    //
    //                             ... but, that was the argument with which two Java devs never spoke again and, thus,
    //                             MSFT lived and thrived again...
    //
    opens YoutubeArchiverDatabase.Model to org.hibernate.orm.core, net.bytebuddy;

    exports YoutubeArchiverDatabase.Model;
    exports YoutubeArchiverDatabase.Service;
    exports YoutubeArchiverDatabase.Utility;
    exports YoutubeArchiverDatabase.Utility.UtilityModel;
    exports YoutubeArchiverDatabase;
    exports YoutubeArchiverDatabase.Event;
    exports YoutubeArchiverDatabase.Logging;
    exports YoutubeArchiverDatabase.Utility.UtilityModel.YoutubeDownloader;
}