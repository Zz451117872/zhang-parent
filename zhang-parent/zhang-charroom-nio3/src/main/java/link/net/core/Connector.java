package link.net.core;

import link.net.impl.SocketChannelAdapter;
import link.net.impl.async.AsyncReceiveDispatcher;
import link.net.impl.async.AsyncSendDispatcher;
import link.packaging.ReceivePacket;
import link.packaging.SendPacket;
import link.packaging.impl.StringReceivePacket;
import link.packaging.impl.StringSendPacket;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {

    private UUID key = UUID.randomUUID();
    private SocketChannel channel;
    private Sender sender;
    private Receiver receiver;

    private SendDispatcher sendDispatcher;
    private ReceiveDispatcher receiveDispatcher;

    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;

        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, context.getIoProvider(), this);

        this.sender = adapter;
        this.receiver = adapter;

        sendDispatcher = new AsyncSendDispatcher( sender );
        receiveDispatcher = new AsyncReceiveDispatcher( receiver , receivePacketCallback);

        //启动接收
        receiveDispatcher.start();
    }

    public void send( String msg){

        SendPacket packet = new StringSendPacket( msg );

        sendDispatcher.send( packet );
    }

    @Override
    public void close() throws IOException {

        receiveDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback() {
        @Override
        public void onReceivePacketCompleted(ReceivePacket packet) {

            if( packet instanceof StringReceivePacket ){

                String msg = ( (StringReceivePacket)packet).string();
                onReceiveNewMessage( msg );
            }
        }
    };

    protected void onReceiveNewMessage( String str ) {
        System.out.println( key.toString() + ":" + str);
    }
}
