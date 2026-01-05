/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.net.base;

/**
 * Created by imndx on 2017/12/15.
 */

public class ResultBroadWrapper<T> extends StatusBroadResult {
    T result;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
