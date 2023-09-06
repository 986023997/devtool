package com.binturong.diy.devTool.core.server;

import com.binturong.diy.devTool.core.config.AgentConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.instrument.Instrumentation;

/**
 * @author zhulin
 * @date 2023-08-31 14:29
 */
public class DevToolProxyServer {

    private Instrumentation inst;
    private String args;
    private static DevToolProxyServer instance;
    private AgentConfig agentConfig;
    private EventLoopGroup workerGroup;


    private DevToolProxyServer(Instrumentation inst, String args) {
        this.inst = inst;
        this.args = args;
        agentConfig = AgentConfig.parse(args);
    }

    public static DevToolProxyServer getInstance(Instrumentation instrumentation, String args) {
        if (instance == null) {
            instance = new DevToolProxyServer(instrumentation, args);
        }
        return instance;
    }


    public void start() {
        this.bindServer();
        Runtime.getRuntime().addShutdownHook(new Thread("hooker") {
            @Override
            public void run() {
                DevToolProxyServer.this.shutdownGracefully();
            }
        });
    }


    /**
     * 启动服务
     */
    public void bindServer() {
        workerGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.group(workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new DevToolServerInitializer(inst, agentConfig));
            ChannelFuture future = b.bind("localhost", 10661);
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("启动成功");
                    } else {
                        System.out.println("启动失败");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void shutdownGracefully() {
        if (workerGroup != null) {
            System.out.println("关闭");
            workerGroup.shutdownGracefully();
        }

    }
}
