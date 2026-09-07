package com.greatmap.agy.util.web.controller;

import com.alibaba.fastjson2.JSONObject;
import com.greatmap.agy.common.domain.Result;
import com.greatmap.agy.dwg.model.dto.CadFeatureDto;
import com.greatmap.agy.dwg.model.dto.CadLayerStatDto;
import com.greatmap.agy.dwg.model.dto.CadParseOptionDto;
import com.greatmap.agy.dwg.service.CadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CAD 空间矢量转换与图斑解析控制器
 * 显式声明安全意图：需登录认证后方可使用
 */
@Tag(name = "CAD空间矢量转换接口", description = "提供DWG/DXF文件向标准GeoJSON/WKT矢量图斑解析,拓扑构面及注记属性挂接")
@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/cad")
@RequiredArgsConstructor
public class CadController {

    private final CadService cadService;

    @Operation(summary = "解析DWG/DXF直接返回标准GeoJSON FeatureCollection", description = "支持上传DWG或DXF文件,自动拓扑构面并挂接地块注记文字")
    @PostMapping(value = "/parse-to-geojson", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<JSONObject> parseToGeoJson(
            @Parameter(description = "CAD文件(.dwg或.dxf)", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "过滤图层名称集合(选填,留空提取全部图层,如DK,JZX)", example = "")
            @RequestParam(value = "layers", required = false) Set<String> layers,
            @Parameter(description = "是否自动进行JTS拓扑构面(默认true)")
            @RequestParam(value = "polygonize", required = false, defaultValue = "true") boolean polygonize,
            @Parameter(description = "是否自动挂接地块注记文字到属性(默认true)")
            @RequestParam(value = "matchAttributes", required = false, defaultValue = "true") boolean matchAttributes,
            @Parameter(description = "最小图斑过滤阈值(默认0.01平方米)")
            @RequestParam(value = "minAreaThreshold", required = false, defaultValue = "0.01") double minAreaThreshold) {

        CadParseOptionDto option = CadParseOptionDto.builder()
                .layers(layers)
                .polygonize(polygonize)
                .matchAttributes(matchAttributes)
                .minAreaThreshold(minAreaThreshold)
                .build();

        JSONObject geoJson = cadService.parseToGeoJson(file, option);
        return Result.ok(geoJson);
    }

    @Operation(summary = "解析DWG/DXF返回矢量要素对象列表(含WKT几何与属性字典)")
    @PostMapping(value = "/parse-to-features", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<List<CadFeatureDto>> parseToFeatures(
            @Parameter(description = "CAD文件(.dwg或.dxf)", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "过滤图层名称集合(选填,留空提取全部图层,如DK,JZX)", example = "")
            @RequestParam(value = "layers", required = false) Set<String> layers,
            @Parameter(description = "是否自动进行JTS拓扑构面(默认true)")
            @RequestParam(value = "polygonize", required = false, defaultValue = "true") boolean polygonize,
            @Parameter(description = "是否自动挂接地块注记文字(默认true)")
            @RequestParam(value = "matchAttributes", required = false, defaultValue = "true") boolean matchAttributes,
            @Parameter(description = "最小图斑过滤阈值(默认0.01平方米)")
            @RequestParam(value = "minAreaThreshold", required = false, defaultValue = "0.01") double minAreaThreshold) {

        CadParseOptionDto option = CadParseOptionDto.builder()
                .layers(layers)
                .polygonize(polygonize)
                .matchAttributes(matchAttributes)
                .minAreaThreshold(minAreaThreshold)
                .build();

        List<CadFeatureDto> features = cadService.parseToFeatures(file, option);
        return Result.ok(features);
    }

    @Operation(summary = "获取CAD图层统计信息与图元数量概况")
    @PostMapping(value = "/layers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<List<CadLayerStatDto>> getLayers(
            @Parameter(description = "CAD文件(.dwg或.dxf)", required = true)
            @RequestPart("file") MultipartFile file) {

        List<CadLayerStatDto> stats = cadService.getLayerStatistics(file);
        return Result.ok(stats);
    }

    @Operation(summary = "探测dwg2dxf工具链与系统就绪状态(含工具版本/路径与操作系统架构)")
    @GetMapping("/health")
    public Result<Map<String, Object>> checkHealth() {
        Map<String, Object> health = cadService.getSystemHealth();
        return Result.ok(health);
    }
}
