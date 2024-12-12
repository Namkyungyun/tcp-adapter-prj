package com.example.tcpadapter.server.netty.encoder;

import com.example.tcpadapter.util.ByteDataLengthSet;
import com.example.tcpadapter.util.ConfigProperties;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class DefaultEncoder extends MessageToMessageEncoder<byte[]> {
    private final Logger logger = LoggerFactory.getLogger(DefaultEncoder.class);
    private ConfigProperties config;
    private String encoding;

    public DefaultEncoder(ConfigProperties config){
        this.config = config;
        this.encoding = this.config.getTcpencoding();
        if( this.encoding == null || "".equals(this.encoding))
            this.encoding = "EUC-KR";
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] bytes, List<Object> out) throws Exception {
        try {
            ByteDataLengthSet bset = new ByteDataLengthSet();
            byte[] data = bset.LengthSetChange(bytes, config);//길이부 실제길이체크;

            if(logger.isInfoEnabled()) {
                InetSocketAddress raddr = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
                String remoteAddr = raddr.getAddress().getHostAddress() + ":" + raddr.getPort();

                InetSocketAddress laddr = (InetSocketAddress) channelHandlerContext.channel().localAddress();
                String localAddr = laddr.getAddress().getHostAddress() + ":" + laddr.getPort();

                logger.info("■ TRANLog Send Data   [" + localAddr + " -> " + remoteAddr + "] length ["+data.length+"]\n [" +  new String(data , encoding)+"]");
            }

            out.add(Unpooled.wrappedBuffer(data));
//                log.info("■ TCP Send OK.");
        } catch (Exception e) {
            if(logger.isErrorEnabled())
                logger.error("Encoder Error" , e);

            throw new Exception();
        }
    }
}
