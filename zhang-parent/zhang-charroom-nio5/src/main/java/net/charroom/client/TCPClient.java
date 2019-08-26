package net.charroom.client;


import link.net.core.Connector;
import link.packaging.Packet;
import link.packaging.ReceivePacket;
import link.utils.CloseUtils;
import link.utils.FileUtils;
import net.charroom.client.bean.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TCPClient extends Connector{

    private final File cachePath ;

    public TCPClient(SocketChannel socketChannel , File cachePath) throws IOException {
        this.cachePath = cachePath;
        setup( socketChannel );
    }

    @Override
    protected File createReceiveFile() {

        return FileUtils.createRandomTemp( cachePath );
    }

    public void exit() {
        CloseUtils.close(this);
    }


    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println( "连接已关闭，无法读取数据");
    }

    public static TCPClient startWith(ServerInfo info , File cachePath ) throws IOException {
        SocketChannel socket =  SocketChannel.open();

        // 连接本地，端口2000；超时时间3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()));

        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + socket.getLocalAddress().toString());
        System.out.println("服务器信息：" + socket.getRemoteAddress().toString());

        try {
            return new TCPClient(socket , cachePath );
        } catch (Exception e) {
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }

        return null;
    }

    @Override
    protected void onReceiveNewPacket(ReceivePacket packet) {
        super.onReceiveNewPacket(packet);
        if( packet.type() == Packet.TYPE_MEMORY_STRING ){

            String string = (String)packet.entity();
            //System.out.println( key.toString() + ":" + string );
        }
    }
}
