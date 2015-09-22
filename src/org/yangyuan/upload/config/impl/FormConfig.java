package org.yangyuan.upload.config.impl;

import javax.servlet.http.HttpServletRequest;

import org.yangyuan.upload.config.IFastUploadConfig;

/**
 * 表单式上传配置
 * @author 杨元
 *
 */
public class FormConfig implements IFastUploadConfig{
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
     * 不带扩展名的文件名
     * form表单上传文件可以拿到扩展名
     */
    private String fileNameWithoutSuffix;
    
    /**
     * 最大文件长度
     */
    private int maxFileSize;
    
    /**
     * 防止外部实例化
     */
    private FormConfig(){}
    
    /**
     * 定义配置
     * 
     * bufferSize默认值  8192
     * 其他参数均无默认值，请自行构造
     * 
     * @return
     */
    public static FormConfig custom(){
        return (new FormConfig()).setBufferSize(8192);
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }

    public FormConfig setRequest(HttpServletRequest request) {
        this.request = request;
        return this;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public FormConfig setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public String getSavePath() {
        return savePath;
    }

    public FormConfig setSavePath(String savePath) {
        //保存路径格式化
        if(savePath.endsWith("/")){
            this.savePath = savePath;
        }else{
            this.savePath = savePath.concat("/");
        }
        
        return this;
    }

    public String getFileNameWithoutSuffix() {
        return fileNameWithoutSuffix;
    }

    public FormConfig setFileNameWithoutSuffix(String fileNameWithoutSuffix) {
        this.fileNameWithoutSuffix = fileNameWithoutSuffix;
        return this;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public FormConfig setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
        return this;
    }

    
}
