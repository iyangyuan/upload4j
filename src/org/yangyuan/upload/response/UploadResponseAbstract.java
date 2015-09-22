package org.yangyuan.upload.response;

/**
 * 上传返回结果封装抽象
 * @author 杨元
 *
 */
public class UploadResponseAbstract {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errmsg;
    
    /**
     * 文件保存路径
     */
    private String filePath;
    
    /**
     * 异常对象
     */
    private Exception exception;
    
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
    
    
}
