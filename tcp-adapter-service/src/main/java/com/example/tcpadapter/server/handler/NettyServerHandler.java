package com.example.tcpadapter.server.handler;

import com.example.tcpadapter.client.HttpClient;
import com.example.tcpadapter.util.ConfigProperties;
import com.example.tcpadapter.util.TcpConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final int maxJars = 2048;
    private static int nItfInHash = 0;
    private static final long[] itfFileModTime = new long[maxJars];
    private static final ConcurrentHashMap<String, Integer> itfFileIdHash
            = new ConcurrentHashMap<String, Integer>(2048);
    private static final ConcurrentHashMap<String, String> urlInfoMap
            = new ConcurrentHashMap<String, String>(2048);

    private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);
    private final ConfigProperties config;
    private RestTemplate restTemplate;
    private String encoding;
    private String httpEncoding;
    private String httpReadTimeOut;

    private static final AsciiString HOST = new AsciiString("HOST");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("CONTENT_LENGTH");
    private static final AsciiString CONNECTION = new AsciiString("Connection");


    public NettyServerHandler(ConfigProperties config , RestTemplate restTemplate){
        this.config = config;
        this.restTemplate = restTemplate;

        encoding = config.getTcpencoding();
        if(encoding == null || encoding.isBlank()) {
            encoding = TcpConstants.GTW_Encode_EUCKR;
        }

        httpEncoding = config.getHttpEncoding();
        if(httpEncoding == null || httpEncoding.isBlank()) {
            httpEncoding = TcpConstants.GTW_Encode_UTF8;
        }

        httpReadTimeOut = config.getHttpReadTimeOut();
        if(httpReadTimeOut == null || httpReadTimeOut.isBlank()) {
            httpReadTimeOut = "30";
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object oMsg) throws Exception {
        byte[] bMsg = (byte[]) oMsg;
        String orgId = config.getOrgId();
        int itfCdOffset = config.getItfcdOffset();
        int itfCdLength = config.getItfcdLength();
        byte[] bItfCd = new byte[itfCdLength];
        String gidMakeyn = config.getMakeyn();
        int gidOffset = config.getGidOffset();
        int gidLength = config.getGidLength();
        String gid = "";

        System.arraycopy(bMsg , itfCdOffset , bItfCd , 0 , itfCdLength);
        String itfCd = new String(bItfCd , encoding);
        itfCd = itfCd.trim();

        String FilePath = TcpConstants.tcprepository + File.separator + orgId + File.separator + itfCd;

        File itfFile = new File(FilePath);

        if(!itfFile.exists()){
            log.error("[" + itfCd + "] not find Interface File. Path["+FilePath+"]");
            ctx.channel().close();
            return;
        }

        String serviceUrl;

        try{
            int itfId = -1;
            String action = TcpConstants.GTW_URLMODE_ADD;

            synchronized (itfFileIdHash) {
                Integer hashIndex = itfFileIdHash.get(FilePath);

                if(hashIndex != null) {
                    itfId = hashIndex.intValue();
                    if(itfFileModTime[itfId] != itfFile.lastModified()) {
                        action = TcpConstants.GTW_URLMODE_UPT;
                        for(int idx = 0; idx < nItfInHash; idx++) {
                            itfFileModTime[idx] = 0;
                        }
                        itfFileIdHash.clear();
                        nItfInHash = 0;
                    }else {
                        action = TcpConstants.GTW_URLMODE_KEP;
                    }
                    serviceUrl = urlInfoMap.get(FilePath);
                } else {
                    itfId = nItfInHash++;
                    itfFileIdHash.putIfAbsent(FilePath, itfId);

                    Properties prop = new Properties();
                    FileInputStream fis = new FileInputStream(FilePath);
                    prop.load(new BufferedInputStream(fis));

                    serviceUrl = prop.getProperty(TcpConstants.GTW_UrlInfo);
                    fis.close();

                    urlInfoMap.put(FilePath , serviceUrl);
                    itfFileModTime[itfId] = itfFile.lastModified();
                }
            }

            if(log.isInfoEnabled())
                log.info("["+itfCd+"]["+action+"]["+FilePath+"]["+serviceUrl+"]");

            String requestData = new String(bMsg , encoding);

            if(gidMakeyn.equals(TcpConstants.useYn_Y)){
                byte[] tmpgid = new byte[gidLength];

                System.arraycopy(bMsg , gidOffset , tmpgid , 0 , gidLength);
                gid = new String(tmpgid , encoding);
            }

            if(log.isInfoEnabled())
                log.info("[" + itfCd + "] Send ["+requestData+"]");

            URL url = new URL(serviceUrl);
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(requestData.getBytes(httpEncoding));
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1 ,  HttpMethod.POST , url.getPath() , buf);
            request.headers().set(HOST, url.getHost());
            request.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(CONTENT_LENGTH , requestData.getBytes(httpEncoding).length);
            if(gidMakeyn.equals(TcpConstants.useYn_Y)){ // GID 게이트 웨이에서 받을 수 있게 기능 추가
                request.headers().set(TcpConstants.GID_KEY , gid);
            }

            HttpClient client  = new HttpClient(url, request, httpReadTimeOut);
            byte[] recv = client.sendAndRecv();

            String result = new String(recv , httpEncoding);

            if(log.isInfoEnabled())
                log.info("[" + itfCd + "] Recv ["+result+"]");

            ctx.channel().writeAndFlush(result.getBytes(encoding)).awaitUninterruptibly();

        }catch (Exception e){
            log.error("[" + itfCd + "] Exception" , e);
            e.printStackTrace();
            ctx.channel().close();
        }
    }
}
