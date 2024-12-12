package com.example.tcpadapter.client;

import com.example.tcpadapter.client.handler.HttpClientHandler;
import com.example.tcpadapter.client.handler.HttpClientHandlerSet;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.net.MalformedURLException;
import java.net.URL;

public class HttpClient {
    private URL TargetUrl;
    private DefaultFullHttpRequest request;
    private String timeout;

    public HttpClient(URL TargetUrl , DefaultFullHttpRequest request , String timeout) {
        this.TargetUrl = TargetUrl;
        this.request = request;
        this.timeout = timeout;
    }

    public byte[] sendAndRecv() {

        EventLoopGroup group = new NioEventLoopGroup();
        byte[] rcvData = null;
        try {
            Bootstrap b = new Bootstrap();

            HttpClientHandler handler = new HttpClientHandler();
            HttpClientHandlerSet handlerSet = new HttpClientHandlerSet(handler , timeout);
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(handlerSet);

            Channel ch = b.connect(TargetUrl.getHost(), TargetUrl.getPort()).sync().channel();
            ch.writeAndFlush(request);

            ch.closeFuture().sync();

            rcvData = handler.getData();

            String errMsg = handlerSet.getReadTimeOutMsg();

            if(errMsg != null && !errMsg.equals("")){
                throw new Exception(errMsg);
            }
        }catch(Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

        return rcvData;
    }

    public static void main(String[] args) {
        try {
            URL url = new URL("https://127.0.0.1:25000");
            String msg = "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?><teleMsg>    <header>        <globId>20140025ivrapdwnkapd1500100160007500</globId>        <chnIntfId>OWNK_02002800</chnIntfId>        <envInfoDvcd>D</envInfoDvcd>        <frstTrnmSysCd>IVR</frstTrnmSysCd>        <trnmSysCd>IVR</trnmSysCd>        <trgtSysCd>WNK</trgtSysCd>        <demdRespDvcd>Q</demdRespDvcd>        <syncDvCd>S</syncDvCd>        <mesgDemdDttm>20140325150010016</mesgDemdDttm>        <emNo></emNo>        <deptCd></deptCd>        <frstTrnmIp></frstTrnmIp>        <winkPgmId></winkPgmId>        <dlKndCd>D</dlKndCd>    </header>    <body>        <CPNO>0267427766</CPNO>    </body>    <end>        <teleMsgEndCd>$$</teleMsgEndCd>    </end></teleMsg>";
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getBytes());
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1 ,  HttpMethod.POST , url.getPath() , buf);
            request.headers().set(HttpHeaders.Names.HOST, url.getHost());
            request.headers().set(HttpHeaders.Names.CONTENT_TYPE , HttpHeaders.Values.APPLICATION_JSON);
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH , msg.getBytes().length);
            request.headers().set(HttpHeaders.Names.AUTHORIZATION , "111111111");

            HttpClient client  = new HttpClient(url, request, "30000");
            byte[] recv = client.sendAndRecv();

            System.out.println("RECV["+new String(recv)+"]");

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
