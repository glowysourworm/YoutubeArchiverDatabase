package YoutubeArchiverDatabase;

/// Component that performs an extra initialization step when application is finished
/// launching. This will set the configuration and load initial data from the data
/// services
public interface IYoutubeArchiverInitializer
{
    /// Initializes the component with the application configuration
    /// @param configuration Application configuration
    void Initialize(Configuration configuration);

    /// Returns true if the component has already been initialized
    /// @return true / false indicating initialization
    boolean IsInitialized();
}
