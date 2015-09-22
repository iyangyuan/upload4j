package org.yangyuan.upload.core.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.yangyuan.upload.config.impl.FormConfig;
import org.yangyuan.upload.config.impl.MetaFilterConfig;
import org.yangyuan.upload.core.FastUploadAbstract;
import org.yangyuan.upload.ex.FastUploadOutOfSizeException;
import org.yangyuan.upload.filter.impl.MetaFilter;
import org.yangyuan.upload.response.UploadResponseAbstract;
import org.yangyuan.upload.response.impl.SimpleUploadResponse;
import org.yangyuan.upload.util.Const;
import org.yangyuan.upload.util.StrUtil;

/**
 * 表单文件上传
 * @author 杨元
 *
 */
public class FormUpload extends FastUploadAbstract{
    
    /**
     * boundary标志，用来寻找Content-Type中的boundary
     */
    private String boundaryMark = "boundary=";
    
    /**
     * 行结束字节
     */
    private byte endOfLine = 0;
    
    /**
     * 文件名称字节码
     */
    private byte[] fileNameMark;
    
    /**
     * 双换行字节码
     */
    private byte[] doubleEnterMark;
    
    /**
     * 流元素结尾字节码
     */
    private byte[] entityEndMark;
    
    /**
     * 构造方法初始化配置
     * @param config
     */
    public FormUpload(){
        try {
            this.endOfLine = "\r".getBytes(Const.UTF8_CHARSET)[0];
            this.fileNameMark = "filename=\"".getBytes(Const.UTF8_CHARSET);
            this.doubleEnterMark = "\r\n\r\n".getBytes(Const.UTF8_CHARSET);
        } catch (UnsupportedEncodingException e){}
    }
    
    /**
     * 表单过滤上传
     * @param config
     * @return
     */
    public UploadResponseAbstract upload(MetaFilterConfig config){
        
        SimpleUploadResponse response = new SimpleUploadResponse();
        response.setSuccess(false);
        
        FileOutputStream fos = null;
        FileChannel writeChannel = null;
        ServletInputStream sis = null;
        String fullPath = Const.EMPTY;
        
        try{
            HttpServletRequest request = config.getRequest();
            MetaFilter filter = config.getFilter();
            String suffix = Const.EMPTY;
            
            //读缓冲区
            byte[] bytes = new byte[config.getBufferSize()];
            int length = 0;
            //存储缓冲区，大小为[读缓冲区]的6倍
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length*6);
            //文件大小限制
            maxFileSize = config.getMaxFileSize();
            //当前文件大小
            int currentFileSize = 0;
            
            //获取边界标记
            String boundary = getBoundaryStr(request);
            if(StrUtil.isNullOrEmpty(boundary)){
                response.setErrmsg("找不到边界(boundary)");
                return response;
            }
            
            //获取流元素结尾标记
            entityEndMark = buildEntityEndMark(boundary);
            
            //流分隔标记
            byte[] splitMark = buildBodySplitMark(boundary);
            
            //获取输入流
            sis = request.getInputStream();
            
            //找到输入流中的文件流，其他流一律丢弃
            String fileName = Const.EMPTY;
            ByteBuffer bufferShadow = null;
            while((length = sis.read(bytes)) > 0){
                buffer.put(bytes, 0, length);
                
                //读取字节数大于分隔符长度时，开始判断流类型
                if(buffer.position() > splitMark.length){
                    bufferShadow = buffer.duplicate();
                    //找文件名
                    fileName = findFileName(bufferShadow, splitMark, fileNameMark);
                    //找到文件名，文件流也找到了
                    if(StrUtil.isNotNullAndEmpty(fileName)){
                        buffer = bufferShadow;
                        break;
                    }
                }
            }
            
            //如果还木有找到文件名，说明没有文件，上传失败
            if(StrUtil.isNullOrEmpty(fileName)){
                response.setErrmsg("找不到文件");
                return response;
            }
            
            //寻找文件流
            findFileStream(buffer, sis, bytes);
            
            //将buffer尾部的有效数据整理到头部
            bufferShadow = buffer.duplicate();
            bufferShadow.clear();
            length = buffer.limit() - buffer.position();
            for(int i = 0; i < length; i++){
                bufferShadow.put(buffer.get());
            }
            buffer = bufferShadow;
            
            //保证现有字节数组长度大于文件头长度
            while((length = sis.read(bytes)) > 0){
                //将[读缓冲区]内容放入[存储缓冲区]，必须通过length进行读取，避免拿到脏数据
                buffer.put(bytes, 0, length);
                
                //判断是否达到过滤条件
                if(buffer.position() >= filter.getMaxMetaLength()){
                    //检查
                    suffix = filter.check(buffer.duplicate());
                    if(StrUtil.isNullOrEmpty(suffix)){
                        //未通过检查，断开上传
                        response.setErrmsg("文件类型不允许");
                        return response;
                    }else{
                        break;
                    }
                }
                
            }
            
            //构造输出通道
            fileName = config.getFileNameWithoutSuffix().concat(suffix);
            fullPath = config.getSavePath().concat(fileName);
            fos = new FileOutputStream(new File(fullPath));
            writeChannel = fos.getChannel();
            
            //读文件流
            while((length = sis.read(bytes)) > 0){
                
                //判断[存储缓冲区]是否足以存放[读缓冲区]的数据
                if(buffer.capacity()-buffer.position() < length){
                    //limit指向position位置，position置零
                    buffer.flip();
                    
                    //检查是否遇到文件尾
                    bufferShadow = buffer.duplicate();
                    if(isFileEnd(bufferShadow, entityEndMark)){
                        currentFileSize = safeWrite(writeChannel, bufferShadow, currentFileSize);
                        buffer = bufferShadow;
                        break;
                    }else{
                        currentFileSize = safeWrite(writeChannel, buffer, currentFileSize);
                    }
                    
                    //limit指向capacity、position置零
                    buffer.clear();
                }
                
                buffer.put(bytes, 0, length);
            }
            
            //清空缓冲区
            if(buffer.position() != buffer.limit()){
                buffer.flip();
                
                //检查是否遇到文件尾
                bufferShadow = buffer.duplicate();
                if(isFileEnd(bufferShadow, entityEndMark)){
                    currentFileSize = safeWrite(writeChannel, bufferShadow, currentFileSize);
                }else{
                    currentFileSize = safeWrite(writeChannel, buffer, currentFileSize);
                }
            }
            
            //返回完整路径
            response.setSuccess(true);
            response.setFilePath(fullPath);
        }catch(FastUploadOutOfSizeException ex){
            response.setErrmsg("文件大小超出允许的范围");
            //文件过大异常需要特殊处理：删除已经创建的文件
            //关流
            close(writeChannel, fos);
            writeChannel = null;
            fos = null;
            //删半途而废的文件
            File file = new File(fullPath);
            if(file.exists()){
                file.delete();
            }
        }catch(Exception ex){
            response.setErrmsg("未知异常:".concat(ex.toString()));
            response.setException(ex);
        }finally{
            close(writeChannel, fos);
        }
        
        return response;
        
    }
    
    /**
     * 表单非过滤上传
     * @param config
     * @return
     */
    public UploadResponseAbstract upload(FormConfig config){
        
        SimpleUploadResponse response = new SimpleUploadResponse();
        response.setSuccess(false);
        FileOutputStream fos = null;
        FileChannel writeChannel = null;
        ServletInputStream sis = null;
        String fullPath = Const.EMPTY;
        
        try{
            HttpServletRequest request = config.getRequest();
            
            //读缓冲区
            byte[] bytes = new byte[config.getBufferSize()];
            int length = 0;
            //存储缓冲区，大小为[读缓冲区]的6倍
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length*6);
            //文件大小限制
            maxFileSize = config.getMaxFileSize();
            //当前文件大小
            int currentFileSize = 0;
            
            //获取边界标记
            String boundary = getBoundaryStr(request);
            if(StrUtil.isNullOrEmpty(boundary)){
                response.setErrmsg("找不到边界(boundary)");
                return response;
            }
            
            //获取流元素结尾标记
            entityEndMark = buildEntityEndMark(boundary);
            
            //流分隔标记
            byte[] splitMark = buildBodySplitMark(boundary);
            
            //获取输入流
            sis = request.getInputStream();
            
            //找到输入流中的文件流，其他流一律丢弃
            String fileName = Const.EMPTY;
            ByteBuffer bufferShadow = null;
            while((length = sis.read(bytes)) > 0){
                buffer.put(bytes, 0, length);
                
                //读取字节数大于分隔符长度时，开始判断流类型
                if(buffer.position() > splitMark.length){
                    bufferShadow = buffer.duplicate();
                    //找文件名
                    fileName = findFileName(bufferShadow, splitMark, fileNameMark);
                    //找到文件名，文件流也找到了
                    if(StrUtil.isNotNullAndEmpty(fileName)){
                        buffer = bufferShadow;
                        break;
                    }
                }
            }
            
            //如果还木有找到文件名，说明没有文件，上传失败
            if(StrUtil.isNullOrEmpty(fileName)){
                response.setErrmsg("找不到文件");
                return response;
            }
            
            //寻找文件流
            findFileStream(buffer, sis, bytes);
            
            //构造输出通道
            fileName = config.getFileNameWithoutSuffix().concat(StrUtil.getFileNameSuffix(fileName));
            fullPath = config.getSavePath().concat(fileName);
            fos = new FileOutputStream(new File(fullPath));
            writeChannel = fos.getChannel();
            
            //将buffer尾部的有效数据整理到头部
            bufferShadow = buffer.duplicate();
            bufferShadow.clear();
            length = buffer.limit() - buffer.position();
            for(int i = 0; i < length; i++){
                bufferShadow.put(buffer.get());
            }
            buffer = bufferShadow;
            
            //读文件流
            while((length = sis.read(bytes)) > 0){
                
                //判断[存储缓冲区]是否足以存放[读缓冲区]的数据
                if(buffer.capacity()-buffer.position() < length){
                    //limit指向position位置，position置零
                    buffer.flip();
                    
                    //检查是否遇到文件尾
                    bufferShadow = buffer.duplicate();
                    if(isFileEnd(bufferShadow, entityEndMark)){
                        currentFileSize = safeWrite(writeChannel, bufferShadow, currentFileSize);
                        buffer = bufferShadow;
                        break;
                    }else{
                        currentFileSize = safeWrite(writeChannel, buffer, currentFileSize);
                    }
                    
                    //limit指向capacity、position置零
                    buffer.clear();
                }
                
                buffer.put(bytes, 0, length);
            }
            
            //清空缓冲区
            if(buffer.position() != buffer.limit()){
                buffer.flip();
                
                //检查是否遇到文件尾
                bufferShadow = buffer.duplicate();
                if(isFileEnd(bufferShadow, entityEndMark)){
                    currentFileSize = safeWrite(writeChannel, bufferShadow, currentFileSize);
                }else{
                    currentFileSize = safeWrite(writeChannel, buffer, currentFileSize);
                }
            }
            
            //返回完整路径
            response.setSuccess(true);
            response.setFilePath(fullPath);
        }catch(FastUploadOutOfSizeException ex){
            response.setErrmsg("文件大小超出允许的范围");
            //文件过大异常需要特殊处理：删除已经创建的文件
            //关流
            close(writeChannel, fos);
            writeChannel = null;
            fos = null;
            //删半途而废的文件
            File file = new File(fullPath);
            if(file.exists()){
                file.delete();
            }
        }catch(Exception ex){
            response.setErrmsg("未知异常:".concat(ex.toString()));
            response.setException(ex);
        }finally{
            close(writeChannel, fos);
        }
        
        return response;
    }
    
    /**
     * 判断输入流是否到达文件结尾
     * @param buffer
     * @param entityEndMark
     * @return
     */
    private boolean isFileEnd(ByteBuffer buffer, byte[] entityEndMark){
        boolean result = false;
        byte bt;
        int i;
        
        try{
            while(buffer.position() != buffer.limit()){
                bt = buffer.get();
                
                //有可能匹配
                if(bt == entityEndMark[0]){
                    buffer.mark();
                    
                    //逐位比较是否匹配
                    for(i = 1; i < entityEndMark.length; i++){
                        //不能存在任何不匹配
                        if(buffer.get() != entityEndMark[i]){
                            break;
                        }
                    }
                    
                    //判断是否完全匹配
                    if(i == entityEndMark.length){
                        //舍弃多余数据
                        buffer.reset();
                        buffer.flip();
                        buffer.limit(buffer.limit()-1);
                        
                        result = true;
                        break;
                    }else{
                        buffer.reset();
                    }
                }
            }
        }catch(Exception ex){}
        
        return result;
    }
    
    
    /**
     * 从流中寻找文件流
     * @param buffer  缓冲区
     * @param sis  输入流
     * @param bytes  缓冲区
     */
    private void findFileStream(ByteBuffer buffer, ServletInputStream sis, byte[] bytes){
        int length = 0;
        
        try{
            //寻找双换行
            if(!findBytesInBuffer(buffer, doubleEnterMark, false)){
                //如果没找到，直接舍弃掉当前数据
                buffer.clear();
                //从输入流里读一点
                if((length = sis.read(bytes)) > 0){
                    buffer.put(bytes, 0, length);
                    buffer.flip();
                    //递归
                    findFileStream(buffer, sis, bytes);
                }
            }
        
        }catch(Exception ex){}
        
    }
    
    /**
     * 从流中解析文件名
     * @param buffer  缓冲区
     * @param splitMark  流元素分隔字节码
     * @param fileNameMark  文件名标记字节码
     * @return
     */
    private String findFileName(ByteBuffer buffer, byte[] splitMark, byte[] fileNameMark){
        String result = Const.EMPTY;
        List<Byte> fileNameBuffer = new ArrayList<Byte>();
        byte[] fileNameBytes = null;
        byte bt;
        
        try{
            
            buffer.flip();
            
            //寻找body分隔标记
            while(findBytesInBuffer(buffer, splitMark, false)){
                //继续寻找filename标记
                if(findBytesInBuffer(buffer, fileNameMark, true)){
                    //找到filename，立即读取文件名
                    while(buffer.position() != buffer.limit()){
                        bt = buffer.get();
                        
                        //读完
                        if(bt == endOfLine){
                            break;
                        }
                        
                        fileNameBuffer.add(bt);
                    }
                    
                    fileNameBytes = new byte[fileNameBuffer.size()];
                    for(int i = 0; i < fileNameBuffer.size(); i++){
                        fileNameBytes[i] = fileNameBuffer.get(i);
                    }
                    
                    //生成文件名
                    result = new String(fileNameBytes, Const.UTF8_CHARSET);
                    //去掉末尾的双引号
                    result = result.substring(0, result.length() - 1);
                    break;
                }
            }
        }catch(Exception ex){}
        
        return result;
    }
    
    /**
     * 从buffer中寻找指定的字节码
     * @param buffer  缓冲区
     * @param content  目标字节码
     * @param inline  true 行内寻找，false 全局寻找
     * @return
     */
    private boolean findBytesInBuffer(ByteBuffer buffer, byte[] content, boolean inline){
        boolean result = false;
        byte bt = 0;
        int i;
        
        while(buffer.position() != buffer.limit()){
            bt = buffer.get();
            
            //行内匹配模式，遇到换行符号即结束
            if(inline && (bt == endOfLine)){
                break;
            }
            
            //可能匹配
            if(bt == content[0]){
                
                buffer.mark();
                
                //逐位比较是否匹配
                for(i = 1; i < content.length; i++){
                    //不能存在任何不匹配
                    if(buffer.get() != content[i]){
                        break;
                    }
                }
                
                //判断是否完全匹配
                if(i == content.length){
                    result = true;
                    break;
                }else{
                    buffer.reset();
                }
            }
        }
        
        return result;
    }
    
    /**
     * 构造流元素分隔标记
     * @param boundary
     * @return
     */
    private byte[] buildBodySplitMark(String boundary){
        byte[] bytes = null;
        String boundaryStart = Const.BOUNDARY_START.concat(boundary).concat(Const.BOUNDARY_END);
        
        try {
            bytes = boundaryStart.getBytes(Const.UTF8_CHARSET);
        } catch (UnsupportedEncodingException e) {}
        
        return bytes;
    }
    
    /**
     * 构造流元素结尾标记
     * @param boundary
     * @return
     */
    private byte[] buildEntityEndMark(String boundary){
        byte[] bytes = null;
        String entityEndMark = Const.BOUNDARY_END.concat(Const.BOUNDARY_START.concat(boundary));
        
        try {
            bytes = entityEndMark.getBytes(Const.UTF8_CHARSET);
        } catch (UnsupportedEncodingException e) {}
        
        return bytes;
    }
    
    /**
     * 获取边界分隔符
     * @param request
     * @return
     */
    private String getBoundaryStr(HttpServletRequest request){
        String result = Const.EMPTY;
        
        String contentType = request.getHeader("Content-Type");
        int boundaryIndex = contentType.indexOf(boundaryMark);
        if(boundaryIndex >= 0){
            result = contentType.substring(boundaryIndex+boundaryMark.length());
        }
        
        return result;
    }
}
