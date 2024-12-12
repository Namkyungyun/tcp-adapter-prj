package com.example.tcpadapter.util;

import java.io.File;

public class TcpConstants {
    public static String useYn_Y = "Y";
    public static String useYn_N = "N";

    public static String GTW_UrlInfo = "gateway.url";   //http변환 후, gateway로 보내기 위한 용도들
    public static String GTW_URLMODE_ADD = "ADD ";
    public static String GTW_URLMODE_UPT = "UPDA";
    public static String GTW_URLMODE_KEP = "KEEP";

    public static String GTW_Encode_UTF8 = "UTF-8";
    public static String GTW_Encode_EUCKR = "EUC-KR";

    public static String tcpHomePath = System.getenv("TCP_MODULE_HOME");
    public static String tcprepository = tcpHomePath + File.separator + "repository";
    public static String tcpHeaderDir = tcpHomePath + File.separator + "header";

    public static String GID_KEY = "direa-gw-gid";
}
