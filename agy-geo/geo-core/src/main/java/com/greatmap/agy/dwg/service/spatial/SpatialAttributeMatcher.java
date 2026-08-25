package com.greatmap.agy.dwg.service.spatial;

import com.greatmap.agy.dwg.model.dto.CadFeatureDto;
import com.greatmap.agy.dwg.model.entity.CadTextEntity;
import com.greatmap.agy.dwg.service.spatial.JtsPolygonizerService.RawPolygon;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SpatialAttributeMatcher {

    private static final GeometryFactory GF = new GeometryFactory();
    private static final WKTWriter WKT_WRITER = new WKTWriter(2);
    private static final Set<String> GENERIC_LAYERS = Set.of("0", "DEFPOINTS", "DEFAULT", "LAYER1");

    /**
     * 将注记文字与扩展属性挂接至对应图斑多边形上,生成标准Feature DTO
     *
     * @param polygons 拓扑构面多边形
     * @param texts    注记文字实体
     * @return 挂接属性后的要素列表
     */
    public List<CadFeatureDto> matchAttributes(List<RawPolygon> polygons, List<CadTextEntity> texts) {
        List<CadFeatureDto> features = new ArrayList<>();
        if (polygons == null || polygons.isEmpty()) {
            return features;
        }

        // 构建R-Tree空间索引
        STRtree strTree = new STRtree();
        if (texts != null && !texts.isEmpty()) {
            for (CadTextEntity text : texts) {
                if (text.getPosition() != null) {
                    Point pt = GF.createPoint(text.getPosition());
                    strTree.insert(pt.getEnvelopeInternal(), text);
                }
            }
            strTree.build();
        }

        int index = 1;
        for (RawPolygon rawPoly : polygons) {
            Geometry geom = rawPoly.getGeometry();
            PreparedGeometry prepGeom = PreparedGeometryFactory.prepare(geom);

            List<String> matchedTexts = new ArrayList<>();
            String mainDkmc = null;

            if (texts != null && !texts.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<CadTextEntity> candidates = strTree.query(geom.getEnvelopeInternal());

                for (CadTextEntity textEntity : candidates) {
                    Point pt = GF.createPoint(textEntity.getPosition());
                    if (prepGeom.contains(pt) || prepGeom.intersects(pt)) {
                        String text = textEntity.getText();
                        matchedTexts.add(text);
                        if (mainDkmc == null) {
                            mainDkmc = text;
                        }
                    }
                }
            }

            // 若无图内注记文字，但图层名具备业务语义(如'光山宗地2022-01')，则将图层名作为候选地块标识
            if (mainDkmc == null && rawPoly.getLayer() != null && !GENERIC_LAYERS.contains(rawPoly.getLayer().trim().toUpperCase())) {
                mainDkmc = rawPoly.getLayer();
            }

            // 构造Feature DTO
            String featureId = "feat_" + (rawPoly.getSourceHandle() != null ? rawPoly.getSourceHandle() : String.valueOf(index++));
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", featureId);
            props.put("layer", rawPoly.getLayer());
            props.put("area", Math.round(rawPoly.getArea() * 100.0) / 100.0);
            if (mainDkmc != null) {
                props.put("dkmc", mainDkmc);
            }
            if (!matchedTexts.isEmpty()) {
                props.put("annotations", matchedTexts);
            }

            // 注入 XData / ArcGIS XDictionary 扩展实体属性
            if (rawPoly.getXdata() != null && !rawPoly.getXdata().isEmpty()) {
                props.put("xdata", rawPoly.getXdata());
                
                // 南方 CASS 扩展属性识别
                if (rawPoly.getXdata().containsKey("SOUTH")) {
                    List<String> southValues = rawPoly.getXdata().get("SOUTH");
                    if (southValues != null && !southValues.isEmpty()) {
                        props.put("cassCode", southValues.get(0));
                    }
                }

                // ArcGIS ESRI / 键值对 XData 自动平铺为顶层属性
                for (Map.Entry<String, List<String>> entry : rawPoly.getXdata().entrySet()) {
                    List<String> vals = entry.getValue();
                    if (vals != null) {
                        for (String val : vals) {
                            if (val != null && val.contains("=")) {
                                String[] kv = val.split("=", 2);
                                if (kv.length == 2 && !kv[0].trim().isEmpty()) {
                                    props.put(kv[0].trim(), kv[1].trim());
                                }
                            }
                        }
                    }
                }

                // 若属性表中含有 DKMC / dkmc 字段，优先覆盖为主地块名称
                if (props.containsKey("DKMC") && props.get("DKMC") != null && !String.valueOf(props.get("DKMC")).trim().isEmpty()) {
                    mainDkmc = String.valueOf(props.get("DKMC")).trim();
                    props.put("dkmc", mainDkmc);
                } else if (props.containsKey("dkmc") && props.get("dkmc") != null && !String.valueOf(props.get("dkmc")).trim().isEmpty()) {
                    mainDkmc = String.valueOf(props.get("dkmc")).trim();
                }
            }

            CadFeatureDto dto = CadFeatureDto.builder()
                    .id(featureId)
                    .dkmc(mainDkmc)
                    .layer(rawPoly.getLayer())
                    .geometryType(geom.getGeometryType())
                    .wkt(WKT_WRITER.write(geom))
                    .area(rawPoly.getArea())
                    .properties(props)
                    .build();

            features.add(dto);
        }

        log.info("空间属性挂接完成, 处理图斑数: {}, 成功挂接地块标识数: {}",
                features.size(),
                features.stream().filter(f -> f.getDkmc() != null).count());

        return features;
    }
}
