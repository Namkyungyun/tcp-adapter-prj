package com.example.tcpadapter.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpClientHandler extends SimpleChannelInboundHandler<Object> {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private byte[] data;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        HttpRequest request;
        HttpResponse response;

        HashMap<String, Object> HeaderReqData = new HashMap<>();
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            HttpHeaders headers = request.headers();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> h : headers) {
                    String key = h.getKey();
                    HeaderReqData.put(key, h.getValue());
                }

            }
        }

        HashMap<String, Object> HeaderResData = new HashMap<>();
        if (msg instanceof HttpResponse) {
            response = (HttpResponse) msg;
            HttpHeaders headers = response.headers();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> h : headers) {
                    String key = h.getKey();
                    HeaderResData.put(key, h.getValue());
                }

            }
        }

        int readerIndex;
        byte[] bData = null;
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                readerIndex = content.readableBytes();
                bData = new byte[readerIndex];
                content.readBytes(bData);
            }
            content.clear();
            // }else if(msg instanceof FullHttpRequest){
            //     FullHttpRequest Freq = (FullHttpRequest)msg;
            //     ByteBuf buf = Freq.content();
            //     readerIndex = buf.readableBytes();
            //     bData = new byte[readerIndex];
            //     buf.getBytes(readerIndex, bData);
            //     buf.clear();
        }

        setData(bData);

        try{
            ctx.channel().close().await();
        }catch(Exception e){
            throw(e);
        }

    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
