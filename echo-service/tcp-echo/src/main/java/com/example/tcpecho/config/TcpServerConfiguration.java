package com.example.tcpecho.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
@RequiredArgsConstructor
public class TcpServerConfiguration {

    @Value("${server.host}")
    private String host;

    @Value("${server.port}")
    private int port;

    @Value("${server.netty.boss-count}")
    private int bossCount;

    @Value("${server.netty.worker-count}")
    private int workerCount;

    @Value("${server.netty.keep-alive}")
    private boolean keepAlive;

    @Value("${server.netty.log-level}")
    private String loglevel;


    @Bean
    public ServerBootstrap serverBootstrap(ChannelInitializer channelInitializer) {
        // ServerBootstrap: 서버 설정을 도와주는 class
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup(), workerGroup())
                // NioServerSocketChannel: incoming connections를 수락하기 위해 새로운 Channel을 객체화할 때 사용
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                // ChannelInitializer: 새로운 Channel을 구성할 때 사용되는 특별한 handler. 주로 ChannelPipeline으로 구성
                .childHandler(channelInitializer);

        return b;
    }

    // boss: incoming connection을 수락하고, 수락한 connection을 worker에게 등록(register)
    @Bean(destroyMethod = "shutdownGracefully")
    public EventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossCount);
    }

    // worker: boss가 수락한 연결의 트래픽 관리
    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerCount);
    }

    // IP 소켓 주소(IP 주소, Port 번호)를 구현
    // 도메인 이름으로 객체 생성 가능
    @Bean
    public InetSocketAddress inetSocketAddress() {
        return new InetSocketAddress(host, port);
    }
}
