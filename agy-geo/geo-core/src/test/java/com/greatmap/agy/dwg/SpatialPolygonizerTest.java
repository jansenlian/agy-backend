package com.greatmap.agy.dwg;

import com.greatmap.agy.dwg.model.dto.CadFeatureDto;
import com.greatmap.agy.dwg.model.dto.CadParseOptionDto;
import com.greatmap.agy.dwg.model.entity.CadLineEntity;
import com.greatmap.agy.dwg.model.entity.CadPolylineEntity;
import com.greatmap.agy.dwg.model.entity.CadTextEntity;
import com.greatmap.agy.dwg.service.spatial.JtsPolygonizerService;
import com.greatmap.agy.dwg.service.spatial.JtsPolygonizerService.RawPolygon;
import com.greatmap.agy.dwg.service.spatial.SpatialAttributeMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpatialPolygonizerTest {

    private final JtsPolygonizerService polygonizerService = new JtsPolygonizerService();
    private final SpatialAttributeMatcher attributeMatcher = new SpatialAttributeMatcher();

    @Test
    public void testPolygonizeAndAttributeMatch() {
        // 1. 构造一个闭合矩形地块 (0,0) -> (100,0) -> (100,100) -> (0,100)
        CadPolylineEntity polyline = new CadPolylineEntity();
        polyline.setLayer("DK");
        polyline.setClosed(true);
        polyline.setVertices(List.of(
                new Coordinate(0.0, 0.0),
                new Coordinate(100.0, 0.0),
                new Coordinate(100.0, 100.0),
                new Coordinate(0.0, 100.0)
        ));

        // 2. 构造用4条独立Line围成的另一个矩形 (200,200) -> (300,200) -> (300,300) -> (200,300)
        List<CadLineEntity> lines = new ArrayList<>();
        lines.add(createLine("DK", 200, 200, 300, 200));
        lines.add(createLine("DK", 300, 200, 300, 300));
        lines.add(createLine("DK", 300, 300, 200, 300));
        lines.add(createLine("DK", 200, 300, 200, 200));

        CadParseOptionDto option = CadParseOptionDto.builder().minAreaThreshold(1.0).build();
        List<RawPolygon> rawPolygons = polygonizerService.polygonize(List.of(polyline), lines, option);

        Assertions.assertEquals(2, rawPolygons.size(), "应该成功构出2个多边形图斑");
        Assertions.assertEquals(10000.0, rawPolygons.get(0).getArea(), 0.001, "图斑1面积应为10000");
        Assertions.assertEquals(10000.0, rawPolygons.get(1).getArea(), 0.001, "图斑2面积应为10000");

        // 3. 构造注记文字 (一个位于第1个矩形内(50,50), 另一个位于第2个矩形内(250,250))
        CadTextEntity text1 = new CadTextEntity();
        text1.setText("DK001");
        text1.setPosition(new Coordinate(50.0, 50.0));

        CadTextEntity text2 = new CadTextEntity();
        text2.setText("DK002");
        text2.setPosition(new Coordinate(250.0, 250.0));

        List<CadFeatureDto> features = attributeMatcher.matchAttributes(rawPolygons, List.of(text1, text2));

        Assertions.assertEquals(2, features.size());
        boolean hasDk001 = features.stream().anyMatch(f -> "DK001".equals(f.getDkmc()));
        boolean hasDk002 = features.stream().anyMatch(f -> "DK002".equals(f.getDkmc()));
        Assertions.assertTrue(hasDk001, "地块1应成功挂接DK001属性");
        Assertions.assertTrue(hasDk002, "地块2应成功挂接DK002属性");
    }

    private CadLineEntity createLine(String layer, double x1, double y1, double x2, double y2) {
        CadLineEntity line = new CadLineEntity();
        line.setLayer(layer);
        line.setStartPoint(new Coordinate(x1, y1));
        line.setEndPoint(new Coordinate(x2, y2));
        return line;
    }
}
