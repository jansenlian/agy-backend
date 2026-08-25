package com.greatmap.agy.aks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greatmap.agy.aks.entity.Yj;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 违法违规与风险预警持久层接口
 */
@Mapper
public interface YjMapper extends BaseMapper<Yj> {

    /**
     * 多维条件分页查询预警列表
     */
    Page<Yj> selectWarningPage(
            Page<Yj> page,
            @Param("areaCode") String areaCode,
            @Param("type") String type,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("keyword") String keyword);

    /**
     * 按预警类型聚合统计
     */
    List<Map<String, Object>> countWarningGroupByType(@Param("areaCode") String areaCode);

    /**
     * 按各区县分布统计预警总数
     */
    List<Map<String, Object>> countWarningGroupByDistrict();
}
