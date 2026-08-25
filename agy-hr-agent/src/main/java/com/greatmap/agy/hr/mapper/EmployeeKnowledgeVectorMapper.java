package com.greatmap.agy.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmap.agy.hr.entity.EmployeeKnowledgeVector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeKnowledgeVectorMapper extends BaseMapper<EmployeeKnowledgeVector> {

    /**
     * 关键字模糊检索知识库
     */
    List<EmployeeKnowledgeVector> searchByKeyword(@Param("keyword") String keyword);
}
