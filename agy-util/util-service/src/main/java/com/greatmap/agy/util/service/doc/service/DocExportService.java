package com.greatmap.agy.util.service.doc.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Swagger / OpenAPI 接口文档导出服务接口
 *
 * @author AGY
 */
public interface DocExportService {

    /**
     * 根据 Swagger / OpenAPI JSON 在线 URL 解析接口表格数据
     *
     * @param swaggerUrl 资源地址
     * @return 接口数据模型 Map
     */
    Map<String, Object> parseFromUrl(String swaggerUrl);

    /**
     * 根据 Swagger / OpenAPI JSON 字符串解析接口表格数据
     *
     * @param jsonStr JSON 字符串
     * @return 接口数据模型 Map
     */
    Map<String, Object> parseFromJsonString(String jsonStr);

    /**
     * 根据上传的 Swagger JSON 文件解析接口表格数据
     *
     * @param jsonFile 上传的文件
     * @return 接口数据模型 Map
     */
    Map<String, Object> parseFromFile(MultipartFile jsonFile);
}
