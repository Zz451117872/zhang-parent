package link.packaging.impl;

import link.packaging.SendPacket;

import java.io.ByteArrayInputStream;

import static link.packaging.Packet.TYPE_MEMORY_BYTES;

/**
 * 纯Byte数组发送包
 */
public class BytesSendPacket extends SendPacket<ByteArrayInputStream> {
    private final byte[] bytes;

    public BytesSendPacket(byte[] bytes) {
        this.bytes = bytes;
        this.length = bytes.length;
    }

    @Override
    public int available() {
        return 0;
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_BYTES;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }

}
