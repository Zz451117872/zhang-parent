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

public class AsyncSendDispatcher implements SendDispatcher{

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean isSending = new AtomicBoolean();

    private IoArgs ioArgs = new IoArgs();

    private SendPacket packetTemp;

    private int total;
    private int position;

    public AsyncSendDispatcher( Sender sender ){

        this.sender = sender;
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
        total =packet.length();
        position = 0;

        sendCurrentPacket();
    }

    private void sendCurrentPacket() {

        IoArgs ioArgs = this.ioArgs;

        //开始清理
        ioArgs.startWriting();

        if( position >= total ){

            sendNextPacket();
            return;
        }else if( position == 0 ){

            //首包，需要带长度信息
            ioArgs.writeLength( total );
        }

        byte[] bytes = packetTemp.bytes();
        //把bytes的数据写入到args
        int count = ioArgs.readFrom( bytes , position );
        position += count;

        //完成封装
        ioArgs.finishWriting();

        try{
            sender.sendAsync( ioArgs , ioArgsEventListener );
        }catch (IOException e){

            closdAndNotfy();
        }
    }

    private void closdAndNotfy() {

        CloseUtils.close( this );
    }

    private final IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            //继续发送当前包
            sendCurrentPacket();
        }
    };

    @Override
    public void cancel(SendPacket packet) {


    }

    @Override
    public void close() throws IOException {
        
    }
}
