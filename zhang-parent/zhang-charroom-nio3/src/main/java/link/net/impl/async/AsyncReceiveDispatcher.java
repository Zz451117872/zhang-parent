package link.net.impl.async;

import link.net.core.IoArgs;
import link.net.core.ReceiveDispatcher;
import link.net.core.Receiver;
import link.packaging.ReceivePacket;
import link.packaging.impl.StringReceivePacket;
import link.utils.CloseUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncReceiveDispatcher implements ReceiveDispatcher{

    private final AtomicBoolean isClosed = new AtomicBoolean( false );

    private final Receiver receiver;

    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();

    private ReceivePacket packetTemp;

    private byte[] buffer;

    private int total;

    private  int position;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.callback = callback;
        this.receiver.setReceiveListener( ioArgsEventListener );
    }

    private IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

            int receiveSeze ;
            if( packetTemp == null ){

                receiveSeze = 4;
            }else{

                receiveSeze = Math.min( total - position , args.capacity() );
            }

            //设置本次接收数据大小
            args.limit( receiveSeze );
        }

        @Override
        public void onCompleted(IoArgs args) {

            assemblePacket(args);
            //继续接收下一条数据
            registerReceive();
        }
    };

    //解析数据到packet
    private void assemblePacket( IoArgs ioArgs){

        if( packetTemp == null ){

            int length = ioArgs.readLength();
            packetTemp = new StringReceivePacket( length );
            buffer = new byte[ length ];

            total = length;
            position = 0;
        }

        int count = ioArgs.writeTo( buffer , 0 );

        if( count > 0 ){

            packetTemp.save( buffer , count );
            position += count;

            if( position == total ){

                completePacket();
                packetTemp = null;
            }
        }
    }

    //完成接收操作
    private void completePacket() {

        ReceivePacket packet = this.packetTemp;
        CloseUtils.close( packet );
        callback.onReceivePacketCompleted( packet );
    }

    @Override
    public void close() throws IOException {

        if( isClosed.compareAndSet( false , true)){

            ReceivePacket packet = this.packetTemp;
            if( packet != null ){

                packetTemp = null;
                CloseUtils.close( packet );
            }
        }
    }

    @Override
    public void start() {

        registerReceive();
    }

    private void registerReceive() {

        try{

            receiver.receiveAsync( ioArgs );
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
