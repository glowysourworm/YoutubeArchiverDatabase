package YoutubeArchiverDatabase.Event;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;

public class EventAggregator
{
    private static final Hashtable<String, RegisterableEventBase> eventTypeNames = new Hashtable<>();

    /// Registers event as a singleton (one per type!)
    /// @param event Event object to register
    /// @param <T> Type of RegisterableEventBase instance
    public static <T extends RegisterableEventBase> void RegisterEvent(RegisterableEventBase event)
    {
        EventAggregator.eventTypeNames.put(event.getClass().getName(), event);
    }

    /// Gets registered event object
    /// @param type Type of event object
    /// @return The event object
    /// @param <T> The type of RegisterableEventBase
    /// @throws IllegalArgumentException RegisterableEventBase type is not registered
    public static <T extends RegisterableEventBase> T GetEvent(Class<T> type) throws IllegalArgumentException
    {
        var eventRef = new AtomicReference<RegisterableEventBase>();

        EventAggregator.eventTypeNames.forEach((key, value) ->
        {
            if (key.equals(type.getName()))
            {
                eventRef.set(value);
            }
        });

        if (eventRef.get() == null)
            throw new IllegalArgumentException("Event not registered:  " + type.getName());

        return (T)eventRef.get();
    }
}
