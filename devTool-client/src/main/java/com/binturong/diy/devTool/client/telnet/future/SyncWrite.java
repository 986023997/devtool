package com.binturong.diy.devTool.client.telnet.future;

import com.alibaba.fastjson2.JSON;
import com.binturong.diy.devTool.client.telnet.Request;
import com.binturong.diy.devTool.client.telnet.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author 朱林
 * @description
 * @date 2021/11/25 10:50
 */
public class SyncWrite {

    public Response syncWrite(Request request, Channel channel, long timeout) {
        if (request == null) {
            throw new RuntimeException("请求为空！");
        }
        if (channel == null) {
            throw new RuntimeException("未建立连接！");
        }
        if (timeout < 0 ) {
            throw new RuntimeException("timeout error");
        }
        //
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);
        WriteFuture<Response> syncWriteFuture = new SyncWriteFuture(requestId);
        SyncWriteMap.syncKey.put(requestId, syncWriteFuture);
        Response response = doSyncWrite(channel, request, syncWriteFuture, timeout);
        SyncWriteMap.syncKey.remove(requestId, syncWriteFuture);
        return response;
    }

    private Response doSyncWrite(Channel channel, Request request, WriteFuture<Response> writeFuture, long timeOut) {
        Response response = null;
        channel.writeAndFlush(JSON.toJSONString(request) + "\r\n").addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                writeFuture.setWriteResult(channelFuture.isSuccess());
                writeFuture.setCause(channelFuture.cause());
                //失败移除
                if (!writeFuture.isWriteSuccess()) {
                    SyncWriteMap.syncKey.remove(writeFuture.getRequestId());
                }
            }});

        try {
            response = writeFuture.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return response;
    }

}
