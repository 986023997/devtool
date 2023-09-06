package com.binturong.diy.devTool.client;

import com.binturong.diy.devTool.client.telnet.TelnetClient;
import com.binturong.shell.core.terminal.Terminal;
import com.binturong.shell.core.terminal.impl.SimpleTerminal;

/**
 * @author zhulin
 * @date 2023-09-05 10:40
 */
public class TelnetConsole {

    public static void main(String[] args) throws Exception {
        Terminal terminal = new SimpleTerminal();
        terminal.start();
        TelnetClient telnetClient = new TelnetClient();
        telnetClient.connect();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> telnetClient.shutdownGracefully()));
        while (true) {
            String commandLine = terminal.reader().read();
            if (commandLine.equals("exit")) {
                break;
            }
            String out = telnetClient.send(commandLine);
            terminal.writer().write(out);
        }
    }
}
