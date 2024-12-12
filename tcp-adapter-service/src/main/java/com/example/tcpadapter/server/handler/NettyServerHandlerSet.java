package com.example.tcpadapter.server.handler;

import com.example.tcpadapter.server.netty.decoder.DefaultDecoder;
import com.example.tcpadapter.server.netty.encoder.DefaultEncoder;
import com.example.tcpadapter.util.ConfigProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

public class NettyServerHandlerSet extends ChannelInitializer<SocketChannel> {

    private final ConfigProperties config;
    private final RestTemplate restTemplate;
    private ReadTimeOutHandler timeOutHandler;
    private final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

    public NettyServerHandlerSet(ConfigProperties config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception{
        ChannelPipeline pipeline = socketChannel.pipeline();
        try {
            //
            pipeline.addLast("ReadTimeOutHandler", new ReadTimeOutHandler(config.getReadTimeOut(), TimeUnit.SECONDS));
            pipeline.addLast(new DefaultDecoder(config));
            pipeline.addLast(new NettyServerHandler(config, restTemplate));
            pipeline.addLast(new DefaultEncoder(config));

        } catch (Exception e) {
            log.info("NettyServerHandlerSet Error invoke : [" + e + "]");
        }
    }
}
