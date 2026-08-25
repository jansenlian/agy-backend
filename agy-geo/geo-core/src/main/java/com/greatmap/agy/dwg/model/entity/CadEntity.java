package com.greatmap.agy.dwg.model.entity;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public abstract class CadEntity {

    /**
     * 图元类型(LWPOLYLINE, LINE, TEXT, MTEXT, ARC, CIRCLE)
     */
    private String entityType;

    /**
     * 所属图层名称
     */
    private String layer;

    /**
     * 实体句柄
     */
    private String handle;

    /**
     * 颜色编号
     */
    private Integer color;

    /**
     * 扩展实体数据(XData, 例如 CASS 编码 SOUTH 属性)
     */
    private Map<String, List<String>> xdata = new LinkedHashMap<>();
}
