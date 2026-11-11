package YoutubeArchiverDatabase.Event;

import java.util.*;
import java.util.function.Consumer;

public abstract class RegisterableEvent<T> implements RegisterableEventBase
{
    private final Hashtable<String, Consumer<T>> actions;

    public RegisterableEvent()
    {
        this.actions = new Hashtable<>();
    }

    /// Subscribes to event; and returns a unique registration token.
    /// @param action Action to perform when event fires
    /// @return Unique UUID token string used to un-subscribe the event
    public String Subscribe(Consumer<T> action)
    {
        var token = UUID.randomUUID().toString();

        this.actions.put(token, action);

        return token;
    }

    /// Removes subscription to event specified by the token.
    /// @param token Token received by subscribing to the event
    /// @throws IllegalArgumentException Token is invalid, missing, or the subscription was never acquired.
    public void UnSubscribe(String token) throws IllegalArgumentException
    {
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("Invalid or missing event subscription token");

        if (!this.actions.containsKey(token))
            throw new IllegalArgumentException("Missing subscription to event");

        this.actions.remove(token);
    }

    public void Publish(T payload)
    {
        for (var action : this.actions.values())
        {
            // Run Consumers (notify)
            action.accept(payload);
        }
    }
}
