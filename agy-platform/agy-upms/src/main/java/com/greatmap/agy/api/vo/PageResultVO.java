package com.greatmap.agy.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 全局通用分页查询响应 VO
 *
 * @param <T> 数据模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一分页响应结构")
public class PageResultVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页数据集合
     */
    @Schema(description = "当前页数据记录集合")
    private List<T> records = Collections.emptyList();

    /**
     * 总记录数
     */
    @Schema(description = "符合条件的总记录数", example = "100")
    private long total = 0;

    /**
     * 每页条数
     */
    @Schema(description = "每页显示记录条数", example = "10")
    private long size = 10;

    /**
     * 当前页码
     */
    @Schema(description = "当前所在页码", example = "1")
    private long current = 1;

    /**
     * 总页数
     */
    @Schema(description = "计算所得总页数", example = "10")
    private long pages = 0;

    public static <T> PageResultVO<T> of(List<T> records, long total, long size, long current) {
        long pages = (size <= 0) ? 0 : (total + size - 1) / size;
        return new PageResultVO<>(records, total, size, current, pages);
    }
}
