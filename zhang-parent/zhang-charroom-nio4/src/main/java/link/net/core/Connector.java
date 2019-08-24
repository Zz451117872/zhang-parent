package link.net.core;

import link.net.impl.SocketChannelAdapter;
import link.net.impl.async.AsyncReceiveDispatcher;
import link.net.impl.async.AsyncSendDispatcher;
import link.packaging.Packet;
import link.packaging.ReceivePacket;
import link.packaging.SendPacket;
import link.packaging.impl.BytesReceivePacket;
import link.packaging.impl.FileReceivePacket;
import link.packaging.impl.StringReceivePacket;
import link.packaging.impl.StringSendPacket;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public abstract class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {
    protected UUID key = UUID.randomUUID();
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

        //readNextMessage();
    }


    public void send( String msg){

        SendPacket packet = new StringSendPacket( msg );

        sendDispatcher.send( packet );
    }

    public void send( SendPacket packet){

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
        public ReceivePacket<?, ?> onArrivedNewPacket(byte type, long length) {

            switch ( type ){
                case Packet.TYPE_MEMORY_BYTES:
                    return new BytesReceivePacket( length );
                case Packet.TYPE_MEMORY_STRING:
                     return new StringReceivePacket( (int)length );
                case Packet.TYPE_STREAM_FILE:
                    return new FileReceivePacket( length , createReceiveFile() );
                case Packet.TYPE_STREAM_DIRECT:
                    return null;
                    default:
                        throw new UnsupportedOperationException("unsupport file type");

            }
        }



        @Override
        public void onReceivePacketCompleted(ReceivePacket packet) {

            onReceiveNewPacket( packet );
        }
    };

    protected abstract File createReceiveFile();

    protected void onReceiveNewPacket( ReceivePacket packet ) {
        System.out.println( key.toString() + " ,type:" + packet.type() + " ,length:"+ packet.length());
    }

    protected void onReceiveNewMessage( String str ) {
        System.out.println( key.toString() + ":" + str);
    }
}
