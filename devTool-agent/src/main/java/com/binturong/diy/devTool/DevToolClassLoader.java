package com.binturong.diy.devTool;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author zhulin
 * @date 2023-08-31 14:15
 */
public class DevToolClassLoader extends URLClassLoader {
    public DevToolClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        // 优先从parent（SystemClassLoader）里加载系统类，避免抛出ClassNotFoundException
        if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
            return super.loadClass(name, resolve);
        }
        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            // ignore
        }
        return super.loadClass(name, resolve);
    }
}
