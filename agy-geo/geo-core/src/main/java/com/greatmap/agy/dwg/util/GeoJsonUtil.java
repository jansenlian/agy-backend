package com.greatmap.agy.dwg.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.greatmap.agy.dwg.model.dto.CadFeatureDto;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import java.util.List;

public class GeoJsonUtil {

    private static final WKTReader WKT_READER = new WKTReader();
    private static final GeoJsonWriter GEO_JSON_WRITER = new GeoJsonWriter();

    static {
        GEO_JSON_WRITER.setEncodeCRS(false);
    }

    /**
     * 将CadFeatureDto列表转换为标准RFC 7946 GeoJSON FeatureCollection格式JSON对象
     */
    public static JSONObject toFeatureCollection(List<CadFeatureDto> features) {
        JSONObject fc = new JSONObject();
        fc.put("type", "FeatureCollection");

        JSONArray featureArray = new JSONArray();
        if (features != null) {
            for (CadFeatureDto dto : features) {
                JSONObject feat = new JSONObject();
                feat.put("type", "Feature");
                feat.put("id", dto.getId());

                // 解析WKT为GeoJSON Geometry
                try {
                    Geometry geometry = WKT_READER.read(dto.getWkt());
                    String geomJsonStr = GEO_JSON_WRITER.write(geometry);
                    feat.put("geometry", JSONObject.parseObject(geomJsonStr));
                } catch (Exception e) {
                    feat.put("geometry", null);
                }

                JSONObject props = new JSONObject();
                if (dto.getProperties() != null) {
                    props.putAll(dto.getProperties());
                }
                props.put("dkmc", dto.getDkmc());
                props.put("layer", dto.getLayer());
                props.put("area", dto.getArea());
                feat.put("properties", props);

                featureArray.add(feat);
            }
        }
        fc.put("features", featureArray);
        return fc;
    }
}
