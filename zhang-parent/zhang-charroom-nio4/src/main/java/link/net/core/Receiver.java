package link.net.core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {
    boolean receiveAsync(IoArgs ioArgs) throws IOException;

    void setReceiveListener(IoArgs.IoArgsEventListener listener);
}
