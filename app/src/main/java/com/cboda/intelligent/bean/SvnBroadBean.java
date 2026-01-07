package com.cboda.intelligent.bean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/10/19
 * @Type:
 */
public class SvnBroadBean {
    public List<String> server;
    public List<Vpn> vpn;
    public String chaturl;
    public static class Vpn{
        public String name;
        public String dns;
        public String port;
        public String password;
        public String domain;
        public String sni;
        public boolean isCheck;
        public List<String> server;
    }
}
