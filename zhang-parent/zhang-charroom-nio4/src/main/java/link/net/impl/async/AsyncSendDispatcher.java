package link.net.impl.async;

import link.net.core.IoArgs;
import link.net.core.SendDispatcher;
import link.net.core.Sender;
import link.packaging.SendPacket;
import link.utils.CloseUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispatcher ,IoArgs.IoArgsEventProcessor,AsyncPacketReader.PacketProvider{

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean isClosed = new AtomicBoolean( false );
    private final AtomicBoolean isSending = new AtomicBoolean();

    private final AsyncPacketReader reader = new AsyncPacketReader( this);

    public AsyncSendDispatcher( Sender sender ){

        this.sender = sender;
        sender.setSendListener( this );
    }

    @Override
    public SendPacket takePacket() {
        SendPacket packet = queue.poll();
        if( packet == null  ){
            //队列为空，取消发送状态
            isSending.set( false );
            return null;
        }

        if(  packet.isCanceled() ){
            //已取消，不用发送
            return takePacket();
        }
        return packet;
    }

    @Override
    public void completedPacket(SendPacket packet, boolean isSucceed) {

        CloseUtils.close( packet );
    }

    @Override
    public IoArgs provideIoArgs() {

        return  isClosed.get() ? null : reader.fillData();
    }

    @Override
    public void onConsumeFailed(IoArgs args, Throwable e) {

        e.printStackTrace();

        synchronized ( isSending ){
            isSending.set( false);
        }
        requestSend();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        //继续发送当前包
        synchronized (isSending){
            isSending.set(false);
        }
        requestSend();
    }

    @Override
    public void send(SendPacket packet) {

        queue.offer( packet );
        requestSend();

    }

    //请求网络进行发送
    private void requestSend() {

        synchronized (isSending){
            if( isSending.get() || isClosed.get() ){
                return;
            }

            if( reader.requestTakePacket() ){
                try{
                    boolean isSucceed = sender.postSendAsync(  );

                    if( isSucceed ){

                        isSending.set( true );
                    }
                }catch (IOException e){

                    closdAndNotfy();
                }
            }
        }

    }


    private void closdAndNotfy() {

        CloseUtils.close( this );
    }

    private final Object queueLock = new Object();

    @Override
    public void cancel(SendPacket packet) {

        boolean ret = queue.remove( packet );

        if( ret ){
            packet.cancel();
            return;
        }

        reader.cancel( packet );
    }

    @Override
    public void close() throws IOException {

        if( isClosed.compareAndSet( false , true)){

            //异常关闭导致的完成操作
            reader.close( );
            queue.clear();

            synchronized (isSending) {
                isSending.set(false);
            }

        }
    }
}
