package com.binturong.diy.devTool.core;

import com.binturong.diy.devTool.core.config.AgentConfig;
import com.binturong.diy.devTool.core.util.PidUtil;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author zhulin
 * @date 2023-08-31 10:55
 */
public class Bootstrap {
    public Bootstrap(String[] args) throws Exception {
        AgentConfig configure = new AgentConfig();
        configure.setAgentPath("D:\\个人\\diy-devTool\\devTool-agent-attach\\target\\devTool-agent.jar");
        configure.setCorePath("D:\\个人\\diy-devTool\\devTool-core\\target\\devTool-core.jar");
        getPid(configure);
        configure.setServerPort(10622);
        attachAgent(configure);
        // 启动客户端，连接代理服务

    }


    public static void main(String[] args) throws Exception {
        new Bootstrap(args);
    }

    private void attachAgent(AgentConfig configure) throws Exception {
        VirtualMachineDescriptor virtualMachineDescriptor = null;
        // 根据被监控的pid得到对应的虚拟机描述对象
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            String pid = descriptor.id();
            if (pid.equals(Long.toString(configure.getJavaPid()))) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }

        VirtualMachine virtualMachine = null;
        try {
            if (null == virtualMachineDescriptor) { // 使用 attach(String pid) 这种方式
                virtualMachine = VirtualMachine.attach("" + configure.getJavaPid());
            } else {
                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
            }
            // 获取代理jar包的位置
            configure.setCorePath(encodeArg(configure.getCorePath()));
            String configureStr = configure.toString();
            try {
                // 加载代理
                virtualMachine.loadAgent(configure.getAgentPath(),configure.getCorePath() + ";" + encodeArg(configureStr));
                System.out.println("12323");
            } catch (IOException e) {
                throw new RuntimeException("代理异常!!");
            }
        } finally {
            if (null != virtualMachine) {
                virtualMachine.detach();
            }
        }
    }

    private void getPid(AgentConfig configure) {
        Map<Long, String> processMap = listProcessByJps();
        if (processMap.isEmpty()) {
            System.out.println("Can not find java process. Try to run `jps` command lists the instrumented Java HotSpot VMs on the target system.");
            return;
        }
        System.out.println("Found existing java process, please choose one and input the serial number of the process, eg : 1. Then hit ENTER.");
        int count = 1;
        for (String process : processMap.values()) {
            if (count == 1) {
                System.out.println("* [" + count + "]: " + process);
            } else {
                System.out.println("  [" + count + "]: " + process);
            }
            count++;
        }
        // 读取选择
        long pid = 0;
        String line = new Scanner(System.in).nextLine();
        if (line.trim().isEmpty()) {
            // get the first process id
            pid =  processMap.keySet().iterator().next();
        }
        int choice = new Scanner(line).nextInt();
        if (choice <= 0 || choice > processMap.size()) {
            pid =  processMap.keySet().iterator().next();
        }

        Iterator<Long> idIter = processMap.keySet().iterator();
        for (int i = 1; i <= choice; ++i) {
            if (i == choice) {
                pid = idIter.next();
            }
            idIter.next();
        }
        configure.setJavaPid(pid);
    }

    private Map<Long, String> listProcessByJps() {
        Map<Long, String> result = new HashMap<>();
        try {
            Process jpsProcess = Runtime.getRuntime().exec("jps -l");
            ArrayList<String> processList = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jpsProcess.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                processList.add(line);
            }
            jpsProcess.waitFor();

            for (String processStr : processList) {
                String[] strings = processStr.trim().split("\\s+");
                if (strings.length < 1) {
                    continue;
                }
                long pid = Long.parseLong(strings[0]);
                if (pid == PidUtil.getCurrentPid()) {
                    continue;
                }
                if (strings.length >= 2 && isJpsProcess(strings[1])) {
                    continue;
                }
                result.put(pid, processStr);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static boolean isJpsProcess(String mainClassName) {
        return "sun.tools.jps.Jps".equals(mainClassName) || "jdk.jcmd/sun.tools.jps.Jps".equals(mainClassName);
    }

    private static String encodeArg(String arg) {
        try {
            return URLEncoder.encode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }
}
