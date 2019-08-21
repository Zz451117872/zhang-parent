package link.packaging.impl;

import link.packaging.SendPacket;

import java.io.IOException;

public class StringSendPacket extends SendPacket {

    private final byte[] bytes;

    public StringSendPacket( String msg ){

        this.bytes = msg.getBytes();
        this.length = bytes.length;
    }

    @Override
    public byte[] bytes() {
        return new byte[0];
    }

    @Override
    public void close() throws IOException {

    }
}
