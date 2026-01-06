package com.cboda.agora.service.service.callback;


import androidx.annotation.Nullable;

public interface AUIBroadCallback {

    /**
     * @param error null: success, notNull: fail
     */
    void onResult(@Nullable AUIBroadException error);

}
