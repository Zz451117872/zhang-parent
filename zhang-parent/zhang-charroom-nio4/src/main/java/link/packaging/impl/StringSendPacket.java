package link.packaging.impl;

import link.packaging.SendPacket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StringSendPacket extends BytesSendPacket {


    public StringSendPacket( String msg ){
        super( msg.getBytes() );
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }





}
