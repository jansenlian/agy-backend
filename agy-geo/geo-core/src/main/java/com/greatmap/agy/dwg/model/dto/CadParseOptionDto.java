package com.greatmap.agy.dwg.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "CAD解析与图斑提取选项")
public class CadParseOptionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "需要提取的图层白名单列表(为空则提取全部图层)", example = "[\"DK\", \"JZX\", \"地块界线\"]")
    private Set<String> layers;

    @Schema(description = "是否自动进行JTS拓扑构面(合并线段构建闭合Polygon)", example = "true")
    @Builder.Default
    private boolean polygonize = true;

    @Schema(description = "是否自动执行点面包含挂接注记文字到地块属性", example = "true")
    @Builder.Default
    private boolean matchAttributes = true;

    @Schema(description = "过滤面积小于此阈值的噪点图斑(平方米)", example = "0.01")
    @Builder.Default
    private double minAreaThreshold = 0.01;
}
