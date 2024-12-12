package com.example.tcpadapter.util;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ConfigProperties {
    private int tcpPort;
    private String orgId;
    private String tcpencoding;
    private int ReadTimeOut;
    private int lengthOffSet;
    private int lengthDataSize;
    private String DataOffSet;
    private String makeyn;
    private int gidOffset;
    private int gidLength;
    private int itfcdOffset;
    private int itfcdLength;
    private String lengthDataSize_ref_yn;
    private String httpReadTimeOut;
    private String httpEncoding;
}
