/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.binturong.diy.devTool.client.telnet;

import com.binturong.diy.devTool.client.telnet.future.SyncWrite;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.UUID;

/**
 * Simplistic telnet client.
 * @author zhulin
 */
public final class TelnetClient {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8992" : "10661"));
    private Channel ch = null;
    private EventLoopGroup group;
    private SyncWrite syncWrite = new SyncWrite();
    private Request request;

    public  void connect() throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new TelnetClientInitializer(sslCtx));

            // Start the connection attempt.
            ch = b.connect(HOST, PORT).sync().channel();
            request= new Request();
            request.setRequestId(UUID.randomUUID().toString());
            // Read commands from the stdin.
        } catch (Exception e) {
            throw e;
        }
    }

    public String send(String commandLine) throws Exception {
        request.setParam(commandLine);
        Response response = syncWrite.syncWrite(request, ch, 10000);
        Object result = response.getResult();
        return (String) result;
    }

    public void shutdownGracefully() {
        group.shutdownGracefully();
    }

}
