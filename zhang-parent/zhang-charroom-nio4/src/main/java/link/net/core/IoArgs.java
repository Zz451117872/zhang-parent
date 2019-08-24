package link.net.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class IoArgs {

    private ByteBuffer buffer = ByteBuffer.allocate( 256 );

    private int limit = 256;

    //从bytes中读取数据
    public int readFrom(ReadableByteChannel channel)throws IOException{

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


    //写入数据到bytes中
    public int writeTo(WritableByteChannel channel )throws IOException{

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

    //从channel中读取数据
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

    //写数据到channel中
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

    //开始写入数据到ioargs
    public void startWriting(){
        buffer.clear();
        //定义容纳区间
        buffer.limit( limit );
    }

    //写完数据
    public void finishWriting(){
        buffer.flip();
    }

    //设置单次写操作的容纳区间
    public void limit( int limit ){

        this.limit = limit;
    }

    public String bufferString() {
        // 丢弃换行符
        return new String(buffer.array(), 0, buffer.position() - 1);
    }

    public void writeLength(int total) {
        startWriting();
        buffer.putInt( total );
        finishWriting();
    }

    public int capacity(){
        return buffer.capacity();
    }

    public int readLength( ){
        return buffer.getInt();
    }


    public interface IoArgsEventProcessor {

        //提供一份可消费的ioargs
        IoArgs provideIoArgs();
        //消费失败时的回调
        void onConsumeFailed(IoArgs args , Throwable e);
        //消费成功时回调
        void onConsumeCompleted(IoArgs args);
    }
}
