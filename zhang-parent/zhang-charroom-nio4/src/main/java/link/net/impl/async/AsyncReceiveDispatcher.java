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

        IoArgs ioArgs =  writer.takeIoArgs();
        //一份新的IoArgs需要调用一次开始写入数据的操作
        ioArgs.startWriting();
        return ioArgs;
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
        //消费数据之前标示args数据填充完成
        //改变未可读取数据状态
        args.finishWriting();
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
