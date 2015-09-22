package org.yangyuan.upload.filter;

import java.nio.ByteBuffer;

/**
 * 过滤器接口
 * @author 杨元
 *
 */
public interface IFastUploadFilter {
    
    public void add(String suffix, String meta);
    
    public byte[] get(String suffix);
    
    public String check(ByteBuffer buffer);
    
    public int getMaxMetaLength();
}
