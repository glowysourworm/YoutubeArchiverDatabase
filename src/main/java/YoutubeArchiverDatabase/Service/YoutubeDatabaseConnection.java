package YoutubeArchiverDatabase.Service;

import YoutubeArchiverDatabase.Model.YoutubeComment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class YoutubeDatabaseConnection {

    /// Gets a comment from the database
    /// @param id The primary key id of the database entity
    /// @return Returns the youtube comment from the database
    public YoutubeComment GetComment(long id)
    {
        var comment = new AtomicReference<YoutubeComment>();

        if (ExecuteDB(session -> comment.set(session.find(YoutubeComment.class, id))))
            return comment.get();

        return null;
    }

    /// Sets comment data to the database
    /// @param comment Comment to persist to the database
    /// @return True if the database execution was successful
    public boolean SetComment(YoutubeComment comment)
    {
        return ExecuteDB(session -> session.persist(comment));
    }

    public boolean ExecuteDB(Consumer<Session> callback)
    {
        var configuration = GetConfiguration();

        // Create Session Factory and auto-close with try-with-resources.
        try(SessionFactory sessionFactory = configuration.buildSessionFactory())
        {

            // Initialize Session Object
            var session = sessionFactory.openSession();

            session.beginTransaction();

            // Execute User Code
            callback.accept(session);

            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private Configuration GetConfiguration()
    {
        var configuration = new Configuration();

        configuration.setJdbcUrl("");
        configuration.setCredentials("root", "!ngndol234");
        configuration.addAnnotatedClass(YoutubeComment.class);

        return configuration;
    }
}
