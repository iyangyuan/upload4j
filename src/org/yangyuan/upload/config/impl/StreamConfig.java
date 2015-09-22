package org.yangyuan.upload.config.impl;

import javax.servlet.http.HttpServletRequest;

import org.yangyuan.upload.config.IFastUploadConfig;

/**
 * 流式上传配置
 * @author 杨元
 *
 */
public class StreamConfig implements IFastUploadConfig{
    /**
     * http 请求对象
     */
    private HttpServletRequest request;
    
    /**
     * 内存缓冲区大小（字节）
     */
    private int bufferSize;
    
    /**
     * 文件保存路径
     */
    private String savePath;
    
    /**
     * 带扩展名的文件名
     * 由于没有filter，扩展名无法自动补全
     */
    private String fileNameWithSuffix;
    
    /**
     * 最大文件长度
     */
    private int maxFileSize;
    
    /**
     * 防止外部实例化
     */
    private StreamConfig(){}
    
    /**
     * 定义配置
     * 
     * bufferSize默认值  8192
     * 其他参数均无默认值，请自行构造
     * 
     * @return
     */
    public static StreamConfig custom(){
        return (new StreamConfig()).setBufferSize(8192);
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }

    public StreamConfig setRequest(HttpServletRequest request) {
        this.request = request;
        return this;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public StreamConfig setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public String getSavePath() {
        return savePath;
    }

    public StreamConfig setSavePath(String savePath) {
        //保存路径格式化
        if(savePath.endsWith("/")){
            this.savePath = savePath;
        }else{
            this.savePath = savePath.concat("/");
        }
        
        return this;
    }

    public String getFileNameWithSuffix() {
        return fileNameWithSuffix;
    }

    public StreamConfig setFileNameWithSuffix(String fileNameWithSuffix) {
        this.fileNameWithSuffix = fileNameWithSuffix;
        return this;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public StreamConfig setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
        return this;
    }
    
}
