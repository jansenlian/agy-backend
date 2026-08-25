package com.greatmap.agy.dwg.service;

import com.alibaba.fastjson2.JSONObject;
import com.greatmap.agy.dwg.model.dto.CadFeatureDto;
import com.greatmap.agy.dwg.model.dto.CadLayerStatDto;
import com.greatmap.agy.dwg.model.dto.CadParseOptionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CadService {

    /**
     * 解析CAD文件(DWG/DXF)并返回矢量要素列表
     */
    List<CadFeatureDto> parseToFeatures(MultipartFile file, CadParseOptionDto option);

    /**
     * 解析CAD文件(DWG/DXF)并直接返回标准GeoJSON FeatureCollection
     */
    JSONObject parseToGeoJson(MultipartFile file, CadParseOptionDto option);

    /**
     * 获取CAD文件图层统计概况
     */
    List<CadLayerStatDto> getLayerStatistics(MultipartFile file);

    /**
     * 获取系统与dwg2dxf工具链详细健康信息(包含版本、路径、操作系统等)
     */
    Map<String, Object> getSystemHealth();
}
