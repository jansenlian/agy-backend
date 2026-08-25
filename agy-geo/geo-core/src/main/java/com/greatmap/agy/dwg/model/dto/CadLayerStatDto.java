package com.greatmap.agy.dwg.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "CAD图层统计信息")
public class CadLayerStatDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "图层名称", example = "DK")
    private String layerName;

    @Schema(description = "多段线数量", example = "42")
    private int polylineCount;

    @Schema(description = "线段数量", example = "120")
    private int lineCount;

    @Schema(description = "文字/注记数量", example = "35")
    private int textCount;

    @Schema(description = "其他图元数量", example = "5")
    private int otherCount;

    @Schema(description = "总图元数量", example = "202")
    private int totalCount;
}
