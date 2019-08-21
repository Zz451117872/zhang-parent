package link.net.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class IoArgs {
    private byte[] byteBuffer = new byte[256];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    private int limit = 256;

    public int readFrom( byte[] bytes , int offset ){

        int size = Math.min( bytes.length - offset, buffer.remaining() );

        buffer.put( bytes , offset , size );
        return  size;
    }

    public int writeTo( byte[] bytes , int offset ){

        int size = Math.min( bytes.length - offset, buffer.remaining() );

        buffer.get( bytes , offset , size );
        return  size;
    }

    public int read(SocketChannel channel) throws IOException {

        startWriting();

        int bytesProduced = 0;

        while ( buffer.hasRemaining() ){

            int len = channel.read( buffer );
            if( len < 0 ){
                throw  new EOFException();
            }
            bytesProduced += len;
        }

        finishWriting();

        return bytesProduced;
    }

    public int write(SocketChannel channel) throws IOException {

        int bytesProduced = 0;

        while ( buffer.hasRemaining() ){

            int len = channel.write( buffer );
            if( len < 0 ){
                throw  new EOFException();
            }
            bytesProduced += len;
        }

        return bytesProduced;
    }

    public void startWriting(){
        buffer.clear();
        buffer.limit( limit );
    }

    public void finishWriting(){
        buffer.flip();
    }

    public void limit( int limit ){

        this.limit = limit;
    }

    public String bufferString() {
        // 丢弃换行符
        return new String(byteBuffer, 0, buffer.position() - 1);
    }

    public void writeLength(int total) {

        buffer.putInt( total );

    }

    public int readLength( ){
        return buffer.getInt();
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }
}
