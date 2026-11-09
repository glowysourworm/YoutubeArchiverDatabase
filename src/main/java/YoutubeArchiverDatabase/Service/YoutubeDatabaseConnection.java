package YoutubeArchiverDatabase.Service;

import YoutubeArchiverDatabase.Model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class YoutubeDatabaseConnection {

    /// Gets a comment from the database
    /// @param id The primary key id of the database entity
    /// @return Returns the youtube comment from the database
    public YoutubeComment GetComment(String id)
    {
        var comment = new AtomicReference<YoutubeComment>();

        if (ExecuteDB(session -> comment.set(session.find(YoutubeComment.class, id))))
            return comment.get();

        return null;
    }

    public YoutubeUser GetUser(String id)
    {
        var user = new AtomicReference<YoutubeUser>();

        if (ExecuteDB(session -> user.set(session.find(YoutubeUser.class, id))))
            return user.get();

        return null;
    }

    public YoutubeChannel GetChannel(String id)
    {
        var channel = new AtomicReference<YoutubeChannel>();

        if (ExecuteDB(session -> channel.set(session.find(YoutubeChannel.class, id))))
            return channel.get();

        return null;
    }

    public YoutubeVideo GetVideo(String id)
    {
        var video = new AtomicReference<YoutubeVideo>();

        if (ExecuteDB(session -> video.set(session.find(YoutubeVideo.class, id))))
            return video.get();

        return null;
    }

    /// Sets comment data to the database:  1) Existing comments will be checked for any
    /// changes to data. Historical comments will be created if there are changes + the
    /// caller's approval. 2) New comments will be persisted based on normal primary key
    /// checks.
    /// @param commentData Comment data to persist to the database
    /// @return True if the database execution was successful
    public boolean AddUpdate(YoutubeComment commentData)
    {
        return ExecuteDB(session ->
        {
            var comment = session.find(YoutubeComment.class, commentData.getId());

            // Add
            if (comment == null)
            {
                session.persist(commentData);
            }

            // Update
            else
            {
                var oldHashValue = comment.getHashValue();
                var newHashValue = YoutubeEntityHashCalculator.CreateHashValue(commentData);

                // -> Create Comment History
                if (!oldHashValue.equals(newHashValue))
                {
                    // Create Historical Comment
                    var historicalComment = new YoutubeCommentHistorical();

                    // -> Map properties from current comment
                    historicalComment.setApiCreated(comment.getApiCreated());
                    historicalComment.setCommentId(comment.getId());
                    historicalComment.setHashValue(YoutubeEntityHashCalculator.CreateHashValue(comment));   // Calculate hash value
                    historicalComment.setDateCreated(Instant.now());

                    // Persist Comment History
                    session.persist(historicalComment);

                    // Map properties to new YoutubeComment
                    var mapper = new ObjectMapper();

                    // Map properties to new YoutubeComment
                    comment = mapper.convertValue(commentData, YoutubeComment.class);

                    // Persist the entity (update)
                    session.persist(comment);
                }
            }
        });
    }

    /// Sets user data to the database:  1) Existing user will be checked for any
    /// changes to data. 2) New user will be persisted based on normal primary key
    /// checks.
    /// @param userData User data to persist to the database
    /// @return True if the database execution was successful
    public boolean AddUpdate(YoutubeUser userData)
    {
        return ExecuteDB(session ->
        {
            var user = session.find(YoutubeUser.class, userData.getId());

            // Add
            if (user == null)
            {
                session.persist(userData);
            }

            // Update
            else
            {
                var oldHashValue = user.getHashValue();
                var newHashValue = YoutubeEntityHashCalculator.CreateHashValue(userData);

                // -> User Changes
                if (!oldHashValue.equals(newHashValue))
                {
                    // Map properties to new YoutubeUser
                    var mapper = new ObjectMapper();

                    // Map properties to new YoutubeComment
                    user = mapper.convertValue(userData, YoutubeUser.class);

                    // Persist the entity (update)
                    session.persist(user);
                }
            }
        });
    }

    /// Sets channel data to the database:  1) Existing channel will be checked for any
    /// changes to data. 2) New user will be persisted based on normal primary key
    /// checks.
    /// @param channelData User channel to persist to the database
    /// @return True if the database execution was successful
    public boolean AddUpdate(YoutubeChannel channelData)
    {
        return ExecuteDB(session ->
        {
            var channel = session.find(YoutubeChannel.class, channelData.getId());

            // Add
            if (channel == null)
            {
                session.persist(channelData);
            }

            // Update
            else
            {
                var oldHashValue = channel.getHashValue();
                var newHashValue = YoutubeEntityHashCalculator.CreateHashValue(channelData);

                // -> Channel Changes
                if (!oldHashValue.equals(newHashValue))
                {
                    // Map properties to new YoutubeUser
                    var mapper = new ObjectMapper();

                    // Map properties to new YoutubeComment
                    channel = mapper.convertValue(channelData, YoutubeChannel.class);

                    // Persist the entity (update)
                    session.persist(channel);
                }
            }
        });
    }

    /// Sets video data to the database:  1) Existing video will be checked for any
    /// changes to data. 2) New user will be persisted based on normal primary key
    /// checks.
    /// @param videoData User video to persist to the database
    /// @return True if the database execution was successful
    public boolean AddUpdate(YoutubeVideo videoData)
    {
        return ExecuteDB(session ->
        {
            var video = session.find(YoutubeVideo.class, videoData.getId());

            // Add
            if (video == null)
            {
                session.persist(videoData);
            }

            // Update
            else
            {
                var oldHashValue = video.getHashValue();
                var newHashValue = YoutubeEntityHashCalculator.CreateHashValue(videoData);

                // -> Channel Changes
                if (!oldHashValue.equals(newHashValue))
                {
                    // Map properties to new YoutubeVideo
                    var mapper = new ObjectMapper();

                    // Map properties to new YoutubeVideo
                    video = mapper.convertValue(videoData, YoutubeVideo.class);

                    // Persist the entity (update)
                    session.persist(video);
                }
            }
        });
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
