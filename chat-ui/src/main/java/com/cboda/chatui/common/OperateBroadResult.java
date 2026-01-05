/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.common;

public class OperateBroadResult<T> {
    T result;
    int errorCode;

    public OperateBroadResult(T result, int errorCode) {
        this.result = result;
        this.errorCode = errorCode;
    }

    public OperateBroadResult(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return errorCode == 0;
    }

    public T getResult() {
        return result;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
