package link.net.core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {
    boolean postReceiveAsync() throws IOException;

    void setReceiveListener(IoArgs.IoArgsEventProcessor processor );
}
