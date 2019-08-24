package link.packaging;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.io.Closeable;
import java.io.IOException;

public abstract class Packet<Stream extends Closeable> implements Closeable{

    protected  long length;
    private Stream stream;

    // BYTES 类型
    public static final byte TYPE_MEMORY_BYTES = 1;
    // String 类型
    public static final byte TYPE_MEMORY_STRING = 2;
    // 文件 类型
    public static final byte TYPE_STREAM_FILE = 3;
    // 长链接流 类型
    public static final byte TYPE_STREAM_DIRECT = 4;

    public abstract byte type();

    public long length(){
        return length;
    }

    protected abstract  Stream createStream();

    protected   void closeStream( Stream stream)throws IOException {
        stream.close();
    }

    public final Stream open() {
        if( stream == null ){

            stream = createStream();
        }

        return stream;
    }

    @Override
    public final void close() throws IOException {

        if( stream != null ){
            closeStream( stream );
            stream = null;
        }
    }

    //头部额外信息，用于携带额外的校验信息等
    public byte[] headerInfo(){
        return null;
    }

}
