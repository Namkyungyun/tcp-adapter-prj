package com.example.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;


import java.net.http.HttpRequest;

import java.util.Scanner;

public class Handler extends ChannelInboundHandlerAdapter {

    private HttpRequest request;
    Scanner sc = null;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        sc = new Scanner(System.in);
        String sendMessage = sc.nextLine();

        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes());


        StringBuilder sb = new StringBuilder();
        sb.append("전송한 문자열[");
        sb.append(sendMessage);
        sb.append("]");

        System.out.println(sb);

        ctx.writeAndFlush(messageBuffer);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String readMessage = ((ByteBuf)msg).toString(CharsetUtil.UTF_8);

        StringBuilder sb = new StringBuilder();
        sb.append("수신한 문자열 [");
        sb.append(readMessage);
        sb.append("]");

        System.out.println(sb);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
