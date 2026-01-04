package com.cboda.baselibrary.bean;

public class OssFileBroadBean {
    private AwsFileBroadBean aws;
    private QiniuFileBroadBean qiniu;

    public OssFileBroadBean() {
    }

    public AwsFileBroadBean getAws() {
        return aws;
    }

    public QiniuFileBroadBean getQiniu() {
        return qiniu;
    }
}
