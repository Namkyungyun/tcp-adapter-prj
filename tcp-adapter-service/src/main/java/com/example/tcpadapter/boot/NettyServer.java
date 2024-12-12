package com.example.tcpadapter.boot;

import com.example.tcpadapter.server.handler.NettyServerHandlerSet;
import com.example.tcpadapter.util.ConfigProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@Component
public class NettyServer {

    private static Logger log = LoggerFactory.getLogger(NettyServer.class);

    @Value("${tcp.port}")
    private int tcpPort;

    public void start( ) {
        ConfigProperties config = setConfig();
        RestTemplate restTemplate = restTemplate();

        EventLoopGroup bossGroup = new NioEventLoopGroup(4);
        EventLoopGroup workerGroup = new NioEventLoopGroup(300); //2400개

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childOption(ChannelOption.TCP_NODELAY, true)                 //소켓 옵션 : 데이터 송수신에 Nagle 알고리즘 비활성화여부
                    .childOption(ChannelOption.SO_KEEPALIVE, true)                //소켓 옵션 : 지정된 시간에 한번 씩 keepalive 패킷을 상대방에게 전송
                    .childOption(ChannelOption.SO_LINGER, 10)                     //소켓 옵션 : 소켓 닫을 때 커널의 송신 버퍼에 전송되지않은 데이터의 전송 대기 시간 지정
                    .childOption(ChannelOption.SO_REUSEADDR, true)                //소켓 옵션 : time-await상태의 포트를 서버 소켓에 바인드
                    .childOption(ChannelOption.SO_BACKLOG, 1000)                  //소켓 옵션 : 동시에 수용가능한 소켓 연결 요청 수
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator((int) 4096)) //상대방으로부터 수신할 커널 수신 버퍼의 크기
                    .childHandler(new NettyServerHandlerSet(config , restTemplate));    //이벤트 처리 핸들러

        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    @Value("${http.ReadTimeOut}")
    private String httpReadTimeOut;
    @Value("${http.encoding}")
    private String httpEncoding;
    public RestTemplate restTemplate() {

        if(httpReadTimeOut == null || httpReadTimeOut.equals("")){
            httpReadTimeOut = "30";
        }
        if(httpEncoding == null || httpEncoding.equals("")){
            httpEncoding = "UTF-8";
        }

        int timeout = Integer.parseInt(httpReadTimeOut);
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(timeout*1000);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName(httpEncoding)));
        log.info("restTemplate 확인 => " +restTemplate.getMessageConverters().toString());
        return restTemplate;
    }

    // Config Setting...
    @Value("${tcp.org}")
    private String orgId;
    @Value("${tcp.encoding}")
    private String tcpencoding;
    @Value("${tcp.ReadTimeOut}")
    private int ReadTimeOut;


    @Value("${tcp.lengthDataSizerefyn}")
    private String lengthDataSize_ref_yn;
    @Value("${tcp.lengthOffSet}")
    private int lengthOffSet;
    @Value("${tcp.lengthDataSize}")
    private int lengthDataSize;
    @Value("${tcp.DataOffSet}")
    private String DataOffSet;

    @Value("${tcp.gid.makeyn}")
    private String makeyn;
    @Value("${tcp.gid.offset}")
    private int gidOffset;
    @Value("${tcp.gid.length}")
    private int gidLength;

    @Value("${tcp.itfcd.offset}")
    private int itfcdOffset;
    @Value("${tcp.itfcd.length}")
    private int itfcdLength;


    public ConfigProperties setConfig(){
        ConfigProperties config = new ConfigProperties();
        config.setTcpPort(tcpPort);
        config.setOrgId(orgId);
        config.setTcpencoding(tcpencoding);
        config.setReadTimeOut(ReadTimeOut);
        config.setLengthDataSize_ref_yn(lengthDataSize_ref_yn);
        config.setLengthOffSet(lengthOffSet);
        config.setLengthDataSize(lengthDataSize);
        config.setMakeyn(makeyn);
        config.setGidOffset(gidOffset);
        config.setGidLength(gidLength);
        config.setItfcdOffset(itfcdOffset);
        config.setItfcdLength(itfcdLength);
        config.setHttpReadTimeOut(httpReadTimeOut);
        config.setHttpEncoding(httpEncoding);
        return config;
    }
}
