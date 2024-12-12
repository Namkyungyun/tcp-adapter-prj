package com.example.tcpadapter.server.netty.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractMessageToMsgDecoder extends MessageToMessageDecoder<ByteBuf> implements Serializable {

    protected byte[] firstArray = null;
    protected int Data_size = 0;
    protected volatile Boolean Check_timer = false;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out ) throws Exception {
        userDecode(ctx, msg, out);
    }

    abstract protected void userDecode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out);

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

}
