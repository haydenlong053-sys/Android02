package wendu.dsbridge;

/**
 * Created by du on 16/12/31.
 */

public interface  CompletionBroadHandler<T> {
    void complete(T retValue);
    void complete();
    void setProgressData(T value);
}
