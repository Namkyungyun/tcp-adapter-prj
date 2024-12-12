package com.example.server.handler;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;

import java.nio.charset.StandardCharsets;


public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final AsciiString CONTENT_TYPE = new AsciiString("CONTENT_TYPE");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("CONTENT_LENGTH");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws IllegalArgumentException {
        if(msg.decoderResult().isFailure()) {
          throw new IllegalArgumentException("we don't support Http/0.9");
        }
        else {
            if (msg instanceof HttpRequest) {

                HttpRequest req = (HttpRequest) msg;
                HttpMessage httpMessage = (HttpMessage) msg;
                CharSequence mimeType = HttpUtil.getMimeType(httpMessage);

                if (HttpUtil.is100ContinueExpected(req)) {
                    ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
                }
                //HttpMethod 여부 판단
                FullHttpResponse response = null;

                if (req.method().equals(HttpMethod.POST)) {
                    response
                            = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, msg.content().copy());

                    //content-type
                    if (mimeType.equals("text/plain")) {
                        response.headers().set(CONTENT_TYPE, "text/plain");
                    } else {
                        response.headers().set(CONTENT_TYPE, "application/json");
                    }

                } else if (req.method().equals(HttpMethod.GET)) {
                    String getMethodMessage = "{data.OK}";
                    ByteBuf messageBuffer = Unpooled.buffer();
                    messageBuffer.writeBytes(getMethodMessage.getBytes(StandardCharsets.UTF_8));
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(messageBuffer));
                } else {
                    String getMethodMessage = "we only support http-method get and post!";
                    ByteBuf messageBuffer = Unpooled.buffer();
                    messageBuffer.writeBytes(getMethodMessage.getBytes(StandardCharsets.UTF_8));
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(messageBuffer));
                }

                //header 정보 가져오기 공통헤더
                boolean keepAlive = HttpUtil.isKeepAlive(httpMessage);
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                //keep-alive
                if (!keepAlive) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(response);
                }

            }
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
