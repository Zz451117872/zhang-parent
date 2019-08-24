package link.net.impl.async;

import link.net.core.IoArgs;
import link.net.core.SendDispatcher;
import link.net.core.Sender;
import link.packaging.SendPacket;
import link.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispatcher ,IoArgs.IoArgsEventProcessor{

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean isClosed = new AtomicBoolean( false );
    private final AtomicBoolean isSending = new AtomicBoolean();

    private IoArgs ioArgs = new IoArgs();

    private SendPacket<?> packetTemp;

    private long total;
    private int position;
    private ReadableByteChannel packChannel;

    public AsyncSendDispatcher( Sender sender ){

        this.sender = sender;
        sender.setSendListener( this );
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs ioArgs = this.ioArgs;

        if( packChannel == null ){

            packChannel = Channels.newChannel( packetTemp.open() );
            ioArgs.limit( 4 );
            ioArgs.writeLength( (int)packetTemp.length() );
        }else{

            ioArgs.limit( (int)Math.min( ioArgs.capacity() , total - position));
            try {
                int count = ioArgs.readFrom( packChannel );

                position += count;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return ioArgs;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Throwable e) {

        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        //继续发送当前包
        sendCurrentPacket();
    }

    @Override
    public void send(SendPacket packet) {

        queue.offer( packet );
        if( isSending.compareAndSet( false , true)){
            sendNextPacket();
        }

    }

    private SendPacket takePacket(){

        SendPacket packet = queue.poll();
        if( packet != null && packet.isCanceled() ){

            return takePacket();
        }
        return packet;
    }

    private void sendNextPacket(){

        SendPacket temp = packetTemp;
        if( temp != null ){
            CloseUtils.close( temp );
        }

        SendPacket packet =  packetTemp = takePacket();

        if( packet == null ){
            isSending.set( false );
            return;
        }
        total = packet.length();
        position = 0;

        sendCurrentPacket();
    }

    private void sendCurrentPacket() {

        if( position >= total ){

            completePacket( position == total );
            sendNextPacket();
            return;
        }

        try{
            sender.postSendAsync(  );
        }catch (IOException e){

            closdAndNotfy();
        }
    }

    private void completePacket( boolean isSuccesd){

        SendPacket packet = this.packetTemp;
        if( packet == null ){
            return;
        }
        CloseUtils.close( packet );
        CloseUtils.close( packChannel);

        packetTemp = null;
        packChannel = null;
        total = 0;
        position = 0;
    }

    private void closdAndNotfy() {

        CloseUtils.close( this );
    }



    @Override
    public void cancel(SendPacket packet) {


    }

    @Override
    public void close() throws IOException {

        if( isClosed.compareAndSet( false , true)){

            isSending.set( false);
            //异常关闭导致的完成操作
            completePacket( false );
        }
    }
}
