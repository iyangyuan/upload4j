package org.yangyuan.upload.core.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.servlet.ServletInputStream;

import org.yangyuan.upload.config.impl.MetaFilterConfig;
import org.yangyuan.upload.config.impl.StreamConfig;
import org.yangyuan.upload.core.FastUploadAbstract;
import org.yangyuan.upload.ex.FastUploadOutOfSizeException;
import org.yangyuan.upload.filter.impl.MetaFilter;
import org.yangyuan.upload.response.UploadResponseAbstract;
import org.yangyuan.upload.response.impl.SimpleUploadResponse;
import org.yangyuan.upload.util.Const;
import org.yangyuan.upload.util.StrUtil;

/**
 * 流文件上传
 * @author 杨元
 *
 */
public class StreamUpload extends FastUploadAbstract{
    
    /**
     * 构造方法初始化配置
     * @param config
     */
    public StreamUpload(){
    }
    
    /**
     * 流过滤上传
     * @param config
     * @return
     */
    public UploadResponseAbstract upload(MetaFilterConfig config){
        SimpleUploadResponse response = new SimpleUploadResponse();
        response.setSuccess(false);
        
        FileOutputStream fos = null;
        FileChannel writeChannel = null;
        MetaFilter filter = config.getFilter();
        String suffix = Const.EMPTY;
        String fullPath = Const.EMPTY;
        
        try{
            //读缓冲区
            byte[] bytes = new byte[config.getBufferSize()];
            int length = 0;
            //存储缓冲区，大小为[读缓冲区]的6倍
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length*6);
            //获取文件输入流
            ServletInputStream sis = config.getRequest().getInputStream();
            //文件大小限制
            maxFileSize = config.getMaxFileSize();
            //当前文件大小
            int currentFileSize = 0;
            
            //判断是否需要过滤
            if(filter != null){
                //servlet的流是边上传边解析，必须用while循环读取
                //read方法会直接覆盖bytes原有数据
                while((length = sis.read(bytes)) > 0){
                    //将[读缓冲区]内容放入[存储缓冲区]，必须通过length进行读取，避免拿到脏数据
                    buffer.put(bytes, 0, length);
                    
                    //判断是否达到过滤条件
                    if(buffer.position() >= filter.getMaxMetaLength()){
                        //检查
                        suffix = filter.check(buffer.duplicate());
                        if(StrUtil.isNullOrEmpty(suffix)){
                            //未通过检查，断开上传
                            sis.close();
                            response.setErrmsg("文件类型不允许");
                            return response;
                        }else{
                            break;
                        }
                    }
                }
            }
            
            //构造输出通道
            String fileName = config.getFileNameWithoutSuffix().concat(suffix);
            fullPath = config.getSavePath().concat(fileName);
            fos = new FileOutputStream(new File(fullPath));
            writeChannel = fos.getChannel();
            
            //继续解析
            while((length = sis.read(bytes)) > 0){
                
                //判断[存储缓冲区]是否足以存放[读缓冲区]的数据
                if(buffer.capacity()-buffer.position() < length){
                    //limit指向position位置，position置零
                    buffer.flip();
                    currentFileSize = safeWrite(writeChannel, buffer, currentFileSize);
                    //limit指向capacity、position置零
                    buffer.clear();
                }
                
                buffer.put(bytes, 0, length);
            }
            
            buffer.flip();
            currentFileSize = safeWrite(writeChannel, buffer, currentFileSize);
            
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
     * 流非过滤上传
     * @param config
     * @return
     */
    public UploadResponseAbstract upload(StreamConfig config){
        SimpleUploadResponse response = new SimpleUploadResponse();
        response.setSuccess(false);
        
        FileOutputStream fos = null;
        FileChannel writeChannel = null;
        String fullPath = Const.EMPTY;
        
        try{
            //读缓冲区
            byte[] bytes = new byte[config.getBufferSize()];
            int length = 0;
            //存储缓冲区，大小为[读缓冲区]的6倍
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length*6);
            //获取文件输入流
            ServletInputStream sis = config.getRequest().getInputStream();
            //文件大小限制
            maxFileSize = config.getMaxFileSize();
            //当前文件大小
            int currentFileSize = 0;
            
            //构造输出通道
            fullPath = config.getSavePath().concat(config.getFileNameWithSuffix());
            fos = new FileOutputStream(new File(fullPath));
            writeChannel = fos.getChannel();
            
            //解析
            while((length = sis.read(bytes)) > 0){
                
                //判断[存储缓冲区]是否足以存放[读缓冲区]的数据
                if(buffer.capacity()-buffer.position() < length){
                    //limit指向position位置，position置零
                    buffer.flip();
                    currentFileSize = safeWrite(writeChannel, buffer, currentFileSize);
                    //limit指向capacity、position置零
                    buffer.clear();
                }
                
                buffer.put(bytes, 0, length);
            }
            
            buffer.flip();
            currentFileSize = safeWrite(writeChannel, buffer, currentFileSize);
            
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
    
}
