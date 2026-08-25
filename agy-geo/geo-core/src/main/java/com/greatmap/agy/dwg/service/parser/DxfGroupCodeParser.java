package com.greatmap.agy.dwg.service.parser;

import com.greatmap.agy.dwg.model.entity.CadEntity;
import com.greatmap.agy.dwg.model.entity.CadLineEntity;
import com.greatmap.agy.dwg.model.entity.CadPolylineEntity;
import com.greatmap.agy.dwg.model.entity.CadTextEntity;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DxfGroupCodeParser {

    private static final Pattern MTEXT_FORMAT_PATTERN = Pattern.compile("\\\\[A-Za-z0-9]+;?|\\\\[fFHhCcQqWwTtAaPp][^;]*;?");

    public static class DxfParseResult {
        private final List<CadEntity> entities = new ArrayList<>();

        public List<CadEntity> getEntities() {
            return entities;
        }

        public List<CadPolylineEntity> getPolylines() {
            List<CadPolylineEntity> list = new ArrayList<>();
            for (CadEntity entity : entities) {
                if (entity instanceof CadPolylineEntity) {
                    list.add((CadPolylineEntity) entity);
                }
            }
            return list;
        }

        public List<CadLineEntity> getLines() {
            List<CadLineEntity> list = new ArrayList<>();
            for (CadEntity entity : entities) {
                if (entity instanceof CadLineEntity) {
                    list.add((CadLineEntity) entity);
                }
            }
            return list;
        }

        public List<CadTextEntity> getTexts() {
            List<CadTextEntity> list = new ArrayList<>();
            for (CadEntity entity : entities) {
                if (entity instanceof CadTextEntity) {
                    list.add((CadTextEntity) entity);
                }
            }
            return list;
        }
    }

    /**
     * 解析DXF文件并提取全量CAD实体(含LWPOLYLINE, POLYLINE+VERTEX, LINE, TEXT, MTEXT, ATTRIB, ARC, CIRCLE, XData及ArcGIS XDictionary/XRecord扩展属性)
     *
     * @param dxfFile DXF文件
     * @return 解析结果集合
     */
    public DxfParseResult parse(File dxfFile) {
        DxfParseResult result = new DxfParseResult();
        if (dxfFile == null || !dxfFile.exists()) {
            return result;
        }

        Charset charset = detectCharset(dxfFile);
        log.info("开始解析DXF文件[字符集:{}]: {}", charset.name(), dxfFile.getName());

        // 实体句柄 -> 字典句柄映射表
        Map<String, String> entityXDictMap = new HashMap<>();
        // 实体句柄 -> CadEntity
        Map<String, CadEntity> handleEntityMap = new HashMap<>();
        // 字典句柄 -> {字段名: 目标句柄}
        Map<String, Map<String, String>> dicts = new HashMap<>();
        // XRecord句柄 -> 属性值列表
        Map<String, List<String>> xrecords = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dxfFile), charset))) {
            String line;
            String currentSection = null;

            String currentEntityType = null;
            String currentLayer = "0";
            String currentHandle = null;
            Integer currentColor = null;
            Map<String, List<String>> currentXdata = new LinkedHashMap<>();
            String currentAppId = null;

            // LWPOLYLINE / POLYLINE 属性
            List<Coordinate> polyVertices = new ArrayList<>();
            boolean polyClosed = false;
            Double currentX = null;
            Double currentY = null;
            double currentZ = 0.0;

            // LINE 属性
            Double lineX1 = null, lineY1 = null, lineZ1 = 0.0;
            Double lineX2 = null, lineY2 = null, lineZ2 = 0.0;

            // TEXT / MTEXT / ATTRIB 属性
            StringBuilder textContent = new StringBuilder();
            Double textX = null, textY = null, textZ = 0.0;
            double textHeight = 2.5;
            double textRotation = 0.0;

            // ARC / CIRCLE 属性
            Double centerX = null, centerY = null, centerZ = 0.0;
            double radius = 0.0;
            double startAngle = 0.0, endAngle = 360.0;

            // 经典POLYLINE内嵌VERTEX模式跟踪
            boolean insidePolylineWithVertices = false;

            // OBJECTS 区块跟踪
            String currentObjType = null;
            String currentObjHandle = null;
            String currentDictKey = null;

            while ((line = reader.readLine()) != null) {
                String codeStr = line.trim();
                String value = reader.readLine();
                if (value == null) {
                    break;
                }
                value = value.trim();

                int code;
                try {
                    code = Integer.parseInt(codeStr);
                } catch (NumberFormatException e) {
                    continue;
                }

                // 探测段起止
                if (code == 0) {
                    String upperVal = value.toUpperCase();

                    if ("SECTION".equals(upperVal)) {
                        continue;
                    } else if ("ENDSEC".equals(upperVal)) {
                        if ("ENTITIES".equals(currentSection)) {
                            commitEntity(result, handleEntityMap, currentEntityType, currentLayer, currentHandle, currentColor, currentXdata,
                                    polyVertices, polyClosed, currentX, currentY, currentZ,
                                    lineX1, lineY1, lineZ1, lineX2, lineY2, lineZ2,
                                    textContent.toString(), textX, textY, textZ, textHeight, textRotation,
                                    centerX, centerY, centerZ, radius, startAngle, endAngle);
                            insidePolylineWithVertices = false;
                        }
                        currentSection = null;
                        currentEntityType = null;
                        currentObjType = null;
                        continue;
                    } else if ("EOF".equals(upperVal)) {
                        break;
                    }

                    // 判断是否进入某段
                    if (currentSection == null) {
                        continue;
                    }

                    if ("ENTITIES".equals(currentSection)) {
                        // 处理经典POLYLINE的VERTEX与SEQEND子节点
                        if (insidePolylineWithVertices) {
                            if ("VERTEX".equals(upperVal)) {
                                if (currentX != null && currentY != null) {
                                    polyVertices.add(new Coordinate(currentX, currentY, currentZ));
                                    currentX = null;
                                    currentY = null;
                                }
                                continue;
                            } else if ("SEQEND".equals(upperVal)) {
                                if (currentX != null && currentY != null) {
                                    polyVertices.add(new Coordinate(currentX, currentY, currentZ));
                                }
                                commitEntity(result, handleEntityMap, "POLYLINE", currentLayer, currentHandle, currentColor, currentXdata,
                                        polyVertices, polyClosed, null, null, 0.0,
                                        null, null, 0.0, null, null, 0.0,
                                        "", null, null, 0.0, 2.5, 0.0,
                                        null, null, 0.0, 0.0, 0.0, 360.0);
                                insidePolylineWithVertices = false;
                                currentEntityType = null;
                                polyVertices = new ArrayList<>();
                                currentXdata = new LinkedHashMap<>();
                                currentAppId = null;
                                continue;
                            }
                        }

                        // 提交上一个常规实体
                        if (currentEntityType != null) {
                            commitEntity(result, handleEntityMap, currentEntityType, currentLayer, currentHandle, currentColor, currentXdata,
                                    polyVertices, polyClosed, currentX, currentY, currentZ,
                                    lineX1, lineY1, lineZ1, lineX2, lineY2, lineZ2,
                                    textContent.toString(), textX, textY, textZ, textHeight, textRotation,
                                    centerX, centerY, centerZ, radius, startAngle, endAngle);
                        }

                        // 初始化新图元
                        currentEntityType = upperVal;
                        currentLayer = "0";
                        currentHandle = null;
                        currentColor = null;
                        currentXdata = new LinkedHashMap<>();
                        currentAppId = null;
                        polyVertices = new ArrayList<>();
                        polyClosed = false;
                        currentX = null;
                        currentY = null;
                        currentZ = 0.0;
                        lineX1 = null; lineY1 = null; lineZ1 = 0.0;
                        lineX2 = null; lineY2 = null; lineZ2 = 0.0;
                        textContent.setLength(0);
                        textX = null; textY = null; textZ = 0.0;
                        textHeight = 2.5;
                        textRotation = 0.0;
                        centerX = null; centerY = null; centerZ = 0.0;
                        radius = 0.0; startAngle = 0.0; endAngle = 360.0;

                        if ("POLYLINE".equals(currentEntityType)) {
                            insidePolylineWithVertices = true;
                        }
                    } else if ("OBJECTS".equals(currentSection)) {
                        // 初始化OBJECTS中的对象
                        currentObjType = upperVal;
                        currentObjHandle = null;
                        currentDictKey = null;
                    }
                } else {
                    if (code == 2 && ("ENTITIES".equalsIgnoreCase(value) || "OBJECTS".equalsIgnoreCase(value))) {
                        currentSection = value.toUpperCase();
                        continue;
                    }

                    if (currentSection == null) {
                        continue;
                    }

                    // 1. ENTITIES 区块解析
                    if ("ENTITIES".equals(currentSection) && currentEntityType != null) {
                        if (code == 1001) {
                            currentAppId = value;
                            currentXdata.computeIfAbsent(currentAppId, k -> new ArrayList<>());
                        } else if (code >= 1000 && code <= 1071 && currentAppId != null) {
                            currentXdata.get(currentAppId).add(value);
                        } else if (code == 8) {
                            currentLayer = value;
                        } else if (code == 5) {
                            currentHandle = value;
                        } else if (code == 62) {
                            currentColor = tryParseInt(value);
                        } else if (code == 360 && currentHandle != null) {
                            entityXDictMap.put(currentHandle, value);
                        }

                        // 实体具体字段解析
                        switch (currentEntityType) {
                            case "LWPOLYLINE":
                                if (code == 70) {
                                    int flag = tryParseInt(value);
                                    polyClosed = (flag & 1) == 1;
                                } else if (code == 10) {
                                    if (currentX != null && currentY != null) {
                                        polyVertices.add(new Coordinate(currentX, currentY, currentZ));
                                        currentY = null;
                                    }
                                    currentX = tryParseDouble(value);
                                } else if (code == 20) {
                                    currentY = tryParseDouble(value);
                                } else if (code == 38) {
                                    currentZ = tryParseDouble(value);
                                }
                                break;

                            case "POLYLINE":
                                if (code == 70) {
                                    int flag = tryParseInt(value);
                                    polyClosed = (flag & 1) == 1;
                                } else if (code == 10) {
                                    currentX = tryParseDouble(value);
                                } else if (code == 20) {
                                    currentY = tryParseDouble(value);
                                } else if (code == 30) {
                                    currentZ = tryParseDouble(value);
                                }
                                break;

                            case "LINE":
                                if (code == 10) lineX1 = tryParseDouble(value);
                                else if (code == 20) lineY1 = tryParseDouble(value);
                                else if (code == 30) lineZ1 = tryParseDouble(value);
                                else if (code == 11) lineX2 = tryParseDouble(value);
                                else if (code == 21) lineY2 = tryParseDouble(value);
                                else if (code == 31) lineZ2 = tryParseDouble(value);
                                break;

                            case "TEXT":
                            case "MTEXT":
                            case "ATTRIB":
                            case "ATTDEF":
                                if (code == 1 || code == 3) {
                                    textContent.append(value);
                                } else if (code == 10) {
                                    textX = tryParseDouble(value);
                                } else if (code == 20) {
                                    textY = tryParseDouble(value);
                                } else if (code == 30) {
                                    textZ = tryParseDouble(value);
                                } else if (code == 40) {
                                    textHeight = tryParseDouble(value);
                                } else if (code == 50) {
                                    textRotation = tryParseDouble(value);
                                }
                                break;

                            case "ARC":
                                if (code == 10) centerX = tryParseDouble(value);
                                else if (code == 20) centerY = tryParseDouble(value);
                                else if (code == 30) centerZ = tryParseDouble(value);
                                else if (code == 40) radius = tryParseDouble(value);
                                else if (code == 50) startAngle = tryParseDouble(value);
                                else if (code == 51) endAngle = tryParseDouble(value);
                                break;

                            case "CIRCLE":
                                if (code == 10) centerX = tryParseDouble(value);
                                else if (code == 20) centerY = tryParseDouble(value);
                                else if (code == 30) centerZ = tryParseDouble(value);
                                else if (code == 40) radius = tryParseDouble(value);
                                break;

                            case "INSERT":
                                if (code == 10) textX = tryParseDouble(value);
                                else if (code == 20) textY = tryParseDouble(value);
                                else if (code == 30) textZ = tryParseDouble(value);
                                else if (code == 2) textContent.append(value);
                                break;

                            default:
                                break;
                        }
                    }

                    // 2. OBJECTS 区块解析 (解析 ArcGIS Extension Dictionary 与 XRecord 属性字典)
                    else if ("OBJECTS".equals(currentSection) && currentObjType != null) {
                        if (code == 5) {
                            currentObjHandle = value;
                            if ("DICTIONARY".equals(currentObjType)) {
                                dicts.putIfAbsent(currentObjHandle, new LinkedHashMap<>());
                            } else if ("XRECORD".equals(currentObjType)) {
                                xrecords.putIfAbsent(currentObjHandle, new ArrayList<>());
                            }
                        } else if ("DICTIONARY".equals(currentObjType) && currentObjHandle != null) {
                            if (code == 3) {
                                currentDictKey = value;
                            } else if ((code == 350 || code == 360) && currentDictKey != null) {
                                dicts.computeIfAbsent(currentObjHandle, k -> new LinkedHashMap<>()).put(currentDictKey, value);
                                currentDictKey = null;
                            }
                        } else if ("XRECORD".equals(currentObjType) && currentObjHandle != null) {
                            if (code == 1 || code == 2 || code == 3 || code == 40 || code == 50 || code == 70 || code == 90 || code == 300) {
                                xrecords.computeIfAbsent(currentObjHandle, k -> new ArrayList<>()).add(value);
                            }
                        }
                    }
                }
            }

            // 属性装配: 将 OBJECTS 区块中的 ArcGIS / AutoCAD 扩展属性字典挂接到实体上
            attachXDictionaryAttributes(entityXDictMap, handleEntityMap, dicts, xrecords);

        } catch (Exception e) {
            log.error("解析DXF文件发生异常: ", e);
        }

        log.info("DXF解析完成, 提取实体总数: {}, 多段线: {}, 线段: {}, 注记文字: {}",
                result.getEntities().size(),
                result.getPolylines().size(),
                result.getLines().size(),
                result.getTexts().size());
        return result;
    }

    private void attachXDictionaryAttributes(Map<String, String> entityXDictMap,
                                            Map<String, CadEntity> handleEntityMap,
                                            Map<String, Map<String, String>> dicts,
                                            Map<String, List<String>> xrecords) {
        for (Map.Entry<String, String> entry : entityXDictMap.entrySet()) {
            String entityHandle = entry.getKey();
            String dictHandle = entry.getValue();
            CadEntity entity = handleEntityMap.get(entityHandle);
            if (entity == null) {
                continue;
            }

            Map<String, String> rootDict = dicts.get(dictHandle);
            if (rootDict == null) {
                continue;
            }

            // 递归或逐级遍历字典
            for (Map.Entry<String, String> dictEntry : rootDict.entrySet()) {
                String key = dictEntry.getKey();
                String targetHandle = dictEntry.getValue();

                // 1. 如果指向嵌套字典 (如 ESRI_Attributes -> 5A)
                if (dicts.containsKey(targetHandle)) {
                    Map<String, String> subDict = dicts.get(targetHandle);
                    for (Map.Entry<String, String> subEntry : subDict.entrySet()) {
                        String fieldName = subEntry.getKey();
                        String xrecHandle = subEntry.getValue();
                        List<String> vals = xrecords.get(xrecHandle);
                        if (vals != null && !vals.isEmpty()) {
                            entity.getXdata().computeIfAbsent("ESRI", k -> new ArrayList<>()).add(fieldName + "=" + vals.get(0));
                        }
                    }
                } else {
                    // 2. 直接指向 XRecord
                    List<String> vals = xrecords.get(targetHandle);
                    if (vals != null && !vals.isEmpty()) {
                        entity.getXdata().computeIfAbsent("XRECORD", k -> new ArrayList<>()).add(key + "=" + vals.get(0));
                    }
                }
            }
        }
    }

    private void commitEntity(DxfParseResult result, Map<String, CadEntity> handleEntityMap,
                              String entityType, String layer, String handle, Integer color, Map<String, List<String>> xdata,
                              List<Coordinate> polyVertices, boolean polyClosed, Double lastX, Double lastY, double lastZ,
                              Double lineX1, Double lineY1, double lineZ1, Double lineX2, Double lineY2, double lineZ2,
                              String text, Double textX, Double textY, double textZ, double textHeight, double textRotation,
                              Double centerX, Double centerY, double centerZ, double radius, double startAngle, double endAngle) {
        if (entityType == null) {
            return;
        }

        CadEntity createdEntity = null;

        if ("LWPOLYLINE".equals(entityType) || "POLYLINE".equals(entityType)) {
            if (lastX != null && lastY != null) {
                polyVertices.add(new Coordinate(lastX, lastY, lastZ));
            }
            if (polyVertices.size() >= 2) {
                CadPolylineEntity polyline = new CadPolylineEntity();
                polyline.setEntityType(entityType);
                polyline.setLayer(layer);
                polyline.setHandle(handle);
                polyline.setColor(color);
                polyline.setXdata(new LinkedHashMap<>(xdata));
                polyline.setVertices(new ArrayList<>(polyVertices));
                polyline.setClosed(polyClosed);
                result.getEntities().add(polyline);
                createdEntity = polyline;
            }
        } else if ("LINE".equals(entityType)) {
            if (lineX1 != null && lineY1 != null && lineX2 != null && lineY2 != null) {
                CadLineEntity line = new CadLineEntity();
                line.setEntityType(entityType);
                line.setLayer(layer);
                line.setHandle(handle);
                line.setColor(color);
                line.setXdata(new LinkedHashMap<>(xdata));
                line.setStartPoint(new Coordinate(lineX1, lineY1, lineZ1));
                line.setEndPoint(new Coordinate(lineX2, lineY2, lineZ2));
                result.getEntities().add(line);
                createdEntity = line;
            }
        } else if ("TEXT".equals(entityType) || "MTEXT".equals(entityType) || "ATTRIB".equals(entityType) || "ATTDEF".equals(entityType)) {
            if (textX != null && textY != null && text != null && !text.trim().isEmpty()) {
                CadTextEntity textEntity = new CadTextEntity();
                textEntity.setEntityType(entityType);
                textEntity.setLayer(layer);
                textEntity.setHandle(handle);
                textEntity.setColor(color);
                textEntity.setXdata(new LinkedHashMap<>(xdata));
                textEntity.setPosition(new Coordinate(textX, textY, textZ));
                textEntity.setText(cleanMText(text));
                textEntity.setHeight(textHeight);
                textEntity.setRotation(textRotation);
                if (!textEntity.getText().isEmpty()) {
                    result.getEntities().add(textEntity);
                    createdEntity = textEntity;
                }
            }
        } else if ("ARC".equals(entityType) && centerX != null && centerY != null && radius > 0) {
            List<Coordinate> arcCoords = discretizeArc(centerX, centerY, centerZ, radius, startAngle, endAngle, 16);
            if (arcCoords.size() >= 2) {
                CadPolylineEntity polyline = new CadPolylineEntity();
                polyline.setEntityType(entityType);
                polyline.setLayer(layer);
                polyline.setHandle(handle);
                polyline.setColor(color);
                polyline.setXdata(new LinkedHashMap<>(xdata));
                polyline.setVertices(arcCoords);
                polyline.setClosed(false);
                result.getEntities().add(polyline);
                createdEntity = polyline;
            }
        } else if ("CIRCLE".equals(entityType) && centerX != null && centerY != null && radius > 0) {
            List<Coordinate> circleCoords = discretizeArc(centerX, centerY, centerZ, radius, 0.0, 360.0, 32);
            if (circleCoords.size() >= 3) {
                CadPolylineEntity polyline = new CadPolylineEntity();
                polyline.setEntityType(entityType);
                polyline.setLayer(layer);
                polyline.setHandle(handle);
                polyline.setColor(color);
                polyline.setXdata(new LinkedHashMap<>(xdata));
                polyline.setVertices(circleCoords);
                polyline.setClosed(true);
                result.getEntities().add(polyline);
                createdEntity = polyline;
            }
        }

        if (createdEntity != null && handle != null) {
            handleEntityMap.put(handle, createdEntity);
        }
    }

    private List<Coordinate> discretizeArc(double cx, double cy, double cz, double r, double startAngle, double endAngle, int segments) {
        List<Coordinate> coords = new ArrayList<>();
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(endAngle);
        if (endRad < startRad) {
            endRad += 2 * Math.PI;
        }
        double step = (endRad - startRad) / segments;
        for (int i = 0; i <= segments; i++) {
            double angle = startRad + i * step;
            double x = cx + r * Math.cos(angle);
            double y = cy + r * Math.sin(angle);
            coords.add(new Coordinate(x, y, cz));
        }
        return coords;
    }

    /**
     * 清洗MTEXT格式控制符(例如 \\P, \\f..., \\A1;, \\H... 等)
     */
    public static String cleanMText(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw;
        s = s.replace("\\P", " ").replace("\\p", " ");
        s = MTEXT_FORMAT_PATTERN.matcher(s).replaceAll("");
        s = s.replace("{", "").replace("}", "");
        s = s.replace("\\\\", "\\").replace("\\~", " ");
        return s.trim();
    }

    /**
     * 智能探测DXF字符集: 优先UTF-8, 遇到多字节乱码/无法解码回退GBK
     */
    private Charset detectCharset(File file) {
        // 先尝试以 UTF-8 读取验证是否为合法 UTF-8
        try (FileInputStream in = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            char[] buf = new char[4096];
            int read = reader.read(buf);
            if (read > 0) {
                String sample = new String(buf, 0, read);
                // 如果是标准 UTF-8 且无非法字符，优先采用 UTF-8
                if (isValidUtf8(sample)) {
                    return StandardCharsets.UTF_8;
                }
            }
        } catch (Exception ignored) {
        }

        // 包含传统中文编码标记时使用 GBK
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buf = new byte[4096];
            int read = in.read(buf);
            if (read > 0) {
                String sample = new String(buf, 0, read, StandardCharsets.ISO_8859_1);
                if (sample.contains("ANSI_936") || sample.contains("GB2312") || sample.contains("GBK")) {
                    return Charset.forName("GBK");
                }
            }
        } catch (Exception ignored) {
        }

        return StandardCharsets.UTF_8;
    }

    private boolean isValidUtf8(String sample) {
        // 检查是否包含乱码替换符 \uFFFD
        return !sample.contains("\uFFFD");
    }

    private double tryParseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int tryParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
