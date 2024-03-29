package net.charroom.client;


import link.net.core.IoContext;
import link.net.impl.IoSelectorProvider;
import link.packaging.impl.FileSendPacket;
import link.utils.FileUtils;
import net.charroom.client.bean.ServerInfo;

import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {

        File cachePath = FileUtils.getCacheDir( "client");

        IoContext.setup()
                .ioProvider(new IoSelectorProvider())
                .start();

        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            TCPClient tcpClient = null;

            try {
                tcpClient = TCPClient.startWith(info , cachePath );
                if (tcpClient == null) {
                    return;
                }

                write(tcpClient);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }

        IoContext.close();
    }


    private static void write(TCPClient tcpClient) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do {

            // 键盘读取一行
            String str = input.readLine();

            if( str == null || str.length() == 0 ){
                break;
            }

            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }

            //--f url

            if( str.startsWith( "--f")){

                String[] array = str.split(" ");
                if( array.length >= 2 ){

                    String filePth = array[ 1 ];
                    File file = new File( filePth );
                    if( file.exists() && file.isFile() ){

                        FileSendPacket packet = new FileSendPacket( file );

                        tcpClient.send( packet );
                        continue;
                    }
                }
            }

            // 发送到服务器
            tcpClient.send(str);


        } while (true);
    }

}
