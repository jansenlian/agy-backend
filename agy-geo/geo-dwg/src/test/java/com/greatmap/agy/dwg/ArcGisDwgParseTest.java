package com.greatmap.agy.dwg;

import com.alibaba.fastjson2.JSON;
import com.greatmap.agy.dwg.config.CadProperties;
import com.greatmap.agy.dwg.model.dto.CadFeatureDto;
import com.greatmap.agy.dwg.service.converter.LibreDwgConverter;
import com.greatmap.agy.dwg.service.impl.CadServiceImpl;
import com.greatmap.agy.dwg.service.parser.DxfGroupCodeParser;
import com.greatmap.agy.dwg.service.spatial.JtsPolygonizerService;
import com.greatmap.agy.dwg.service.spatial.SpatialAttributeMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class ArcGisDwgParseTest {

    @Test
    public void testParseArcGisExportedDwg() throws Exception {
        File dwgFile = new File("/Users/jianjian/Desktop/aa.dwg");
        if (!dwgFile.exists()) {
            return;
        }

        CadProperties props = new CadProperties();
        props.setDwg2dxfPath("/opt/homebrew/bin/dwg2dxf");
        props.setTempDir("./temp/test_cad");

        LibreDwgConverter converter = new LibreDwgConverter(props);
        DxfGroupCodeParser parser = new DxfGroupCodeParser();
        JtsPolygonizerService polygonizer = new JtsPolygonizerService();
        SpatialAttributeMatcher matcher = new SpatialAttributeMatcher();

        CadServiceImpl cadService = new CadServiceImpl(props, converter, parser, polygonizer, matcher);

        try (FileInputStream in = new FileInputStream(dwgFile)) {
            MockMultipartFile multipartFile = new MockMultipartFile("file", "aa.dwg", "application/octet-stream", in);
            List<CadFeatureDto> features = cadService.parseToFeatures(multipartFile, null);

            System.out.println("=== 实际解析结果 JSON ===");
            System.out.println(JSON.toJSONString(features));

            Assertions.assertFalse(features.isEmpty(), "要素列表不应为空");
            CadFeatureDto feat = features.get(0);
            System.out.println("图层名: " + feat.getLayer());
            System.out.println("地块名称: " + feat.getDkmc());
            System.out.println("属性字典: " + feat.getProperties());

            Assertions.assertEquals("湖北爱圣环保科技股份有限公司亚克力裂解再利用及配套项目二期工程", feat.getLayer());
            Assertions.assertEquals("地块1", feat.getDkmc());
            Assertions.assertEquals("4865.2", feat.getProperties().get("YDMJ"));
            Assertions.assertEquals("建字第420921202400032号", feat.getProperties().get("YGDZJGH"));
        }
    }
}
