package com.greatmap.agy.aks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.aks.entity.DkhcSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地块现状核查持久层接口
 */
@Mapper
public interface DkhcSummaryMapper extends BaseMapper<DkhcSummary> {

    @Select("SELECT * FROM jgpt_dkhc WHERE hcdkbh = #{dkbh} AND (del_flag = 0 OR del_flag IS NULL) LIMIT 1")
    DkhcSummary findByDkbh(@Param("dkbh") String dkbh);

    @Select("SELECT * FROM jgpt_dkhc WHERE htbh = #{htbh} AND (del_flag = 0 OR del_flag IS NULL)")
    List<DkhcSummary> findByHtbh(@Param("htbh") String htbh);
}
