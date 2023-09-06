package com.binturong.diy.devTool.core.util;

import java.lang.management.ManagementFactory;

/**
 * @author zhulin
 * @date 2023-09-04 10:19
 */
public class PidUtil {

    private static final Long currentPid;


    static {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf('@');
        if (index < 1) {
           currentPid = null;
        } else {
            currentPid = Long.parseLong(jvmName.substring(0, index));
        }


    }


    public static Long getCurrentPid() {
        return currentPid;
    }
}
