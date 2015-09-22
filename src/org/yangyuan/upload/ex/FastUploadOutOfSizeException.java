package org.yangyuan.upload.ex;

/**
 * 上传文件大小超过规定的最大字节数异常
 * @author 杨元
 *
 */
@SuppressWarnings("serial")
public class FastUploadOutOfSizeException extends RuntimeException{
    public FastUploadOutOfSizeException(){
        super("File out of size!");
    }
}
