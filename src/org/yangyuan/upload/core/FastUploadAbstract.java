package org.yangyuan.upload.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.yangyuan.upload.ex.FastUploadOutOfSizeException;

/**
 * 上传抽象
 * @author 杨元
 *
 */
public abstract class FastUploadAbstract {
    
    protected int maxFileSize = 0;
    
    /**
     * 安全写入封装，防止文件超长
     * @param writeChannel
     * @param buffer
     * @throws IOException
     */
    protected int safeWrite(FileChannel writeChannel, ByteBuffer buffer, int currentFileSize) throws IOException, FastUploadOutOfSizeException{
        currentFileSize = currentFileSize + (buffer.limit() - buffer.position());
        
        //判断是否超长
        if(this.maxFileSize == 0 || currentFileSize <= this.maxFileSize){
            writeChannel.write(buffer);
        }else{
            throw new FastUploadOutOfSizeException();
        }
        
        return currentFileSize;
    }
    
    /**
     * 通用流关闭方法
     * @param writeChannel
     * @param fos
     */
    protected void close(FileChannel writeChannel, FileOutputStream fos){
        //关闭通道
        if(writeChannel != null){
            try {
                writeChannel.close();
            } catch (IOException e) {
                writeChannel = null;
            }
        }
        //关闭流
        if(fos != null){
            try {
                fos.close();
            } catch (IOException e) {
                fos = null;
            }
        }
    }
    
}
