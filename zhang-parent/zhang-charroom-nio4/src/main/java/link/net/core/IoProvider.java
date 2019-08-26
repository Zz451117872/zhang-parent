package link.net.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

public interface IoProvider extends Closeable {
    boolean registerInput(SocketChannel channel, HandleProviderCallback callback);

    boolean registerOutput(SocketChannel channel, HandleProviderCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);



    abstract class HandleProviderCallback implements Runnable {

        protected  volatile  IoArgs attach;

        @Override
        public final void run() {
            canProviderIo( attach);
        }

        protected abstract void canProviderIo( IoArgs args);

        public void checkAttackNull(){
            if( attach != null ){
                throw new IllegalStateException("current state exception");
            }
        }
    }

}
