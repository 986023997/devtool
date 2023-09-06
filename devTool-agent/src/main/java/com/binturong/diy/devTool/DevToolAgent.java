package com.binturong.diy.devTool;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Map;

/**
 * @author zhulin
 * @date 2023-08-31 10:31
 */
public class DevToolAgent {

    private static PrintStream ps = System.err;

    private static ClassLoader devToolClassLoader;
    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    // 代理开始的位置
    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    private static ClassLoader getClassLoader(Instrumentation inst, File arthasCoreJarFile) throws Throwable {
        // 构造自定义的类加载器，尽量减少Arthas对现有工程的侵蚀
        return loadOrDefineClassLoader(arthasCoreJarFile);
    }

    private static ClassLoader loadOrDefineClassLoader(File coreJarFile) throws Throwable {
        if (devToolClassLoader == null) {
            devToolClassLoader = new DevToolClassLoader(new URL[]{coreJarFile.toURI().toURL()});
        }
        return devToolClassLoader;
    }


    private static synchronized void main(String args, final Instrumentation inst) {
        try {
            ps.println("agent start...");
            // 开启代理服务器，接收客户端的请求
            String coreJarFile = null;
            System.out.println(args);
            args = decodeArg(args);
            int index = args.indexOf(";");
            String agentArgs = null;
            if (index != -1) {
                String[] argsArr = args.split(";");
                coreJarFile = argsArr[0];
                agentArgs = argsArr[1];
            }
            System.out.println(coreJarFile);
            final ClassLoader devClassLoader = getClassLoader(inst, new File(coreJarFile));
            // 新建启动一个新的绑定线程
            String finalAgentArgs = agentArgs;
            Thread bindingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        // 绑定
                        ps.println("开启服务");
                        bind(inst, devClassLoader, finalAgentArgs);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace(ps);
                    }
                }
            };
            bindingThread.setName("bindingThread");
            bindingThread.start();
            bindingThread.join();
        } catch (Throwable t) {
        }
    }

    private static void bind(Instrumentation inst, ClassLoader devClassLoader, String agentArgs) throws Throwable {
        // 加载代理类
        Class<?> proxyServerClass = devClassLoader.loadClass("com.binturong.diy.devTool.core.server.DevToolProxyServer");
        Object proxyServer = proxyServerClass.getMethod("getInstance", Instrumentation.class, String.class).invoke(null, inst, agentArgs);
        proxyServerClass.getMethod("start").invoke(proxyServer);
    }

    private static String decodeArg(String arg) {
        try {
            return URLDecoder.decode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }
}
