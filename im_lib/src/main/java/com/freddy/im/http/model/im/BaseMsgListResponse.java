package com.freddy.im.http.model.im;

import java.util.List;

/**
 * IM 接口通用响应包装：{@code errorCode}、{@code errorMsg}、泛型 {@code content}。
 * <p>
 * {@code errorCode} 缺省（未出现在 JSON）时 Gson 不会覆盖字段，保持默认值 {@code -1}，表示未解析到合法业务码。
 *
 * @param <T> {@code content} 的业务体类型，如 {@link MessageListContent}、{@link ConversationListContent}
 */
public class BaseMsgListResponse<T> {

    /** 默认 -1；服务端成功时为 0 */
    private int errorCode = -1;
    private String errorMsg;
    private List<T> content;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public List<T> getContent() {
        return content;
    }

    /** 仅当 {@code errorCode == 0} 视为成功 */
    public boolean isSuccess() {
        return errorCode == 0;
    }

    @Override
    public String toString() {
        return "BaseMsgResponse{" +
                "errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                ", content=" + content +
                '}';
    }
}
