package link.packaging;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.io.IOException;
import java.io.InputStream;

public abstract class SendPacket<T extends InputStream> extends  Packet<T> {

    private boolean isCanceled;

    public boolean isCanceled(){
        return isCanceled;
    }


}
