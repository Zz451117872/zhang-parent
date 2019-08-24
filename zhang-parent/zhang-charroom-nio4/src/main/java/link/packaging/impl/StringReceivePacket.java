package link.packaging.impl;

import java.io.ByteArrayOutputStream;


public class StringReceivePacket extends AbsByteArrayReceivePacket<String>{


    public StringReceivePacket( int len ){
        super(len);
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }


    public String buildEntity( ByteArrayOutputStream stream ){
        return new String( stream.toByteArray() );
    }

}
