module YoutubeArchiverDatabase {
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    exports YoutubeArchiverDatabase.Model;
    exports YoutubeArchiverDatabase.Service;
}