package com.greatmap.agy.dwg.service.spatial;

import com.greatmap.agy.dwg.model.dto.CadParseOptionDto;
import com.greatmap.agy.dwg.model.entity.CadLineEntity;
import com.greatmap.agy.dwg.model.entity.CadPolylineEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class JtsPolygonizerService {

    private static final GeometryFactory GF = new GeometryFactory();

    @Data
    public static class RawPolygon {
        private Geometry geometry;
        private String layer;
        private String sourceHandle;
        private double area;
        private Map<String, List<String>> xdata = new LinkedHashMap<>();
    }

    /**
     * 将CAD线要素拓扑构建为Polygon多边形
     *
     * @param polylines 多段线列表
     * @param lines     线段列表
     * @param option    解析配置选项
     * @return 构面完成的多边形列表
     */
    public List<RawPolygon> polygonize(List<CadPolylineEntity> polylines, List<CadLineEntity> lines, CadParseOptionDto option) {
        List<RawPolygon> result = new ArrayList<>();
        double minArea = option != null ? option.getMinAreaThreshold() : 0.01;

        // 按图层分别构面,避免不同图层的线混在一起互相切割
        Map<String, List<CadPolylineEntity>> polylineByLayer = new HashMap<>();
        for (CadPolylineEntity p : polylines) {
            polylineByLayer.computeIfAbsent(p.getLayer(), k -> new ArrayList<>()).add(p);
        }

        Map<String, List<CadLineEntity>> lineByLayer = new HashMap<>();
        for (CadLineEntity l : lines) {
            lineByLayer.computeIfAbsent(l.getLayer(), k -> new ArrayList<>()).add(l);
        }

        Set<String> allLayers = new HashSet<>();
        allLayers.addAll(polylineByLayer.keySet());
        allLayers.addAll(lineByLayer.keySet());

        for (String layer : allLayers) {
            List<CadPolylineEntity> layerPolylines = polylineByLayer.getOrDefault(layer, Collections.emptyList());
            List<CadLineEntity> layerLines = lineByLayer.getOrDefault(layer, Collections.emptyList());

            Polygonizer polygonizer = new Polygonizer();

            // 1. 处理多段线
            for (CadPolylineEntity poly : layerPolylines) {
                List<Coordinate> v = poly.getVertices();
                if (v == null || v.size() < 2) {
                    continue;
                }

                boolean isClosed = poly.isClosed() || v.get(0).equals2D(v.get(v.size() - 1));

                // 自身已闭合且点数>=3的多段线,可直接转成标准Polygon
                if (isClosed && v.size() >= 3) {
                    try {
                        List<Coordinate> ringCoords = new ArrayList<>(v);
                        if (!ringCoords.get(0).equals2D(ringCoords.get(ringCoords.size() - 1))) {
                            ringCoords.add(ringCoords.get(0));
                        }
                        LinearRing shell = GF.createLinearRing(ringCoords.toArray(new Coordinate[0]));
                        Polygon polygon = GF.createPolygon(shell);
                        if (polygon.isValid() && polygon.getArea() >= minArea) {
                            RawPolygon rp = new RawPolygon();
                            rp.setGeometry(polygon);
                            rp.setLayer(layer);
                            rp.setSourceHandle(poly.getHandle());
                            rp.setArea(polygon.getArea());
                            if (poly.getXdata() != null) {
                                rp.setXdata(new LinkedHashMap<>(poly.getXdata()));
                            }
                            result.add(rp);
                            continue;
                        }
                    } catch (Exception e) {
                        // 闭合构造失败则回退至polygonizer
                    }
                }

                // 未闭合多段线: 加入Polygonizer参与拓扑构网
                Coordinate[] coords = v.toArray(new Coordinate[0]);
                try {
                    LineString ls = GF.createLineString(coords);
                    polygonizer.add(ls);
                } catch (Exception ignored) {
                }
            }

            // 2. 处理散列线段
            for (CadLineEntity line : layerLines) {
                if (line.getStartPoint() != null && line.getEndPoint() != null) {
                    try {
                        LineString ls = GF.createLineString(new Coordinate[]{line.getStartPoint(), line.getEndPoint()});
                        polygonizer.add(ls);
                    } catch (Exception ignored) {
                    }
                }
            }

            // 3. 执行JTS拓扑构面
            @SuppressWarnings("unchecked")
            Collection<Polygon> builtPolygons = polygonizer.getPolygons();
            for (Polygon p : builtPolygons) {
                if (p.isValid() && p.getArea() >= minArea) {
                    RawPolygon rp = new RawPolygon();
                    rp.setGeometry(p);
                    rp.setLayer(layer);
                    rp.setArea(p.getArea());
                    result.add(rp);
                }
            }
        }

        log.info("拓扑构面处理完成, 生成有效图斑多边形: {}个", result.size());
        return result;
    }
}
