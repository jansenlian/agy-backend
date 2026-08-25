package com.greatmap.agy.dwg.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSONObject;
import com.greatmap.agy.dwg.config.CadProperties;
import com.greatmap.agy.dwg.model.common.BusinessException;
import com.greatmap.agy.dwg.model.common.ErrorCode;
import com.greatmap.agy.dwg.model.dto.CadFeatureDto;
import com.greatmap.agy.dwg.model.dto.CadLayerStatDto;
import com.greatmap.agy.dwg.model.dto.CadParseOptionDto;
import com.greatmap.agy.dwg.model.entity.CadEntity;
import com.greatmap.agy.dwg.model.entity.CadLineEntity;
import com.greatmap.agy.dwg.model.entity.CadPolylineEntity;
import com.greatmap.agy.dwg.model.entity.CadTextEntity;
import com.greatmap.agy.dwg.service.CadService;
import com.greatmap.agy.dwg.service.converter.LibreDwgConverter;
import com.greatmap.agy.dwg.service.parser.DxfGroupCodeParser;
import com.greatmap.agy.dwg.service.parser.DxfGroupCodeParser.DxfParseResult;
import com.greatmap.agy.dwg.service.spatial.JtsPolygonizerService;
import com.greatmap.agy.dwg.service.spatial.JtsPolygonizerService.RawPolygon;
import com.greatmap.agy.dwg.service.spatial.SpatialAttributeMatcher;
import com.greatmap.agy.dwg.util.GeoJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CadServiceImpl implements CadService {

    private final CadProperties cadProperties;
    private final LibreDwgConverter dwgConverter;
    private final DxfGroupCodeParser dxfParser;
    private final JtsPolygonizerService polygonizerService;
    private final SpatialAttributeMatcher attributeMatcher;

    @Override
    public List<CadFeatureDto> parseToFeatures(MultipartFile file, CadParseOptionDto option) {
        File workingDir = createTempWorkingDir();
        try {
            File dxfFile = prepareDxfFile(file, workingDir);
            DxfParseResult parseResult = dxfParser.parse(dxfFile);

            List<CadPolylineEntity> polylines = parseResult.getPolylines();
            List<CadLineEntity> lines = parseResult.getLines();
            List<CadTextEntity> texts = parseResult.getTexts();

            // 图层过滤 (自动忽略空白、null及Swagger默认的"string"占位符)
            if (option != null && option.getLayers() != null && !option.getLayers().isEmpty()) {
                Set<String> filterLayers = option.getLayers().stream()
                        .filter(l -> l != null && !l.trim().isEmpty() && !"string".equalsIgnoreCase(l.trim()))
                        .collect(Collectors.toSet());
                if (!filterLayers.isEmpty()) {
                    polylines = polylines.stream().filter(p -> filterLayers.contains(p.getLayer())).collect(Collectors.toList());
                    lines = lines.stream().filter(l -> filterLayers.contains(l.getLayer())).collect(Collectors.toList());
                    texts = texts.stream().filter(t -> filterLayers.contains(t.getLayer())).collect(Collectors.toList());
                }
            }

            // 拓扑构面
            List<RawPolygon> polygons = polygonizerService.polygonize(polylines, lines, option);

            // 属性挂接
            if (option == null || option.isMatchAttributes()) {
                return attributeMatcher.matchAttributes(polygons, texts);
            } else {
                return attributeMatcher.matchAttributes(polygons, Collections.emptyList());
            }
        } finally {
            // 清理临时工作目录
            FileUtil.del(workingDir);
        }
    }

    @Override
    public JSONObject parseToGeoJson(MultipartFile file, CadParseOptionDto option) {
        List<CadFeatureDto> features = parseToFeatures(file, option);
        return GeoJsonUtil.toFeatureCollection(features);
    }

    @Override
    public List<CadLayerStatDto> getLayerStatistics(MultipartFile file) {
        File workingDir = createTempWorkingDir();
        try {
            File dxfFile = prepareDxfFile(file, workingDir);
            DxfParseResult parseResult = dxfParser.parse(dxfFile);

            Map<String, CadLayerStatDto> map = new HashMap<>();

            for (CadEntity entity : parseResult.getEntities()) {
                String layer = entity.getLayer() != null ? entity.getLayer() : "0";
                CadLayerStatDto stat = map.computeIfAbsent(layer, k -> CadLayerStatDto.builder().layerName(k).build());

                if (entity instanceof CadPolylineEntity) {
                    stat.setPolylineCount(stat.getPolylineCount() + 1);
                } else if (entity instanceof CadLineEntity) {
                    stat.setLineCount(stat.getLineCount() + 1);
                } else if (entity instanceof CadTextEntity) {
                    stat.setTextCount(stat.getTextCount() + 1);
                } else {
                    stat.setOtherCount(stat.getOtherCount() + 1);
                }
                stat.setTotalCount(stat.getTotalCount() + 1);
            }

            List<CadLayerStatDto> result = new ArrayList<>(map.values());
            result.sort((a, b) -> Integer.compare(b.getTotalCount(), a.getTotalCount()));
            return result;
        } finally {
            FileUtil.del(workingDir);
        }
    }

    @Override
    public Map<String, Object> getSystemHealth() {
        boolean toolReady = dwgConverter.isAvailable();
        String toolPath = dwgConverter.resolveExecutablePath();
        String toolVersion = toolReady ? dwgConverter.getToolVersion() : "未检测到或不可用";

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("dwg2dxfAvailable", toolReady);
        status.put("dwg2dxfVersion", toolVersion);
        status.put("dwg2dxfPath", toolPath);
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("osName", System.getProperty("os.name"));
        status.put("osArch", System.getProperty("os.arch"));
        return status;
    }

    /**
     * 处理上传文件: 如果是DWG则转成DXF,如果是DXF则直接落盘
     */
    private File prepareDxfFile(MultipartFile file, File workingDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "upload.dwg";
        }
        // 彻底净化文件名，杜绝 ../ 目录逃逸
        String safeFilename = FileUtil.getName(originalFilename);

        String ext = FileUtil.extName(safeFilename).toLowerCase();
        if (!"dwg".equals(ext) && !"dxf".equals(ext)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_FORMAT);
        }

        File uploadFile = new File(workingDir, safeFilename).getAbsoluteFile();
        FileUtil.mkdir(uploadFile.getParentFile());
        try {
            // 使用 NIO 流复制
            Files.copy(file.getInputStream(), uploadFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("保存上传文件失败: ", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件保存失败");
        }

        if ("dxf".equals(ext)) {
            return uploadFile;
        }

        // DWG 转 DXF
        File dxfOutputFile = new File(workingDir, FileUtil.mainName(safeFilename) + ".dxf").getAbsoluteFile();
        boolean ok = dwgConverter.convertDwgToDxf(uploadFile, dxfOutputFile);
        if (!ok) {
            throw new BusinessException(ErrorCode.DWG_CONVERT_FAILED);
        }
        return dxfOutputFile;
    }

    private File createTempWorkingDir() {
        String baseDir = cadProperties.getTempDir();
        File base = new File(baseDir).getAbsoluteFile();
        File dir = new File(base, UUID.randomUUID().toString().replace("-", ""));
        FileUtil.mkdir(dir);
        return dir;
    }
}
