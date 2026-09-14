package com.freddy.im.http.model.im;

/** {@code /im/meeting/end} 响应 content。 */
@SuppressWarnings("unused")
public class MeetingEndContent implements java.io.Serializable {

    private Long endedAt;
    private Integer duration;

    public Long getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Long endedAt) {
        this.endedAt = endedAt;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
