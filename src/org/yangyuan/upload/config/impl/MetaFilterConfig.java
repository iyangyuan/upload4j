package org.yangyuan.upload.config.impl;

import javax.servlet.http.HttpServletRequest;

import org.yangyuan.upload.config.IFastUploadConfig;
import org.yangyuan.upload.filter.impl.MetaFilter;

/**
 * 包含[文件头过滤器]的配置
 * @author 杨元
 *
 */
public class MetaFilterConfig implements IFastUploadConfig{
    /**
     * http 请求对象
     */
    private HttpServletRequest request;
    
    /**
     * 文件过滤器
     */
    private MetaFilter filter;
    
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
     * 扩展名自动补全
     */
    private String fileNameWithoutSuffix;
    
    /**
     * 最大文件长度
     */
    private int maxFileSize;
    
    /**
     * 防止外部实例化
     */
    private MetaFilterConfig(){}
    
    /**
     * 定义配置
     * 
     * bufferSize默认值  8192
     * 其他参数均无默认值，请自行构造
     * 
     * @return
     */
    public static MetaFilterConfig custom(){
        return (new MetaFilterConfig()).setBufferSize(8192);
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }

    public MetaFilterConfig setRequest(HttpServletRequest request) {
        this.request = request;
        return this;
    }
    
    public MetaFilter getFilter() {
        return filter;
    }

    public MetaFilterConfig setFilter(MetaFilter filter) {
        this.filter = filter;
        return this;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public MetaFilterConfig setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public String getSavePath() {
        return savePath;
    }

    public MetaFilterConfig setSavePath(String savePath) {
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

    public MetaFilterConfig setFileNameWithoutSuffix(String fileNameWithoutSuffix) {
        this.fileNameWithoutSuffix = fileNameWithoutSuffix;
        return this;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public MetaFilterConfig setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
        return this;
    }
    
    
}
