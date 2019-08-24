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

public class AsyncReceiveDispatcher implements ReceiveDispatcher , IoArgs.IoArgsEventProcessor{

    private final AtomicBoolean isClosed = new AtomicBoolean( false );

    private final Receiver receiver;

    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();

    private ReceivePacket<?,?> packetTemp;

    private WritableByteChannel packetChannel;

    private long total;

    private  int position;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.callback = callback;
        this.receiver.setReceiveListener( this );
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;

        int receiveSeze ;
        if( packetTemp == null ){

            receiveSeze = 4;
        }else{

            receiveSeze = (int)Math.min( total - position , args.capacity() );
        }

        //设置本次接收数据大小
        args.limit( receiveSeze );
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {

        assemblePacket( args );
        registerReceive();
    }


    //解析数据到packet
    private void assemblePacket( IoArgs ioArgs){

        if( packetTemp == null ){

            int length = ioArgs.readLength();

            byte type = length > 200 ? Packet.TYPE_STREAM_FILE : Packet.TYPE_MEMORY_STRING;

            packetTemp = callback.onArrivedNewPacket( type , length );

            packetChannel = Channels.newChannel( packetTemp.open() );

            total = length;
            position = 0;
        }

        try {
            int count = ioArgs.writeTo( packetChannel);

            position += count;

            if( position == total ){

                completePacket( true );
                packetTemp = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            completePacket( false );
        }


    }

    //完成接收操作
    private void completePacket( boolean isSucceed) {

        ReceivePacket packet = this.packetTemp;
        packetTemp = null;
        CloseUtils.close( packet );

        WritableByteChannel channel = this.packetChannel;
        CloseUtils.close( channel );
        packetChannel = null;

        if( packet != null ) {
            callback.onReceivePacketCompleted(packet);
        }
    }

    @Override
    public void close() throws IOException {

        if( isClosed.compareAndSet( false , true)){
            completePacket( false );
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
