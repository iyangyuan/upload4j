package org.yangyuan.upload.filter.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.yangyuan.upload.filter.IFastUploadFilter;
import org.yangyuan.upload.util.StrUtil;

/**
 * 文件头过滤器
 * @author 杨元
 *
 */
public class MetaFilter implements IFastUploadFilter{
    
    private Map<String, Object> metas;
    
    /**
     * 最大文件头字节长度
     */
    private int maxMetaLength = 0;
    
    public MetaFilter(){
        metas = new HashMap<String, Object>();
    }
    
    /**
     * 添加过滤条件
     * @param suffix 文件后缀名
     * @param meta 文件头十六进制字符串
     */
    public void add(String suffix, String meta){
        if(StrUtil.isNotNullAndEmpty(suffix) && 
                StrUtil.isNotNullAndEmpty(meta) && 
                meta.length()%2 == 0){
            //获取文件头字节码
            byte[] bytes = hexToByte(meta);
            //保存
            metas.put(suffix, bytes);
            
            //记录最大长度
            if(maxMetaLength < bytes.length){
                maxMetaLength = bytes.length;
            }
        }
    }
    
    /**
     * 获取某个过滤条件
     * @param suffix
     * @return
     */
    public byte[] get(String suffix){
        return (byte[])metas.get(suffix);
    }
    
    /**
     * 获取条件集合
     * @return
     */
    public Set<String> keySet(){
        return metas.keySet();
    }
    
    /**
     * 从已有的map导入过滤条件
     * @param map 包含了过滤条件的map集合
     * @return
     */
    public static MetaFilter fromMap(Map<String, String> map){
        MetaFilter metaFilter = new MetaFilter();
        
        if(map != null){
            for(String key : map.keySet()){
                metaFilter.add(key, map.get(key));
            }
        }
        
        return metaFilter;
    }
    
    /**
     * 过滤检查
     * @param buffer 数据buffer
     * @return 检查通过返回文件头对应的后缀名，反之返回空
     */
    public String check(ByteBuffer buffer){
        
        buffer.flip();
        
        for(String key : metas.keySet()){
            byte[] metaByte = (byte[])metas.get(key);
            byte[] bytes = new byte[metaByte.length];
            
            buffer.get(bytes, 0, bytes.length);
            
            if(Arrays.equals(metaByte, bytes)){
                return key;
            }
            
            buffer.rewind();
        }
        
        return "";
    }
    
    /**
     * 获取最大文件头字节长度
     * @return
     */
    public int getMaxMetaLength() {
        return maxMetaLength;
    }

    /**
     * 十六进制字符串转字节数组
     * @param hex
     * @return
     */
    private byte[] hexToByte(String hex){
        int words = hex.length()/2;
        int start;
        int end;
        byte[] bytes = new byte[words];
        for(int i = 0; i < bytes.length; i++){
            start = i*2;
            end = start +2;
            bytes[i] = Integer.valueOf(hex.substring(start, end), 16).byteValue();
        }
        
        return bytes;
    }
}
