package YoutubeArchiverDatabase.Utility;

/// Specifies a service that initializes with some data object
/// @param <T> The type of data object required for initialization
public interface IServiceInitializer<T>
{
    /// Initializes service
    /// @param initializationData Data specific to the initialization
    void Initialize(T initializationData);

    /// Allows for re-initialization of the service with some new data
    /// @param initializationData Data specific to the initialization
    void ReInitialize(T initializationData);

    /// Returns true if the
    /// @return True if the service is initialized
    boolean IsInitialized();
}
