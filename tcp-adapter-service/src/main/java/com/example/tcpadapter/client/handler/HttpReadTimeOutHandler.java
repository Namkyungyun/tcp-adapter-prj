package com.example.tcpadapter.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

public class HttpReadTimeOutHandler extends ReadTimeoutHandler {

    private boolean timeOutCheck ;
    private String errMsg;

    public HttpReadTimeOutHandler(int timeoutSeconds, TimeUnit timeUnit) {
        super(timeoutSeconds, timeUnit);
        this.timeOutCheck = false;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        this.timeOutCheck = true;
        Thread.sleep(2000);
        super.readTimedOut(ctx);
    }

    public String getErrMsg() {
        return this.errMsg;
    }

    public void setErrMsg(String errMsg){
        this.errMsg = errMsg;
    }

    public boolean isTimeOutCheck() {
        return timeOutCheck;
    }

    public void setTimeOutCheck(boolean timeOutCheck) {
        this.timeOutCheck = timeOutCheck;
    }

}