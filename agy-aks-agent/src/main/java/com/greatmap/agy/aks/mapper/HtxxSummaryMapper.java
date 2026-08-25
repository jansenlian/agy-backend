package com.greatmap.agy.aks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.aks.entity.HtxxSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合同信息持久层接口
 */
@Mapper
public interface HtxxSummaryMapper extends BaseMapper<HtxxSummary> {

    @Select("SELECT * FROM jgpt_htxx WHERE htbh = #{htbh} AND (del_flag = 0 OR del_flag IS NULL) LIMIT 1")
    HtxxSummary findByHtbh(@Param("htbh") String htbh);

    @Select("SELECT * FROM jgpt_htxx WHERE (cbrmc LIKE CONCAT('%', #{nameOrKw}, '%') OR czffbmc LIKE CONCAT('%', #{nameOrKw}, '%') OR cbrzjhm = #{nameOrKw}) AND (del_flag = 0 OR del_flag IS NULL) LIMIT 10")
    List<HtxxSummary> findByNameOrKw(@Param("nameOrKw") String nameOrKw);
}
