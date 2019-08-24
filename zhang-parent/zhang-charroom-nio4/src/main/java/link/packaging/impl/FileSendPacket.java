package link.packaging.impl;

import link.packaging.SendPacket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileSendPacket extends SendPacket<FileInputStream> {

    private final File file;

    public FileSendPacket( File file ){

        this.file = file;
        this.length = file.length();
    }

    @Override
    public int available() {
        return 0;
    }

    public byte type(){

        return TYPE_STREAM_FILE;
    }

    @Override
    protected FileInputStream createStream() {
        try {
            return new FileInputStream( file );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
