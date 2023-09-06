package com.binturong.diy.devTool.core.shell.command;

import com.binturong.cli.core.annotaion.Cmd;
import com.binturong.cli.core.annotaion.Op;

@Cmd("watch")
public class WatchCommandBean {

    @Op(name = "class",
            shortName = "c",
            description = "class name",
            required = true,argCount = 1)
    private String clazz;


    @Op(name = "method",
            shortName = "m",
            description = "method name",
            required = true,argCount = 1)
    private String method;

}
