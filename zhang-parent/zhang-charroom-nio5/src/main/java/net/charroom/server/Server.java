package net.charroom.server;


import link.constants.TCPConstants;
import link.foos.FooGui;
import link.net.core.IoContext;
import link.net.impl.IoSelectorProvider;
import link.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) throws IOException {

        File cachePath = FileUtils.getCacheDir( "server");

        IoContext.setup()
                .ioProvider(new IoSelectorProvider())
                .start();

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER , cachePath);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        UDPProvider.start(TCPConstants.PORT_SERVER);

        FooGui gui = new FooGui("clink-server", new FooGui.Callback() {
            @Override
            public Object[] takeText() {
                return tcpServer.getStatusString();
            }
        });

        gui.doShow();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();

            if( str == null || str.length() == 0 ){
                break;
            }

            if( "00bye00".equalsIgnoreCase( str)){
                break;
            }

            tcpServer.broadcast(str);
        } while (!"00bye00".equalsIgnoreCase(str));

        UDPProvider.stop();
        tcpServer.stop();

        IoContext.close();
    }
}
