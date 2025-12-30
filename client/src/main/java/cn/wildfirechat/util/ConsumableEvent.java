package cn.wildfirechat.util;

/**
 * One-off event wrapper for LiveData to avoid re-delivery on lifecycle changes.
 * Call {@link #getContentIfNotHandled()} to consume the event once.
 */
public class ConsumableEvent<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public ConsumableEvent(T content) {
        this.content = content;
    }

    /**
     * Returns the content and prevents its use again.
     * If the event has already been handled, returns null.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        }
        hasBeenHandled = true;
        return content;
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public T peekContent() {
        return content;
    }
}