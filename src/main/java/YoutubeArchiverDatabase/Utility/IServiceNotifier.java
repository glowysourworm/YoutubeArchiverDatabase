package YoutubeArchiverDatabase.Utility;

import java.util.function.Consumer;

/// This is a simple concept interface that should imply a listener interface with a payload
public interface IServiceNotifier<T>
{
    /// Adds listener Consumer to the notifications for the service
    /// @param listener
    void AddListener(Consumer<T> listener);

    /// Calls all listeners (in the order they were added)
    void Notify();
}
