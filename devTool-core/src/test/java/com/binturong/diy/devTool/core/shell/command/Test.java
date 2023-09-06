package com.binturong.diy.devTool.core.shell.command;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author zhulin
 * @date 2023-09-01 14:27
 */
public class Test {
    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader("com.binturong.diy.devTool.core.shell.command.WatchTest");
        ClassWriter classWriter = new ClassWriter(0);
        WatcherAdapter watcherAdapter = new WatcherAdapter(classWriter, "printf");
        classReader.accept(watcherAdapter, 0);
        byte[] bytes = classWriter.toByteArray();
        File file = new File("WatchTestProxy.class");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bytes);
        MyClassLoader myClassLoader = new MyClassLoader();
        Class c = myClassLoader.defineClass("com.binturong.diy.devTool.core.shell.command.WatchTest", bytes);
        System.out.println(c.getName());
        System.out.println(c.getFields());
    }
}
