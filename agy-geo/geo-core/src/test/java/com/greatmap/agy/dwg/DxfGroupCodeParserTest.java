package com.greatmap.agy.dwg;

import com.greatmap.agy.dwg.model.entity.CadLineEntity;
import com.greatmap.agy.dwg.model.entity.CadPolylineEntity;
import com.greatmap.agy.dwg.model.entity.CadTextEntity;
import com.greatmap.agy.dwg.service.parser.DxfGroupCodeParser;
import com.greatmap.agy.dwg.service.parser.DxfGroupCodeParser.DxfParseResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;

public class DxfGroupCodeParserTest {

    private final DxfGroupCodeParser parser = new DxfGroupCodeParser();

    @Test
    public void testParseDxfEntities() throws Exception {
        File tempDxf = File.createTempFile("test_", ".dxf");
        tempDxf.deleteOnExit();

        // 构造最小合法DXF ENTITIES内容
        String dxfContent = "0\nSECTION\n2\nENTITIES\n" +
                // 闭合LWPOLYLINE
                "0\nLWPOLYLINE\n8\nDK\n70\n1\n90\n4\n" +
                "10\n0.0\n20\n0.0\n" +
                "10\n100.0\n20\n0.0\n" +
                "10\n100.0\n20\n100.0\n" +
                "10\n0.0\n20\n100.0\n" +
                // LINE线段
                "0\nLINE\n8\nJZX\n" +
                "10\n200.0\n20\n200.0\n30\n0.0\n" +
                "11\n300.0\n21\n200.0\n31\n0.0\n" +
                // TEXT注记
                "0\nTEXT\n8\nDK_TEXT\n1\nDK652801001\n" +
                "10\n50.0\n20\n50.0\n30\n0.0\n" +
                // MTEXT注记(带格式符)
                "0\nMTEXT\n8\nDK_TEXT\n1\n\\fSimSun;张三\\P承包地\n" +
                "10\n60.0\n20\n60.0\n30\n0.0\n" +
                "0\nENDSEC\n0\nEOF\n";

        try (FileWriter writer = new FileWriter(tempDxf)) {
            writer.write(dxfContent);
        }

        DxfParseResult result = parser.parse(tempDxf);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getPolylines().size(), "应正确解析1条多段线");
        CadPolylineEntity polyline = result.getPolylines().get(0);
        Assertions.assertEquals("DK", polyline.getLayer());
        Assertions.assertTrue(polyline.isClosed());
        Assertions.assertEquals(4, polyline.getVertices().size());

        Assertions.assertEquals(1, result.getLines().size(), "应正确解析1条线段");
        CadLineEntity line = result.getLines().get(0);
        Assertions.assertEquals("JZX", line.getLayer());

        Assertions.assertEquals(2, result.getTexts().size(), "应正确解析2条文字实体");
        CadTextEntity text1 = result.getTexts().get(0);
        Assertions.assertEquals("DK652801001", text1.getText());

        CadTextEntity mtext = result.getTexts().get(1);
        Assertions.assertEquals("张三 承包地", mtext.getText(), "应自动清洗MTEXT格式控制符");
    }
}
