package com.example.server;
import com.example.server.handler.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.stereotype.Component;

@Component
public class Server {
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);    // 단일 스레드로 동작하는 객체
        EventLoopGroup workerGroup = new NioEventLoopGroup();           // cpu 코어 수에 따른 스레드 수 설정

        try {
            ServerBootstrap b = new ServerBootstrap();                  // 서버부트스트랩 생성
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)              // 부모 스레드가 사용할 네트워크 입출력 모드
                    .childOption(ChannelOption.SO_LINGER, 3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 자식 채널의 초기화 방법 설정(익명으로 설정)
                        @Override                                           // 채널 초기화 시 될 시, 기본 동작이 지정된 추상 클래스
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();   // 채널 파이프
                            p.addLast(new HttpRequestDecoder());            // http 코덱(디코더)
                            p.addLast(new HttpObjectAggregator(65536)); // http 코덱(디코더)
                            p.addLast(new HttpResponseEncoder());
                            p.addLast(new HttpContentCompressor());
                            p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(new HttpServerHandler());                       // 클라이언트 연결 생성시 데이터 처리 담당
                        }
                    });
            ChannelFuture f = b.bind(8888).sync();

            f.channel().closeFuture().sync();

        }catch (Exception e) {
            e.printStackTrace();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
