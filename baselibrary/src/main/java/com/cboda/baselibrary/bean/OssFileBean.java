package com.cboda.baselibrary.bean;

public class OssFileBean {
    private AwsFileBean aws;
    private QiniuFileBean qiniu;

    public OssFileBean() {
    }

    public AwsFileBean getAws() {
        return aws;
    }

    public QiniuFileBean getQiniu() {
        return qiniu;
    }
}
