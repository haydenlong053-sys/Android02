package io.agora.auikit.service.callback;


import androidx.annotation.Nullable;

public interface AUIBroadCallback {

    /**
     * @param error null: success, notNull: fail
     */
    void onResult(@Nullable AUIBroadException error);

}
