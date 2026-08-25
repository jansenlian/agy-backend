package com.greatmap.agy.api.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典数据项视图对象
 */
@Data
@Schema(description = "数据字典数据项视图对象")
public class SysDictDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典数据主键 ID (String 防精度丢失)", example = "1")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "字典类型编码", example = "sys_user_sex")
    private String dictType;

    @Schema(description = "字典标签展示文本", example = "男")
    private String dictLabel;

    @Schema(description = "字典键值", example = "1")
    private String dictValue;

    @Schema(description = "排序号", example = "1")
    private Integer dictSort;

    @Schema(description = "父级节点 ID (0 表示根节点)", example = "0")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    @Schema(description = "样式属性 (CSS 类名)", example = "badge-primary")
    private String cssClass;

    @Schema(description = "表格回显回弹样式", example = "primary")
    private String listClass;

    @Schema(description = "是否默认值 (1-是 0-否)", example = "0")
    private Integer isDefault;

    @Schema(description = "状态 (1-正常 0-停用)", example = "1")
    private Integer status;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "创建时间", example = "2026-08-14 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "子级树形字典数据集合")
    private List<SysDictDataVO> children = new ArrayList<>();
}
