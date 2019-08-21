package link.net.core;

import link.packaging.ReceivePacket;

public interface ReceiveDispatcher {

    void start();

    void stop();

    interface  ReceivePacketCallback{

        void onReceivePacketCompleted(ReceivePacket packet);
    }

}
