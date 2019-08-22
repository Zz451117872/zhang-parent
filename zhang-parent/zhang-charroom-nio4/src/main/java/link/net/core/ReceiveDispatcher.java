package link.net.core;

import link.packaging.ReceivePacket;

import java.io.Closeable;

public interface ReceiveDispatcher extends Closeable{

    void start();

    void stop();

    interface  ReceivePacketCallback{

        void onReceivePacketCompleted(ReceivePacket packet);
    }

}
