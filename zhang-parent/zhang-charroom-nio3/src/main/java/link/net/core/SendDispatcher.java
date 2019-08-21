package link.net.core;

import link.packaging.SendPacket;

import java.io.Closeable;

public interface SendDispatcher extends Closeable {

    void send(SendPacket packet);

    void cancel( SendPacket packet );
}
