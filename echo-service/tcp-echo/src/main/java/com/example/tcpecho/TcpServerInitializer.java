package com.example.tcpecho;

import com.example.tcpecho.decoder.TcpServerDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import io.netty.channel.socket.SocketChannel;

@Component
@RequiredArgsConstructor
public class TcpServerInitializer extends ChannelInitializer<io.netty.channel.socket.SocketChannel> {

    private final TcpHandler handler;

    // 클라이언트 소켓 채널이 생성될 때 호출
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // decoder는 @Sharable이 안 됨, Bean 객체 주입이 안 되고, 매번 새로운 객체 생성해야 함
        TcpServerDecoder decoder = new TcpServerDecoder();

        // 뒤이어 처리할 디코더 및 핸들러 추가
        pipeline.addLast(decoder);
        pipeline.addLast(handler);
    }

}
