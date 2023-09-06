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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.binturong.cli.core.Cli;
import com.binturong.cli.core.command.AnnotationCommandDefinition;
import com.binturong.cli.core.command.CommandDefinition;
import com.binturong.cli.core.command.CommandHelper;
import com.binturong.cli.core.parse.DefaultParse;
import com.binturong.diy.devTool.client.telnet.Request;
import com.binturong.diy.devTool.client.telnet.Response;
import com.binturong.diy.devTool.core.config.AgentConfig;
import com.binturong.diy.devTool.core.shell.command.WatchCommandBean;
import com.binturong.diy.devTool.core.shell.command.WatchHandler;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.ReferenceCountUtil;

import java.lang.instrument.Instrumentation;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Handles a server-side channel.
 */
@Sharable
public class DevToolServerHandler extends SimpleChannelInboundHandler<String> {

    private Instrumentation inst;
    AgentConfig agentConfig;

    public DevToolServerHandler(Instrumentation inst, AgentConfig agentConfig) {
        this.inst = inst;
        this.agentConfig = agentConfig;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String obj) throws Exception {
        Request request = JSONObject.parseObject(obj,Request.class);
        // Request request = (Request) obj;
        System.out.println("消息：" + obj);
        String result;
        boolean close = false;
        String command = (String) request.getParam();
        if (command.isEmpty()) {
            result = "Please type something.\r\n";
        } else if ("bye".equals(command)) {
            result = "Have a good day!\r\n";
            close = true;
        }  else if (command != null && command.startsWith("watch")) {
            AnnotationCommandDefinition define = CommandHelper.define(WatchCommandBean.class);
            HashMap<String, CommandDefinition> commandHashMap = new HashMap<>();
            commandHashMap.put("watch", define);
            Cli cli = new Cli("my");
            cli.setCommands(commandHashMap);
            cli.setParse(new DefaultParse());
            cli.setHandlers(Arrays.asList(new WatchHandler(inst)));
            String[] split = command.split(" ");
            System.out.println(Arrays.toString(split));
            String execute = cli.execute(split);
            result = execute;
        } else {
            result = "Did you say '" + command + "'?\r\n";
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.

        Response response = new Response();
        response.setRequestId(request.getRequestId());
        response.setResult(result);
        ChannelFuture future = ctx.writeAndFlush(JSON.toJSONString(response) +  "\r\n");
        //释放
        ReferenceCountUtil.release(request);
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
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
