package com.example.tcpadapter.client.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.util.concurrent.TimeUnit;

public class HttpClientHandlerSet extends ChannelInitializer<SocketChannel> {

    private int timeOut = 30;	//default
    private HttpReadTimeOutHandler timeoutHandler;
    private HttpClientHandler handler;

    public HttpClientHandlerSet(HttpClientHandler handler , String timeOutStr) {
        this.handler = handler;
        try{
            this.timeOut = Integer.parseInt(timeOutStr);
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        timeoutHandler = new HttpReadTimeOutHandler(timeOut, TimeUnit.SECONDS);
        p.addLast("ReadTimeOutHandler", timeoutHandler);
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(1048576));
        p.addLast(handler);
    }

    public String getReadTimeOutMsg() {
        return timeoutHandler.getErrMsg();
    }

}
