package com.freddy.im.http.model;

@SuppressWarnings("unused")
@Deprecated
public class WanBannerItem {

    private String desc;
    private int id;
    private String imagePath;
    private int isVisible;
    private int order;
    private String title;
    private int type;
    private String url;

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getUrl() {
        return url;
    }
}
