package com.greatmap.agy.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.hr.entity.EmployeeProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeProfileMapper extends BaseMapper<EmployeeProfile> {

    /**
     * 根据姓名或工号精确查询员工档案
     */
    List<EmployeeProfile> findByNameOrId(@Param("keyword") String keyword);

    /**
     * 模糊搜索员工档案
     */
    List<EmployeeProfile> fuzzySearch(@Param("keyword") String keyword);
}
