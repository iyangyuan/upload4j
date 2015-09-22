package org.yangyuan.upload.util;


public class StrUtil {
    
    /**
     * 字符串不为null且不为空
     * 
     * @param str
     * @return
     */
    public static boolean isNotNullAndEmpty(String str) {
        return (str != null) && (!"".equals(str));
    }

    /**
     * 字符串为null或者为空
     * 
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return !isNotNullAndEmpty(str);
    }
    
    /**
     * 从文件名中提取扩展名
     * @param fileName
     * @return
     */
    public static String getFileNameSuffix(String fileName){
        String result = "";
        
        if(isNotNullAndEmpty(fileName)){
            int index = fileName.lastIndexOf(".");
            result = fileName.substring(index);
        }
        
        return result;
    }
}
