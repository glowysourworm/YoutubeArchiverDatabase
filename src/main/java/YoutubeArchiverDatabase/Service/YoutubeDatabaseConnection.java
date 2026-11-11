package YoutubeArchiverDatabase.Service;

import YoutubeArchiverDatabase.Configuration;
import YoutubeArchiverDatabase.Logging.LogMessage;
import YoutubeArchiverDatabase.Model.*;
import YoutubeArchiverDatabase.Utility.ApplicationHelpers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class YoutubeDatabaseConnection
{
    private final Configuration configuration;

    public YoutubeDatabaseConnection(Configuration config)
    {
        this.configuration = config;
    }

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
                ApplicationHelpers.Log(LogMessage.Info("Adding YoutubeComment entity to database:  %1s", commentData.getId()));
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
                    ApplicationHelpers.Log(LogMessage.Info("Updating YoutubeComment entity (moving current to historical comment):  %1s", comment.getId()));

                    // Create Historical Comment
                    var historicalComment = new YoutubeCommentHistorical();

                    // -> Map properties from current comment
                    historicalComment.setApiCreated(comment.getApiCreated());
                    historicalComment.setCommentId(comment.getId());
                    historicalComment.setHashValue(YoutubeEntityHashCalculator.CreateHashValue(comment));   // Calculate hash value
                    historicalComment.setDateCreated(Instant.now());

                    ApplicationHelpers.Log(LogMessage.Info("Persisting YoutubeCommentHistorical:  %1s", historicalComment.getId()));

                    // Persist Comment History
                    session.persist(historicalComment);

                    // Map properties to new YoutubeComment
                    var mapper = new ObjectMapper();

                    // Map properties to new YoutubeComment
                    comment = mapper.convertValue(commentData, YoutubeComment.class);

                    ApplicationHelpers.Log(LogMessage.Info("Persisting YoutubeComment:  %1s", historicalComment.getId()));

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
                ApplicationHelpers.Log(LogMessage.Info("Adding YoutubeUser entity to database:  %1s", userData.getId()));
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
                    ApplicationHelpers.Log(LogMessage.Info("Updating YoutubeUser entity:  %1s", user.getId()));

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
                ApplicationHelpers.Log(LogMessage.Info("Adding YoutubeChannel entity to database:  %1s", channelData.getId()));
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
                    ApplicationHelpers.Log(LogMessage.Info("Updating YoutubeChannel entity:  %1s", channel.getId()));

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
                ApplicationHelpers.Log(LogMessage.Info("Adding YoutubeVideo entity to database:  %1s", videoData.getId()));
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
                    ApplicationHelpers.Log(LogMessage.Info("Updating YoutubeVideo entity:  %1s", video.getId()));

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
        // Create Session (+Factory) and auto-close with try-with-resources.
        try(var session = OpenDB())
        {
            // Logging already happened
            if (session == null)
                return false;

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

    private Session OpenDB()
    {
        // Configuration:  Some parts are in hibernate.cfg.xml. The other parts that depend on
        //                 user settings are applied here.
        //
        var configuration = new org.hibernate.cfg.Configuration().configure();

        try
        {
            ApplicationHelpers.Log(LogMessage.Info("Configuring database connection..."));

            // Format:  Host, Port, Database
            var jdbcUrlFormat = "jdbc:mariadb://%1s:%2d/%3s";

            configuration.setJdbcUrl(String.format(jdbcUrlFormat, this.configuration.databaseHost, this.configuration.databasePort, "YoutubeArchiver"));
            configuration.setCredentials(this.configuration.databaseUser, this.configuration.databasePassword);

            // ORM Classes
            configuration.addAnnotatedClass(LocalComment.class);
            configuration.addAnnotatedClass(YoutubeChannel.class);
            configuration.addAnnotatedClass(YoutubeComment.class);
            configuration.addAnnotatedClass(YoutubeCommentHistorical.class);
            configuration.addAnnotatedClass(YoutubeUser.class);
            configuration.addAnnotatedClass(YoutubeUserChannelMap.class);
            configuration.addAnnotatedClass(YoutubeVideo.class);

            ApplicationHelpers.Log(LogMessage.Info("Database configuration complete!"));
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error configuring database connection", e));
            return null;
        }

        try(var sessionFactory = configuration.buildSessionFactory())
        {
            ApplicationHelpers.Log(LogMessage.Info("Opening database session..."));

            var session = sessionFactory.openSession();

            if (session != null)
                ApplicationHelpers.Log(LogMessage.Info("Database session open successfully:  " + session.toString()));

            else
                ApplicationHelpers.Log(LogMessage.Info("Unknown error creating database session!"));

            return session;
        }
        catch (Exception e)
        {
            ApplicationHelpers.Log(LogMessage.Error("Error creating database session", e));
        }

        return null;
    }
}
