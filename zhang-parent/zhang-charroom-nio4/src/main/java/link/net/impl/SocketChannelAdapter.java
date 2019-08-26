package link.net.impl;

import link.net.core.IoArgs;
import link.net.core.IoProvider;
import link.net.core.Receiver;
import link.net.core.Sender;
import link.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdapter implements Sender, Receiver, Cloneable {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final IoProvider ioProvider;
    private final OnChannelStatusChangedListener listener;

    private IoArgs.IoArgsEventProcessor receiveIoEventProcessor;
    private IoArgs.IoArgsEventProcessor sendIoEventProcessor;

    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider,
                                OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;

        channel.configureBlocking(false);
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        //进行callback状态监测，判断是否处于自循环状态
        inputCallback.checkAttackNull();

        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public void setReceiveListener(IoArgs.IoArgsEventProcessor processor) {
        this.receiveIoEventProcessor = processor;
    }

    @Override
    public void setSendListener(IoArgs.IoArgsEventProcessor processor) {
        this.sendIoEventProcessor = processor;
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }

        //进行callback状态监测，判断是否处于自循环状态
        outputCallback.checkAttackNull();

        // 当前发送的数据附加到回调中
        return ioProvider.registerOutput(channel, outputCallback);
    }


    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            // 解除注册回调
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            // 关闭
            CloseUtils.close(channel);
            // 回调当前Channel已关闭
            listener.onChannelClosed(channel);
        }
    }

    private final IoProvider.HandleProviderCallback inputCallback = new IoProvider.HandleProviderCallback() {
        @Override
        protected void canProviderIo(IoArgs ioArgs) {
            if (isClosed.get()) {
                return;
            }


            IoArgs.IoArgsEventProcessor processor = receiveIoEventProcessor;

            if( ioArgs == null ) {
                ioArgs = receiveIoEventProcessor.provideIoArgs();
            }

            try {
                if( ioArgs == null ){

                    processor.onConsumeFailed( null , new IOException("args is null"));
                }else{

                    int count = ioArgs.read(channel);
                    if (count == 0) {
                        System.out.println("current write zero data");
                    }

                    if (ioArgs.remained()) {

                        attach = ioArgs;
                        //再次注册数据发送
                        ioProvider.registerInput(channel, this);
                    } else {

                        attach = null;
                        //读取完成回调
                        processor.onConsumeCompleted(ioArgs);
                    }
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }


        }
    };


    private final IoProvider.HandleProviderCallback outputCallback = new IoProvider.HandleProviderCallback() {

        @Override
        protected void canProviderIo(IoArgs ioArgs) {
            if (isClosed.get()) {
                return;
            }

            IoArgs.IoArgsEventProcessor processor = sendIoEventProcessor;

            if( ioArgs == null ) {
                ioArgs = processor.provideIoArgs();
            }

            try{

                if( ioArgs == null ){

                    processor.onConsumeFailed( null , new IOException("args is null"));
                }else {

                    int count = ioArgs.write(channel);
                    if (count == 0) {
                        System.out.println("current write zero data");
                    }

                    if (ioArgs.remained()) {

                        attach = ioArgs;
                        //再次注册数据发送
                        ioProvider.registerOutput(channel, this);
                    } else {

                        attach = null;
                        //输出完成回调
                        processor.onConsumeCompleted(ioArgs);
                    }
                }
            }catch (Exception e){

                CloseUtils.close( SocketChannelAdapter.this );
            }

        }

    };


    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
