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
package com.binturong.diy.devTool.core.server;

import com.binturong.diy.devTool.core.config.AgentConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.lang.instrument.Instrumentation;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class DevToolServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();
    private Instrumentation inst;
    private DevToolServerHandler SERVER_HANDLER ;
    private AgentConfig agentConfig;
    public DevToolServerInitializer(Instrumentation inst, AgentConfig agentConfig) {
        this.inst = inst;
        SERVER_HANDLER = new DevToolServerHandler(inst, agentConfig);
        this.agentConfig = agentConfig;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // Add the text line codec combination first,
        pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()));
        // the encoder and decoder are static as these are sharable
        pipeline.addLast(DECODER);
        pipeline.addLast(ENCODER);
        // and then business logic.
        pipeline.addLast(SERVER_HANDLER);
    }
}
