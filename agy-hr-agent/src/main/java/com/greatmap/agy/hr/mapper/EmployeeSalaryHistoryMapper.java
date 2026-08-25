package com.greatmap.agy.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.hr.entity.EmployeeSalaryHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeSalaryHistoryMapper extends BaseMapper<EmployeeSalaryHistory> {

    /**
     * 查询员工近期薪酬实际成本记录
     */
    List<EmployeeSalaryHistory> findRecentSalaries(@Param("nameOrId") String nameOrId);
}
