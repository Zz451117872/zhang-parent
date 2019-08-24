package net.charroom.server.handler;


import link.net.core.Connector;
import link.packaging.Packet;
import link.packaging.ReceivePacket;
import link.utils.CloseUtils;
import link.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ClientHandler extends Connector{

    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;

    private final File cachePath;

    @Override
    protected File createReceiveFile() {
        return FileUtils.createRandomTemp( cachePath );
    }

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback,File cachePath) throws IOException {

        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        this.cachePath = cachePath;

        System.out.println("新客户端连接：" + clientInfo);
        setup( socketChannel );
    }


    public void exit() {
        CloseUtils.close( this );
        System.out.println("客户端已退出：" + clientInfo);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelf();
    }

    @Override
    protected void onReceiveNewPacket(ReceivePacket packet) {
        super.onReceiveNewPacket(packet);
        if( packet.type() == Packet.TYPE_MEMORY_STRING ){

            String string = (String)packet.entity();
            System.out.println( key.toString() + ":" + string );

            clientHandlerCallback.onNewMessageArrived( this , string );
        }
    }


    private void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler);

        // 收到消息时通知
        void onNewMessageArrived(ClientHandler handler, String msg);
    }


}
