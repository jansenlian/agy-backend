package com.greatmap.agy.dwg.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "CAD矢量要素对象")
public class CadFeatureDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "要素唯一标识", example = "feat_01")
    private String id;

    @Schema(description = "地块名称/编号", example = "DK652801001")
    private String dkmc;

    @Schema(description = "所属CAD图层", example = "DK")
    private String layer;

    @Schema(description = "几何类型", example = "Polygon")
    private String geometryType;

    @Schema(description = "WKT格式几何数据", example = "POLYGON ((86.12 41.45, 86.13 41.45, 86.13 41.46, 86.12 41.46, 86.12 41.45))")
    private String wkt;

    @Schema(description = "图斑面积(平方米)", example = "12560.45")
    private Double area;

    @Schema(description = "要素属性字典", example = "{\"dkmc\": \"DK652801001\", \"area\": 12560.45, \"layer\": \"DK\", \"annotations\": [\"DK652801001\", \"水浇地\"]}")
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
}
