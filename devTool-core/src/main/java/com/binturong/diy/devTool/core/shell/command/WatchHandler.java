package com.binturong.diy.devTool.core.shell.command;

import com.binturong.cli.core.CommandLine;
import com.binturong.cli.core.command.Command;
import com.binturong.cli.core.handler.CommandHandler;
import com.binturong.cli.core.option.Option;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Objects;

/**
 * @author zhulin
 * @date 2023-07-06 17:15
 */
public class WatchHandler implements CommandHandler {

    private Instrumentation inst;
    public WatchHandler(Instrumentation inst) {
        this.inst = inst;
    }

    @Override
    public boolean support(String commandType) {
        return commandType.equals("watch");
    }

    @Override
    public String handle(CommandLine commandLine) {
    //     处理
        Command command = commandLine.getCommand();
        Option classOption = command.getOption("class");
        String clazz = classOption.getValues().get(0);
        Option methodOption = command.getOption("method");
        String method = methodOption.getValues().get(0);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                //    重写指定的方法，在方法的入口处和返回处增加监控的代码
                if (!Objects.equals(classBeingRedefined.getName(), clazz)) {
                    return classfileBuffer;
                }
                ClassReader classReader = new ClassReader(classfileBuffer);
                ClassWriter classWriter = new ClassWriter(0);
                WatcherAdapter watcherAdapter = new WatcherAdapter(classWriter, method);
                classReader.accept(watcherAdapter, 0);
                return classWriter.toByteArray();
            }
        }, true);
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class allLoadedClass : allLoadedClasses) {
            if (allLoadedClass.getName().equals(clazz)) {
                try {
                    inst.retransformClasses(allLoadedClass);
                } catch (UnmodifiableClassException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        return "对" + clazz + " " + method + "进行监控";
    }
}
