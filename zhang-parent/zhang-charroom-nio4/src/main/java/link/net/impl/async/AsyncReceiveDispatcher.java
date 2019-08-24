package link.net.impl.async;

import link.net.core.IoArgs;
import link.net.core.ReceiveDispatcher;
import link.net.core.Receiver;
import link.packaging.Packet;
import link.packaging.ReceivePacket;
import link.packaging.impl.StringReceivePacket;
import link.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncReceiveDispatcher implements ReceiveDispatcher , IoArgs.IoArgsEventProcessor,AsyncPacketWriter.PacketProvider{

    private final AtomicBoolean isClosed = new AtomicBoolean( false );

    private final Receiver receiver;

    private final ReceivePacketCallback callback;

    private final AsyncPacketWriter writer = new AsyncPacketWriter( this );

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.callback = callback;
        this.receiver.setReceiveListener( this );
    }

    @Override
    public ReceivePacket takePacket(byte type, long length, byte[] headerInfo) {
        return callback.onArrivedNewPacket( type , length );
    }

    @Override
    public void completedPacket(ReceivePacket packet, boolean isSucceed) {

        CloseUtils.close( packet );
        callback.onReceivePacketCompleted( packet );
    }

    @Override
    public void onReceivedHeartbeat() {

    }

    @Override
    public IoArgs provideIoArgs() {

        return writer.takeIoArgs();
    }

    @Override
    public void onConsumeFailed(IoArgs args, Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {

        if( isClosed.get() ){
            return;
        }

        do{
            writer.consumeIoArgs( args );
        }while ( args.remained() && !isClosed.get()  );

        registerReceive();
    }

    @Override
    public void close() throws IOException {

        if( isClosed.compareAndSet( false , true)){
            writer.close();
        }
    }

    @Override
    public void start() {

        registerReceive();
    }

    private void registerReceive() {

        try{

            receiver.postReceiveAsync( );
        }catch (Exception e){
            
            closeAndNotify();
        }
    }

    private void closeAndNotify() {

        CloseUtils.close( this );
    }

    @Override
    public void stop() {

    }
}
