package com.greatmap.agy.util.api.dto.doc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Swagger 响应属性模型
 *
 * @author AGY
 */
@Data
@Schema(description = "Swagger 接口模型属性定义")
public class ModelAttr implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "类名")
    private String className = StringUtils.EMPTY;

    @Schema(description = "属性名")
    private String name = StringUtils.EMPTY;

    @Schema(description = "类型")
    private String type = StringUtils.EMPTY;

    @Schema(description = "是否必填")
    private Boolean require = false;

    @Schema(description = "属性描述")
    private String description;

    @Schema(description = "嵌套属性列表")
    private List<ModelAttr> properties = new ArrayList<>();

    @Schema(description = "是否加载完成（防循环引用）")
    private boolean isCompleted = false;
}
