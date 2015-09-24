# 简介

`upload4j`是一款轻量级`http`文件上传框架，使用*简单*，实现*高效*，功能*专一*，摆脱传统`http`文件上传框架的繁琐。  

`upload4j`的诞生并不是为了解决所有上传需求，而是专注于*基础通用需求*。  

# upload4j带来的

+ 实现直接文件流上传、`html`表单流上传两种上传模式。
+ `io`操作部分采用`nio`机制。
+ 支持文件过滤，并且基于二进制文件头进行过滤，而非传统的文件扩展名过滤。
+ 上传文件大小不受内存大小限制。

# upload4j给不了的

+ 文件上传进度。
+ 批量上传。

# upload4j使用示例

## 直接文件流上传

*启用过滤功能*

    // 过滤器
    MetaFilter metaFilter = new MetaFilter();
    metaFilter.add(".jpg", "FFD8FF");  // 允许的文件类型，params: 文件后缀名, 文件头十六进制字符串
    Map<String, String> metaMap = new HashMap<String, String>();
    metaMap.put(".png", "89504E47");
    metaFilter.fromMap(metaMap);  // 批量添加允许的文件类型

    // 配置
    MetaFilterConfig metaFilterConfig = MetaFilterConfig.custom().setBufferSize(8192)  // 默认8192B，单位B
                                                     .setMaxFileSize(1024 * 1024)  // 限制文件最大1M，单位B
                                                     .setFilter(metaFilter)  // 过滤器
                                                     .setRequest(request)  // 从request对象的body中读取文件流
                                                     .setFileNameWithoutSuffix("123")  // 保存文件名(不带扩展名，自动识别)
                                                     .setSavePath("/home/user1/upload/img/2015/09/24/");  // 保存路径

    // 上传
    StreamUpload streamUpload = new StreamUpload();
    SimpleUploadResponse response = streamUpload.upload(metaFilterConfig);

    // 上传结果
    if(response.isSuccess()){  // 成功
        // 获取文件保存完整路径
        response.getFilePath();
        // ......
    }else{  // 失败
        // 判断失败是否可控
        if(response.getException() == null){  // 可控
            // 获取失败原因
            response.getErrmsg();
            // ......
        }else{  // 不可控
            // 直接抛出异常
            throw response.getException();
        }
    }

*不启用过滤功能*

    // 配置
    StreamConfig streamConfig = StreamConfig.custom().setBufferSize(8192)  // 默认8192B，单位B
                                                     .setMaxFileSize(1024 * 1024)  // 限制文件最大1M，单位B
                                                     .setRequest(request)  // 从request对象的body中读取文件流
                                                     .setFileNameWithSuffix("123.jpg")  // 保存完整文件名(带扩展名)
                                                     .setSavePath("/home/user1/upload/img/2015/09/24/");  // 保存路径

    // 上传
    StreamUpload streamUpload = new StreamUpload();
    SimpleUploadResponse response = streamUpload.upload(streamConfig);

    // 上传结果
    if(response.isSuccess()){  // 成功
        // 获取文件保存完整路径
        response.getFilePath();
        // ......
    }else{  // 失败
        // 判断失败是否可控
        if(response.getException() == null){  // 可控
            // 获取失败原因
            response.getErrmsg();
            // ......
        }else{  // 不可控
            // 直接抛出异常
            throw response.getException();
        }
    }

## html表单流上传

*启用过滤功能*

    // 过滤器
    MetaFilter metaFilter = new MetaFilter();
    metaFilter.add(".jpg", "FFD8FF");  // 允许的文件类型，params: 文件后缀名, 文件头十六进制字符串
    Map<String, String> metaMap = new HashMap<String, String>();
    metaMap.put(".png", "89504E47");
    metaFilter.fromMap(metaMap);  // 批量添加允许的文件类型

    // 配置
    MetaFilterConfig metaFilterConfig = MetaFilterConfig.custom().setBufferSize(8192)  // 默认8192B，单位B
                                                     .setMaxFileSize(1024 * 1024)  // 限制文件最大1M，单位B
                                                     .setFilter(metaFilter)  // 过滤器
                                                     .setRequest(request)  // 从request对象的body中读取文件流
                                                     .setFileNameWithoutSuffix("123")  // 保存文件名(不带扩展名，自动识别)
                                                     .setSavePath("/home/user1/upload/img/2015/09/24/");  // 保存路径

    // 上传
    FormUpload formUpload = new FormUpload();
    SimpleUploadResponse response = formUpload.upload(metaFilterConfig);

    // 上传结果
    if(response.isSuccess()){  // 成功
        // 获取文件保存完整路径
        response.getFilePath();
        // ......
    }else{  // 失败
        // 判断失败是否可控
        if(response.getException() == null){  // 可控
            // 获取失败原因
            response.getErrmsg();
            // ......
        }else{  // 不可控
            // 直接抛出异常
            throw response.getException();
        }
    }

*不启用过滤功能*

    // 配置
    FormConfig formConfig = FormConfig.custom().setBufferSize(8192)  // 默认8192B，单位B
                                                     .setMaxFileSize(1024 * 1024)  // 限制文件最大1M，单位B
                                                     .setRequest(request)  // 从request对象的body中读取文件流
                                                     .setFileNameWithoutSuffix("123")  // 保存文件名(不带扩展名，自动提取)
                                                     .setSavePath("/home/user1/upload/img/2015/09/24/");  // 保存路径

    // 上传
    FormUpload formUpload = new FormUpload();
    SimpleUploadResponse response = formUpload.upload(formConfig);

    // 上传结果
    if(response.isSuccess()){  // 成功
        // 获取文件保存完整路径
        response.getFilePath();
        // ......
    }else{  // 失败
        // 判断失败是否可控
        if(response.getException() == null){  // 可控
            // 获取失败原因
            response.getErrmsg();
            // ......
        }else{  // 不可控
            // 直接抛出异常
            throw response.getException();
        }
    }

